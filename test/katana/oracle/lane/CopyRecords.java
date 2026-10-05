package katana.oracle.lane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The hand-written Java program of phase 1.8's check: under the DD contract (docs/LANES.md) it copies
 * INFILE to OUTFILE, record for record (20 bytes, the COPYREC layout), and says how many, as COPYREC.cbl
 * does in the cobc lane.
 */
public final class CopyRecords {
    private CopyRecords() {}

    public static void main(String[] args) throws IOException {
        byte[] in = Files.readAllBytes(Path.of(System.getenv("DD_INFILE")));
        int records = in.length / 20;
        Files.write(Path.of(System.getenv("DD_OUTFILE")), java.util.Arrays.copyOf(in, records * 20));
        System.out.println("COPIED " + String.format("%05d", records) + " RECORDS");
    }
}
