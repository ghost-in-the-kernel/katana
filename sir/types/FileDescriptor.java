package katana.sir.types;

import java.util.List;

/**
 * A file: its name, what it is ASSIGNed to, its organization and access, its RECORD KEY item and
 * ALTERNATE keys (0 and empty when not indexed), its RELATIVE KEY item (0 for none), its FILE STATUS item (0 for none), its records (the
 * 01s under its FD), the smallest and largest record in bytes, and its origin.
 */
public record FileDescriptor(String name, String assign, Organization organization, Access access, int recordKey, List<AlternateKey> alternateKeys,
        int relativeKey, int status, List<Integer> records, int minRecord, int maxRecord, Origin origin) {
    public enum Organization { SEQUENTIAL, LINE_SEQUENTIAL, INDEXED, RELATIVE }

    public enum Access { SEQUENTIAL, RANDOM, DYNAMIC }

    public record AlternateKey(int item, boolean duplicates) {}

    public FileDescriptor {
        alternateKeys = List.copyOf(alternateKeys);
        records = List.copyOf(records);
        Invalid.origin(origin, "file " + name);
    }
}
