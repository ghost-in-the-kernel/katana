package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import katana.front.Settings;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Test;

/** R-081: the fixture's `define` lines are one declaration, read by both lanes alike: cobc's `-D`, the front end's Settings. */
class DefinitionsTest {
    @Test
    void noDefineLineDeclaresNothing() {
        Fixture f = Fixture.read("job T\n");
        assertNull(Definitions.of(f));
        assertFalse(Definitions.settings(f).isDeclared());
        assertEquals(List.of(), Definitions.cobcArguments(f));
    }

    @Test
    void defineDashDeclaresThereAreNone() {
        Fixture f = Fixture.read("job T\ndefine -\n");
        assertEquals(Map.of(), Definitions.of(f));
        assertTrue(Definitions.settings(f).isDeclared());
        assertEquals(List.of(), Definitions.cobcArguments(f));
    }

    @Test
    void eachNameGoesToBothLanes() {
        Fixture f = Fixture.read("job T\ndefine ACTIVATE=1\ndefine CS=EBCDIC\n");
        assertEquals(List.of("-D", "ACTIVATE=1", "-D", "CS=EBCDIC"), Definitions.cobcArguments(f));
        assertEquals(Settings.declared(Map.of("ACTIVATE", "1", "CS", "EBCDIC"), false), Definitions.settings(f));
    }
}
