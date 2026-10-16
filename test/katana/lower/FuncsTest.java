package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.junit.jupiter.api.Test;

/** `Funcs`: a function's arguments as IBM separates and expands them (NIST IF, J-174). */
class FuncsTest {
    private static final String HASH = "sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296";

    private static final Parse.Result PARSED = Parse.text(java.util.Arrays.stream(new String[] {
        "IDENTIFICATION DIVISION.", "PROGRAM-ID. FUNCS.", "DATA DIVISION.", "WORKING-STORAGE SECTION.",
        "01 A PIC 9 VALUE 2.", "01 B PIC 9 VALUE 3.", "01 C PIC 9 VALUE 4.", "01 R PIC 9(3).",
        "01 T.", "   05 ROW OCCURS 2 TIMES.", "      10 CELL PIC 9 OCCURS 3 TIMES.",
        "PROCEDURE DIVISION.", "MAIN-PARA.",
        "    COMPUTE R = FUNCTION MAX(A * B, (C + 1) / 2, 3 + 4).",
        "    COMPUTE R = FUNCTION SUM(CELL(ALL, ALL)).",
        "    STOP RUN."}).map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8));
    private static final Program P = Build.program(PARSED, HASH).program();
    private static final Exprs X = new Exprs(P, new ArrayList<>());

    private static Value.Expression function(int statement) {
        Statement s = P.procedure().paragraphs().stream().filter(g -> "MAIN-PARA".equals(g.name())).findFirst().orElseThrow().statements().get(statement);
        return (Value.Expression) s.operands().stream().map(Statement.Operand::value).filter(v -> Funcs.name(v) != null).findFirst().orElseThrow();
    }

    @Test
    void aSeparatorCommaEndsAnArgumentBeforeAParenthesis() {
        assertEquals(List.of(), PARSED.errors());
        Value.Expression max = function(0);
        assertEquals(3, max.operands().size(), "B, (C + 1) is two arguments, not B subscripted by C + 1 (IF119A): " + max);
        assertEquals("*", ((Value.Expression) max.operands().get(0)).operator());
    }

    @Test
    void allSubscriptsExpandWithTheRightmostFastest() {
        List<String> cells = new ArrayList<>();
        for (Value v : X.funcs.arguments(function(1))) {
            cells.add(((Value.Field) v).subscripts().stream().map(s -> ((Value.Literal) s).payload()).collect(Collectors.joining(",")));
        }
        assertEquals(List.of("1,1", "1,2", "1,3", "2,1", "2,2", "2,3"), cells, "LR 6.4 pp. 501-502: Table-name(1, 1) first, the rightmost ALL incremented first");
    }
}
