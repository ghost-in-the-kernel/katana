package katana.sir.types;

import java.util.List;

/**
 * A file: its name, what it is ASSIGNed to, whether it is OPTIONAL, its organization and access, its
 * RECORD KEY item and ALTERNATE keys (0 and empty when not indexed), its RELATIVE KEY item (0 for none),
 * its FILE STATUS item and the second one a VSAM file may name (0 for none; SIR-09), what its FD or SD
 * entry says (`Fd`, null with none; SIR-11), its records (the 01s under its FD), the smallest and
 * largest record in bytes, and its origin.
 */
public record FileDescriptor(String name, String assign, boolean optional, Organization organization, Access access, int recordKey, List<AlternateKey> alternateKeys,
        int relativeKey, int status, int vsamStatus, Fd fd, List<Integer> records, int minRecord, int maxRecord, Origin origin) {
    public enum Organization { SEQUENTIAL, LINE_SEQUENTIAL, INDEXED, RELATIVE }

    public enum Access { SEQUENTIAL, RANDOM, DYNAMIC }

    public record AlternateKey(int item, boolean duplicates) {}

    /**
     * What a file's FD (or SD, `sort`) entry says, as data (SIR-11): EXTERNAL, GLOBAL, the RECORD clause
     * (from and to in bytes, 0 when not written; VARYING; its DEPENDING ON item, 0 for none), the
     * `Linage` (null for none), the CODE-SET alphabet and the RECORDING MODE ("" for none). LABEL
     * RECORDS, VALUE OF, DATA RECORDS and BLOCK CONTAINS change nothing a program does and are not kept.
     */
    public record Fd(boolean sort, boolean external, boolean global, int recordFrom, int recordTo, boolean varying, int recordDepending,
            Linage linage, String codeSet, String recordingMode) {
        public Fd {
            if (recordTo != 0 && recordFrom > recordTo) throw new Invalid("SIR-11", "a RECORD clause from " + recordFrom + " to " + recordTo);
        }
    }

    /**
     * A LINAGE clause (SIR-11): the page body, the first footing line, the top and bottom margins, each a
     * numeric literal or a field (null when the phrase is not written: footing is then the page body,
     * the margins zero, LR 6.4 p. 190).
     */
    public record Linage(Value body, Value footing, Value top, Value bottom) {
        public Linage {
            for (Value v : java.util.Arrays.asList(body, footing, top, bottom)) {
                if (v != null && !(v instanceof Value.Field) && !(v instanceof Value.Literal l && l.kind() == Value.Literal.Kind.NUMERIC && l.scale() == 0 && !l.payload().startsWith("-"))) {
                    throw new Invalid("SIR-11", "a LINAGE value is an unsigned integer or an item, not " + v);
                }
            }
            if (body == null) throw new Invalid("SIR-11", "a LINAGE clause has a page body");
            if (body instanceof Value.Literal b && b.payload().equals("0")) throw new Invalid("SIR-11", "a LINAGE page body is greater than zero");
            if (body instanceof Value.Literal b && footing instanceof Value.Literal f && (f.payload().equals("0") || Long.parseLong(f.payload()) > Long.parseLong(b.payload()))) {
                throw new Invalid("SIR-11", "a LINAGE footing line is in the page body, 1 to " + b.payload());
            }
        }
    }

    public FileDescriptor {
        alternateKeys = List.copyOf(alternateKeys);
        records = List.copyOf(records);
        Invalid.origin(origin, "file " + name);
    }
}
