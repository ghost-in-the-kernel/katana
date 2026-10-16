package katana.oracle.lane;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CREASTMT's statements against what they summarize (research §25): the cobc lane abends before writing
 * any, so the java lane's are held to their own input. Each statement's transactions are the sorted
 * transaction file's (TRXFL, 350-byte records, the card number then the transaction ID), each once, and
 * its Total EXP is the sum of its lines.
 */
final class Statements {
    private static final Pattern LINE = Pattern.compile("^(\\d{16}) .*\\$\\s+([\\d.]+)(-?)\\s*$"), TOTAL = Pattern.compile("^Total EXP:.*\\$\\s+([\\d.]+)(-?)\\s*$");

    private Statements() {}

    /** What the statements hold, as a sentence, or what is wrong with them. */
    static String check(byte[] statements, byte[] transactions) {
        String ps = new String(statements, StandardCharsets.ISO_8859_1), tr = new String(transactions, StandardCharsets.ISO_8859_1);
        List<String> listed = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (int k = 0; k + 80 <= ps.length(); k += 80) {
            String line = ps.substring(k, k + 80);
            Matcher m = LINE.matcher(line), t = TOTAL.matcher(line);
            if (m.matches()) {
                listed.add(m.group(1));
                sum = sum.add(amount(m.group(2), m.group(3)));
            } else if (t.matches()) {
                if (sum.compareTo(amount(t.group(1), t.group(2))) != 0) return "statement " + (count + 1) + ": its lines sum to " + sum + ", its total says " + t.group(1) + t.group(2);
                count++;
                sum = BigDecimal.ZERO;
            }
        }
        Set<String> input = new HashSet<>();
        for (int k = 0; k + 350 <= tr.length(); k += 350) input.add(tr.substring(k + 16, k + 32));
        if (listed.size() != new HashSet<>(listed).size() || !new HashSet<>(listed).equals(input)) return "the statements list " + listed.size() + " transactions, the input holds " + input.size();
        return count + " statements, " + listed.size() + " transactions, each once, every total its lines' sum";
    }

    private static BigDecimal amount(String digits, String sign) {
        BigDecimal a = new BigDecimal(digits);
        return sign.isEmpty() ? a : a.negate();
    }
}
