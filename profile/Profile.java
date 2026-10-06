package katana.profile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * The dialect profile (docs/PROFILE.md): the compiler, its version, its dialect and the GnuCOBOL
 * dialect flags set beyond the dialect, which the cobc lane runs under; and the IBM compile options
 * the source program was compiled with on z/OS (R-049), which the literal lowering reads. It is
 * content-addressed by the SHA-256 of its canonical text; a proof, a fixture and a ledger line are
 * valid for one profile (P-12).
 */
public record Profile(String compiler, String version, String dialect, Map<String, String> flags, Map<String, String> ibm) {
    /** The fields of a profile file, in canonical order (PROFILE-02). */
    public static final List<String> FIELDS = List.of("compiler", "version", "dialect", "flag",
            "ibm.trunc", "ibm.numproc", "ibm.arith", "ibm.codepage", "ibm.invdata", "ibm.version", "ibm.dispsign", "ibm.wsclear");
    /** What each IBM field may hold (docs/PROFILE.md's fields table). */
    static final Map<String, Pattern> IBM = ibmValues();

    public Profile {
        flags = new TreeMap<>(flags);
        ibm = new TreeMap<>(ibm);
    }

    private static Map<String, Pattern> ibmValues() {
        Map<String, Pattern> values = new LinkedHashMap<>();
        values.put("ibm.trunc", Pattern.compile("STD|OPT|BIN"));
        values.put("ibm.numproc", Pattern.compile("PFD|NOPFD|MIG"));
        values.put("ibm.arith", Pattern.compile("COMPAT|EXTEND"));
        values.put("ibm.codepage", Pattern.compile("\\d{1,5}"));
        values.put("ibm.invdata", Pattern.compile("NO|PFD|NOPFD|MIG|[A-Z]+,[A-Z]+"));
        values.put("ibm.version", Pattern.compile("\\d+\\.\\d+"));
        values.put("ibm.dispsign", Pattern.compile("COMPAT|SEP"));
        values.put("ibm.wsclear", Pattern.compile("NO|YES"));
        return values;
    }

    /** Reads a profile file; a missing, repeated or unknown field, or an IBM value outside its set, is refused, never defaulted (PROFILE-02). */
    public static Profile read(String text) {
        Map<String, String> fields = new TreeMap<>();
        Map<String, String> flags = new TreeMap<>();
        for (String line : text.lines().toList()) {
            String body = (line.contains("#") ? line.substring(0, line.indexOf('#')) : line).trim();
            if (body.isEmpty()) continue;
            String[] kv = body.split("\\s+", 2);
            if (kv.length != 2 || !FIELDS.contains(kv[0])) throw new IllegalArgumentException("not a profile field: " + line);
            if (kv[0].equals("flag")) {
                String[] flag = kv[1].split("=", 2);
                if (flag.length != 2 || flags.put(flag[0], flag[1]) != null) throw new IllegalArgumentException("a flag is name=value, once: " + line);
            } else if (fields.put(kv[0], kv[1]) != null) {
                throw new IllegalArgumentException("repeated: " + line);
            } else if (IBM.containsKey(kv[0]) && !IBM.get(kv[0]).matcher(kv[1]).matches()) {
                throw new IllegalArgumentException("not a value of " + kv[0] + ": " + line);
            }
        }
        for (String field : FIELDS) {
            if (!field.equals("flag") && !fields.containsKey(field)) throw new IllegalArgumentException("missing: " + field);
        }
        Map<String, String> ibm = new TreeMap<>();
        IBM.keySet().forEach(name -> ibm.put(name, fields.get(name)));
        truncation(fields.get("dialect"), flags.get("binary-truncate"), ibm.get("ibm.trunc"));
        return new Profile(fields.get("compiler"), fields.get("version"), fields.get("dialect"), flags, ibm);
    }

    /** PROFILE-04: the lane truncates binary items as ibm.trunc says (STD and OPT truncate, BIN does not); `-std=ibm` does not by itself. */
    private static void truncation(String dialect, String flag, String trunc) {
        String wanted = trunc.equals("BIN") ? "no" : "yes";
        if (flag != null && !flag.equals(wanted)) throw new IllegalArgumentException("ibm.trunc " + trunc + " needs flag binary-truncate=" + wanted);
        if (flag == null && dialect.equals("ibm")) throw new IllegalArgumentException("dialect ibm does not truncate binary items; state flag binary-truncate=" + wanted);
    }

    /** What a profile states that Katana flags rather than models: NUMPROC(MIG) is Enterprise COBOL 4 only (docs/PROFILE.md). */
    public List<String> findings() {
        return "MIG".equals(ibm.get("ibm.numproc")) ? List.of("NUMPROC(MIG) is Enterprise COBOL 4 only; 6.x does not support it") : List.of();
    }

    /** The text the hash is taken over: one field a line, in FIELDS order, flags sorted by name (PROFILE-01). */
    public String canonical() {
        StringBuilder text = new StringBuilder();
        text.append("compiler ").append(compiler).append('\n');
        text.append("version ").append(version).append('\n');
        text.append("dialect ").append(dialect).append('\n');
        flags.forEach((name, value) -> text.append("flag ").append(name).append('=').append(value).append('\n'));
        IBM.keySet().forEach(name -> text.append(name).append(' ').append(ibm.get(name)).append('\n'));
        return text.toString();
    }

    /** "sha256:" and the hex SHA-256 of the canonical text (PROFILE-01). */
    public String hash() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical().getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The cobc arguments this profile means: the dialect, then each flag (PROFILE-03). */
    public List<String> cobcArguments() {
        List<String> arguments = new ArrayList<>(List.of("-std=" + dialect));
        flags.forEach((name, value) -> arguments.add(switch (value) {
            case "yes" -> "-f" + name;
            case "no" -> "-fno-" + name;
            default -> "-f" + name + "=" + value;
        }));
        return arguments;
    }
}
