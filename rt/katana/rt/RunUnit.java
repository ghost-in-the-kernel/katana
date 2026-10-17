package katana.rt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The programs of a run (LANE-19, research §24): what they share, the codepage, UPSI, SYSIN, the
 * console, RETURN-CODE and each EXTERNAL record, and every separately compiled program called so far.
 * Under NODYNAM, IBM's default (PG 6.4 p. 366), a CALL literal reaches the program link-edited with the
 * main program, its static copy, and a CALL identifier a dynamic copy apart from it (PG 6.4 p. 552); under
 * DYNAM both reach the dynamic copy. CANCEL drops the dynamic copy only (LR 6.4 p. 327).
 */
public final class RunUnit {
    public final Codepage cp;
    public final Upsi upsi;
    public final Sysin in;
    public final Sysout out, err;
    /** RETURN-CODE, "01 RETURN-CODE GLOBAL PICTURE S9(4) USAGE BINARY VALUE ZERO" (LR 6.4 p. 24), the run unit's one register. */
    public final Field rc;
    private final boolean dynam;
    private final Map<String, Subprogram> statics = new HashMap<>(), dynamics = new HashMap<>();
    private final Map<String, Record> externals = new HashMap<>();
    private final Map<String, Object> externalFiles = new HashMap<>();
    private final java.util.Set<String> described = new java.util.HashSet<>();
    private Storage storage;

    public RunUnit(Codepage cp, Upsi upsi, Sysin in, Sysout out, Sysout err, boolean dynam) {
        this.cp = cp;
        this.upsi = upsi;
        this.in = in;
        this.out = out;
        this.err = err;
        this.dynam = dynam;
        rc = new Field(Record.of(2, cp), 0, Attr.numeric(Attr.Usage.BINARY, 4, 0, true));
        rc.put(new Decimal(java.math.BigInteger.ZERO, 0));
    }

    /**
     * CALL of a separately compiled program: a literal's name (`literal`) or an identifier's; false when it
     * cannot be found, the exception condition (LR 6.4 p. 323). An INITIAL program is cancelled when it
     * returns, as EXIT PROGRAM in one is a CANCEL (LR 6.4 p. 343).
     */
    // Decision: R-062
    public boolean call(String name, boolean literal, Field... using) {
        Map<String, Subprogram> copies = literal && !dynam ? statics : dynamics;
        Subprogram p = copies.get(name);
        if (p == null) {
            p = load(name);
            if (p == null) return false;
            copies.put(name, p);
        }
        p.call(using);
        if (p.initial() && copies.remove(name) != null) p.cancel();
        return true;
    }

    /**
     * Ends the run when a CALL without ON EXCEPTION names a program that cannot be found (LR 6.4 p. 323 leaves
     * the statement then; LE ends the enclave).
     */
    // Decision: R-062
    // Unsure: the abend code is LE's for an unhandled severity-3 condition (U4038), not read for this case. Stays: a mainframe run of a CALL to a missing program settles it; no NIST run with a report reaches one.
    public boolean found(boolean found, String name) {
        if (!found) throw new Abend(4038, "CALL " + name + ": the program cannot be found");
        return true;
    }

    /** CANCEL of a separately compiled program: its dynamic copy forgotten, its files closed; no action for one never called dynamically (LR 6.4 p. 327). */
    // Decision: R-062
    public void cancel(String name) {
        Subprogram p = dynamics.remove(name);
        if (p != null) p.cancel();
    }

    /** A program-name an identifier holds: its characters, trailing spaces dropped. */
    public String name(Field f) {
        byte[] b = new byte[f.attr().length()];
        System.arraycopy(f.record().bytes(), f.offset(), b, 0, b.length);
        return new String(b, cp.charset).stripTrailing();
    }

    /** An EXTERNAL record: the run unit's one representative of it, by its name (LR 6.4 p. 197). */
    // Decision: R-019
    public Record external(String name, int length) {
        Record r = externals.computeIfAbsent(name, n -> Record.of(length, cp));
        if (r.bytes().length != length) throw new Abend(4038, "EXTERNAL " + name + " described with " + length + " bytes and " + r.bytes().length);
        return r;
    }

    /** Whether this is the first program to describe an EXTERNAL record, and so the one to give it its initial values. */
    public boolean first(String name) {
        return described.add(name);
    }

    /** An EXTERNAL file's connector, by its name: the first program to describe it makes it. */
    @SuppressWarnings("unchecked")
    public <T> T externalFile(String name, java.util.function.Supplier<T> make) {
        return (T) externalFiles.computeIfAbsent(name, n -> make.get());
    }

    /** The step's z/OS storage (R-027), from the lane's `KATANA_TIOT`, made when a program first reads it. */
    public Storage storage() {
        if (storage == null) storage = Storage.of(System.getenv("KATANA_TIOT"), cp);
        return storage;
    }

    /** The step's z/OS storage given, in place of the one `KATANA_TIOT` describes (a test's). */
    public void storage(Storage given) {
        storage = given;
    }

    /** At the end of the run, normal or not: every called program's files closed. */
    public void close() {
        List<Subprogram> all = new ArrayList<>(statics.values());
        all.addAll(dynamics.values());
        statics.clear();
        dynamics.clear();
        for (Subprogram p : all) p.end();
    }

    /** A BY CONTENT argument: a copy of the item made at the CALL (LR 6.4 p. 321). */
    public static Field content(Field f) {
        byte[] b = new byte[f.attr().length()];
        System.arraycopy(f.record().bytes(), f.offset(), b, 0, b.length);
        return new Field(new Record(b, f.record().codepage()), 0, f.attr());
    }

    /** The program a name loads: the lowered class of that name on the thread's class path, made for this run unit; null when there is none. */
    private Subprogram load(String name) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> c = Class.forName(className(name), true, loader != null ? loader : RunUnit.class.getClassLoader());
            if (!Subprogram.class.isAssignableFrom(c)) return null;
            return (Subprogram) c.getConstructor(RunUnit.class).newInstance(this);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The Java class a program-name is lowered to: as `lower/Lower.className` makes it (`LoweredNamesTest`). */
    public static String className(String program) {
        String name = program.replaceAll("[^A-Za-z0-9_]", "_");
        return Character.isJavaIdentifierStart(name.charAt(0)) ? name : "P" + name;
    }
}
