import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** The tests of docs/ROADMAP.md 4.1 under JUnit, the documents' and the kernel's: each must report no problem. */
class DocTests {
    @Test
    void ghost() {
        assertEquals(List.of(), GhostTest.run().problems());
    }

    @Test
    void glossary() {
        assertEquals(List.of(), GlossaryTest.run().problems());
    }

    @Test
    void docs() {
        assertEquals(List.of(), DocsTest.run().problems());
    }

    @Test
    void modules() {
        assertEquals(List.of(), ModulesTest.run().problems());
    }

    @Test
    void checks() {
        assertEquals(List.of(), ChecksTest.run().problems());
    }

    @Test
    void discoveries() {
        assertEquals(List.of(), DiscoveriesDocTest.run().problems());
    }

    @Test
    void journal() {
        assertEquals(List.of(), JournalTest.run().problems());
    }

    @Test
    void license() {
        assertEquals(List.of(), LicenseTest.run().problems());
    }

    @Test
    void supplyChain() {
        assertEquals(List.of(), SupplyChainTest.run().problems());
    }

    @Test
    void duplicates() {
        assertEquals(List.of(), DuplicatesDocTest.run().problems());
    }

    @Test
    void passes() {
        assertEquals(List.of(), PassesDocTest.run().problems());
    }

    @Test
    void todo() {
        assertEquals(List.of(), TodoTest.run().problems());
    }

    @Test
    void kernelMap() {
        assertEquals(List.of(), KernelMapTest.run().problems());
    }

    @Test
    void wayBack() {
        assertEquals(List.of(), WayBackTest.run().problems());
    }

    @Test
    void parallel() {
        assertEquals(List.of(), ParallelTest.run().problems());
    }

    @Test
    void metrics() {
        assertEquals(List.of(), MetricsTest.run().problems());
    }

    @Test
    void escalations() {
        assertEquals(List.of(), EscalationsTest.run().problems());
    }
}
