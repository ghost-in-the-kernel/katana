package katana.intent.rules;

import java.util.List;
import katana.intent.types.Fact;
import katana.sir.types.Program;

/** One inference rule (docs/ROADMAP.md 5.2): its name and version, the evidence its facts cite, and a confidence never above its recorded precision. */
public interface Rule {
    String name();

    int version();

    /** The confidence its facts carry: at most its precision recorded on the key (`RulesBenchIntegrationTest`). */
    double confidence();

    /** The facts it infers on a program, each citing the IR nodes it read. */
    List<Fact> infer(Program p);
}
