package katana.sir.build;

import katana.front.Mapped;
import katana.front.Parse;
import katana.sir.types.Origin;
import org.antlr.v4.runtime.ParserRuleContext;

/** The front end's origin of a node as the IR's own Origin: the one place the IR's origins are converted. */
final class Origins {
    private Origins() {}

    static Origin at(Parse.Result parse, ParserRuleContext node) {
        return of(parse.origin(node));
    }

    static Origin at(Parse.Result parse, org.antlr.v4.runtime.Token token) {
        return of(parse.origin(token));
    }

    private static Origin of(Mapped.Origin o) {
        return new Origin(o.file(), o.line(), o.column(), Origin.Kind.valueOf(o.kind().name()));
    }
}
