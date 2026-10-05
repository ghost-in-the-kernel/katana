package katana.oracle.lane;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * The hand-written Java program of phase 1.8's check, in the shape every generated program takes
 * (docs/LANES.md LANE-06 to LANE-08): `run` copies INFILE to OUTFILE, record for record (20 bytes, the
 * COPYREC layout), and says how many, as COPYREC.cbl does in the cobc lane; `main` is the adapter a
 * process runs.
 */
public final class CopyRecords {
    private CopyRecords() {}

    /** The program: its DDs by name (without `DD_`), its PARM, its SYSIN and its stdout; it returns its return code. */
    public static int run(Map<String, String> dd, String parm, InputStream sysin, PrintStream sysout) {
        try {
            byte[] in = Files.readAllBytes(Path.of(dd.get("INFILE")));
            int records = in.length / 20;
            Files.write(Path.of(dd.get("OUTFILE")), java.util.Arrays.copyOf(in, records * 20));
            sysout.println("COPIED " + String.format("%05d", records) + " RECORDS");
            return 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** LANE-06's adapter: the DDs from `DD_` variables, the PARM the one argument, the return code the exit code. */
    public static void main(String[] args) {
        Map<String, String> dd = new HashMap<>();
        System.getenv().forEach((k, v) -> {
            if (k.startsWith("DD_")) dd.put(k.substring(3), v);
        });
        System.out.flush();
        System.exit(run(dd, args.length > 0 ? args[0] : "", System.in, System.out));
    }
}
