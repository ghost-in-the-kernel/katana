package katana.oracle.nist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * NIST's Executive Routine, EXEC85, run as the CCVS85 User Guide 4.2 says (3.2 to 3.5): taken from the
 * population file, its five files named (3.3), compiled, and fed the control cards (3.4); its source
 * output read back as a population. Every file it touches holds 80-character records with no line
 * ends, as EXEC85 assumes (3.1), so nothing outside COBOL 85 is needed to run it.
 */
public final class Exec85 {
    /** The one copy of `newcob.val` the NIST run stands on (corpus/fetch.sh, R-044). */
    public static final String POPULATION_SHA256 = "744a04982095a3abea29a9df5faf63d226083edecc7b5bf34bc412eae0d53274";
    static final String POPULATION = "population.dat";
    static final String SOURCE = "source.dat";
    static final String CARDS = "cards.dat";
    static final String PRINT = "exec85.print";
    private static final Map<String, String> FILES = Map.of(
            "XXXXX001", POPULATION, "XXXXX002", SOURCE, "XXXXX003", "newpop.dat", "XXXXX055", PRINT, "XXXXX058", CARDS);
    private static final int WIDTH = 80;

    private Exec85() {}

    /** The cards EXEC85 reads: every line of the annotated file but its notes and blank lines. */
    public static List<String> cards(String annotated) {
        return annotated.lines().filter(line -> !line.isBlank() && !line.startsWith("#")).toList();
    }

    /** A profile's cards: the base cards with each X-card the overlay holds replaced by the overlay's (1.2). */
    public static List<String> cards(String base, String overlay) {
        Map<String, String> over = new java.util.LinkedHashMap<>();
        for (String card : cards(overlay)) {
            if (!card.startsWith("X-")) throw new IllegalArgumentException("an overlay holds X-cards only: " + card);
            over.put(card.substring(0, 5), card);
        }
        List<String> merged = new ArrayList<>();
        for (String card : cards(base)) merged.add(card.startsWith("X-") ? over.getOrDefault(card.substring(0, 5), card) : card);
        for (String number : over.keySet()) {
            if (merged.stream().noneMatch(c -> c.startsWith(number))) throw new IllegalArgumentException(number + " is not in the base cards");
        }
        return merged;
    }

    /**
     * The runtime switches the cards ask for: X-51's switch on and X-52's off (Appendix D), as cobc's
     * COB_SWITCH_n, where SWITCH-n and UPSI-n are both switch n.
     */
    public static Map<String, String> switches(List<String> cards) {
        Map<String, String> env = new java.util.LinkedHashMap<>();
        for (String card : cards) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^X-05([12])\\d*\\s+(?:SWITCH-|UPSI-)(\\d)\\.").matcher(card);
            if (m.find()) env.put("COB_SWITCH_" + m.group(2), m.group(1).equals("1") ? "ON" : "OFF");
        }
        if (env.size() != 2) throw new IllegalArgumentException("X-51 and X-52 must each name a switch: " + env);
        return env;
    }

    /** EXEC85's source from the population, each of its five files assigned to a name in its folder. */
    public static List<String> program(List<Population.Member> population) {
        Population.Member exec85 = population.stream().filter(m -> m.name().equals("EXEC85")).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("the population holds no EXEC85"));
        List<String> lines = new ArrayList<>();
        for (String line : exec85.lines()) {
            for (var file : FILES.entrySet()) line = line.replace(file.getKey(), "\"" + file.getValue() + "\"");
            lines.add(line);
        }
        return lines;
    }

    /** Lines as 80-character records, padded with spaces; a longer line is refused, never cut. */
    public static byte[] records(List<String> lines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (String line : lines) {
            if (line.length() > WIDTH) throw new IllegalArgumentException("longer than 80 characters: " + line);
            out.writeBytes((line + " ".repeat(WIDTH - line.length())).getBytes(StandardCharsets.ISO_8859_1));
        }
        return out.toByteArray();
    }

    /** Records of a fixed width back to lines; a partial record at the end is refused. */
    public static List<String> lines(byte[] records, int width) {
        if (records.length % width != 0) throw new IllegalArgumentException(records.length + " bytes is not whole " + width + "-character records");
        List<String> lines = new ArrayList<>();
        for (int at = 0; at < records.length; at += width) lines.add(new String(records, at, width, StandardCharsets.ISO_8859_1));
        return lines;
    }

    /** Where EXEC85 left an undefined X-card in live code (not a comment), as "program line-number". */
    public static List<String> undefined(List<Population.Member> extracted) {
        List<String> found = new ArrayList<>();
        for (Population.Member member : extracted) {
            for (String line : member.lines()) {
                boolean live = line.length() > 6 && line.charAt(6) != '*';
                if (live && line.contains("**** X-CARD UNDEFINED ****")) found.add(member.name() + " " + line.substring(0, 6));
            }
        }
        return found;
    }

    /**
     * Runs EXEC85 on `newcob.val` in an empty folder and returns what it extracted. The population is
     * refused unless it is the pinned copy; a compile or run failure is an error, with cobc's words.
     */
    public static List<Population.Member> run(Path newcob, List<String> cards, Path work, Cobc cobc) {
        try {
            byte[] bytes = Files.readAllBytes(newcob);
            String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            if (!sha.equals(POPULATION_SHA256)) throw new IllegalArgumentException(newcob + " is not the pinned newcob.val: " + sha);
            List<String> population = new String(bytes, StandardCharsets.ISO_8859_1).lines().toList();
            Files.write(work.resolve(POPULATION), records(population));
            Files.write(work.resolve(CARDS), records(cards));
            Files.write(work.resolve("EXEC85.cbl"), program(Population.read(population)), StandardCharsets.ISO_8859_1);
            Runner.Result compiled = cobc.compile(work, "EXEC85.cbl", List.of());
            if (!compiled.ok()) throw new IllegalStateException("EXEC85 did not compile:\n" + compiled.output());
            Runner.Result ran = cobc.execute(work, "EXEC85", Map.of());
            if (!ran.ok()) throw new IllegalStateException("EXEC85 failed:\n" + ran.output());
            return Population.read(lines(Files.readAllBytes(work.resolve(SOURCE)), WIDTH));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
