import java.util.List;
import java.util.Map;

/**
 * Holds docs/KERNEL-MAP.md (docs/ROADMAP.md 4.1, 4.3): every function in a module has a row, and a
 * function removed or renamed has a later row closing it (`removed:` or `superseded-by:`).
 */
final class KernelMapTest {
    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        Map<String, String> one = Map.of("sir/A.java", "class A { int f() { return 1; } }");
        String held = KernelMap.write(HEADER, one, "2026-10-04");
        return new Check("kernel map")
                .repository(KernelMap.check(Check.read(KernelMap.PATH), Repo.moduleSources()))
                .rejects("a function the map does not hold", () -> KernelMap.check(HEADER, one))
                .rejects("a function gone with no row closing it", () -> KernelMap.check(held, Map.of()))
                .accepts("a function closed by a later row", () -> KernelMap.check(held + "| sir | `sir/A.java:A.f` |  | 2026-10-05 | removed: abc1234 |\n", Map.of()))
                .rejects("a successor the map does not hold", () -> KernelMap.check(held + "| sir | `sir/A.java:A.f` |  | 2026-10-05 | superseded-by: `sir/A.java:A.g` |\n", Map.of()))
                .accepts("the map the generator just wrote", () -> KernelMap.check(held, one))
                .accepts("a decision cited under a Javadoc", () -> KernelMap.write(HEADER, CITED, "2026-10-04").contains("| R-007 |") ? List.of() : List.of("no R-007"))
                .accepts("a second write, which adds nothing", () -> held.equals(KernelMap.write(held, one, "2026-10-06")) ? List.of() : List.of("changed"));
    }

    private static final Map<String, String> CITED = Map.of("sir/B.java", """
            class B {
                /** What it is for. */
                // Decision: R-007
                int g() { return 2; }
            }
            """);

    private static final String HEADER = """
            | Module | Function | Stands on | First seen | Superseded |
            | --- | --- | --- | --- | --- |
            """;
}
