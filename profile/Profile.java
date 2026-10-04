package katana.profile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The dialect profile (docs/PROFILE.md): the compiler, its version, its dialect and the GnuCOBOL
 * dialect flags set beyond the dialect, content-addressed by the SHA-256 of its canonical text. A
 * proof, a fixture and a ledger line are valid for one profile (P-12).
 */
public record Profile(String compiler, String version, String dialect, Map<String, String> flags) {
    /** The fields of a profile file, in canonical order (PROFILE-02). */
    public static final List<String> FIELDS = List.of("compiler", "version", "dialect", "flag");

    public Profile {
        flags = new TreeMap<>(flags);
    }

    /** Reads a profile file; a missing, repeated or unknown field is refused, never defaulted (PROFILE-02). */
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
            }
        }
        for (String field : List.of("compiler", "version", "dialect")) {
            if (!fields.containsKey(field)) throw new IllegalArgumentException("missing: " + field);
        }
        return new Profile(fields.get("compiler"), fields.get("version"), fields.get("dialect"), flags);
    }

    /** The text the hash is taken over: one field a line, in FIELDS order, flags sorted by name (PROFILE-01). */
    public String canonical() {
        StringBuilder text = new StringBuilder();
        text.append("compiler ").append(compiler).append('\n');
        text.append("version ").append(version).append('\n');
        text.append("dialect ").append(dialect).append('\n');
        flags.forEach((name, value) -> text.append("flag ").append(name).append('=').append(value).append('\n'));
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
