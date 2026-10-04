import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** The doc tests of docs/ROADMAP.md 4.1 under JUnit: each must report no problem. */
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
    void todo() {
        assertEquals(List.of(), TodoTest.run().problems());
    }
}
