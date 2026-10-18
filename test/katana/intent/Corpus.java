package katana.intent;

import java.util.List;
import java.util.Map;
import katana.sir.types.Program;

/** The programs a review test may read beyond the one it checks (research §34): a CALL's other end by name, and the programs whose source names an item. */
interface Corpus {
    /** The program of that PROGRAM-ID, or null where it is not one of the corpus's COBOL programs. */
    Program get(String program);

    /** The programs whose source names the word: a superset of those that hold or call it. */
    List<Program> mentioning(String word);

    /** A corpus of given programs, every one taken to mention every word (the small programs of the unit tests). */
    static Corpus of(Map<String, Program> programs) {
        return new Corpus() {
            public Program get(String program) {
                return programs.get(program);
            }

            public List<Program> mentioning(String word) {
                return List.copyOf(programs.values());
            }
        };
    }
}
