import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Holds R-045 (written in tithe pass 2, C4; named there since phase 1, missing until then): no file in
 * the repository outside `corpus/` and `third_party/` is a PDF (R-082: IBM's manuals stay uncommitted), or has a
 * header that names the GPL, so GnuCOBOL's
 * code, harness scripts and expected results, or any other GPL file, cannot sit in the tree unnoticed.
 * A header is a file's first 30 lines; a document that discusses the GPL further down is not one.
 */
// Decision: R-045
final class LicenseTest {
    private static final Pattern GPL = Pattern.compile("GNU (Lesser |Library |Affero )?General Public License|SPDX-License-Identifier:\\s*(A|L)?GPL");
    private static final List<String> OUTSIDE = List.of("corpus/", "third_party/", "build/", ".git/", ".gradle/");

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        String gpl = "/*\n * This program is free software; you can redistribute it under the terms of the GNU General " + "Public License.\n */\n";
        return new Check("license")
                .repository(check(files()))
                .rejects("a GPL header in the tree", () -> check(Map.of("oracle/nist/expand.pl", gpl)))
                .rejects("an SPDX GPL line", () -> check(Map.of("scripts/x.sh", "#!/bin/sh\n# SPDX-License-" + "Identifier: GPL-3.0-or-later\n")))
                .rejects("a manual's PDF in the tree", () -> check(Map.of("docs/research/SC27-8713.pdf", "%PDF-1.7\n")))
                .accepts("a manual's PDF under corpus/", () -> check(Map.of("corpus/ibm-manuals/SC27-8713.pdf", "%PDF-1.7\n")))
                .accepts("the GPL under corpus/", () -> check(Map.of("corpus/gnucobol-autotests/tests/x.at", gpl)))
                .accepts("the GPL discussed below a header", () -> check(Map.of("docs/X.md", "# X\n".repeat(30) + "GNU General " + "Public License\n")));
    }

    /** Each file, by path from the root, whose first 30 lines name the GPL, outside the folders that may hold it. */
    static List<String> check(Map<String, String> files) {
        List<String> failures = new ArrayList<>();
        for (var f : files.entrySet()) {
            if (OUTSIDE.stream().anyMatch(f.getKey()::startsWith)) continue;
            if (f.getKey().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
                failures.add(f.getKey() + ": a PDF in the tree; a manual stays uncommitted under corpus/ (R-082)");
                continue;
            }
            String header = String.join("\n", f.getValue().lines().limit(30).toList());
            if (GPL.matcher(header).find()) failures.add(f.getKey() + ": its header names the GPL; a GPL file stays under corpus/ or third_party/ (R-045)");
        }
        return failures;
    }

    /** Every text file of the working tree, outside the folders the check skips. */
    private static Map<String, String> files() {
        Map<String, String> files = new TreeMap<>();
        try (Stream<Path> walk = Files.walk(Check.ROOT)) {
            for (Path p : walk.filter(Files::isRegularFile).toList()) {
                String rel = Check.ROOT.relativize(p).toString().replace('\\', '/');
                if (OUTSIDE.stream().anyMatch(rel::startsWith) || Files.size(p) > 2_000_000) continue;
                files.put(rel, new String(Files.readAllBytes(p), StandardCharsets.ISO_8859_1));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return files;
    }
}
