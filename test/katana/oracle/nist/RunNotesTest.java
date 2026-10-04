package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class RunNotesTest {
    private static final RunNotes NOTES = RunNotes.read("""
            # a note
            DB*      debug=on        # module default
            DB102A   debug=off       # its own line wins
            DB103M   debug=on,off    # two runs
            IX217A   absent=INDEX1,INDEX2
            """);

    @Test
    void aProgramsOwnLineThenItsModulesThenNothing() {
        assertEquals(List.of("off"), NOTES.of("DB102A", "DB").debug());
        assertEquals(List.of("on"), NOTES.of("DB104A", "DB").debug());
        assertEquals(List.of("on", "off"), NOTES.of("DB103M", "DB").debug());
        assertEquals(List.of(""), NOTES.of("NC101A", "NC").debug());
        assertEquals(List.of("INDEX1", "INDEX2"), NOTES.of("IX217A", "IX").absent());
    }

    @Test
    void anUnknownSettingIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> RunNotes.read("NC101A speed=fast"));
        assertThrows(IllegalArgumentException.class, () -> RunNotes.read("NC101A debug"));
    }

    @Test
    void theRepositorysNotesRead() throws Exception {
        RunNotes.read(Files.readString(Path.of(System.getProperty("katana.root", "."), "oracle/nist/run-notes")));
    }
}
