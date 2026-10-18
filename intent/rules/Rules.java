package katana.intent.rules;

import java.util.ArrayList;
import java.util.List;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.sir.types.Program;

/** Every rule, in the order they are run, and a program's Intent IR from them. */
public final class Rules {
    public static final List<Rule> ALL = List.of(new FlagRule(), new EnumRule(), new MoneyRule(), new FunctionRule());

    private Rules() {}

    /** A program's facts from every rule, under its IR's profile hash; held to its IR (INT-07). */
    public static Intent infer(Program p) {
        List<Fact> facts = new ArrayList<>();
        for (Rule r : ALL) facts.addAll(r.infer(p));
        Intent intent = new Intent(p.header().program(), p.header().profileHash(), facts);
        intent.against(p);
        return intent;
    }
}
