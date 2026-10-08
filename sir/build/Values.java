package katana.sir.build;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Locale;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import katana.sir.types.Value;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Tree to `Value`: an identifier to a Field (or a ConditionName when its name is an 88's), a literal
 * or figurative constant as written, a file name to a File (a containing program's GLOBAL file to a
 * Name), a mnemonic, register, GLOBAL item, CICS translator name or unresolvable word to a Name, and a condition or an arithmetic expression to Expressions shaped as the grammar is:
 * each rule with an operator (`+`, `AND`, `=` ...) is one, its operator the rule's own words, a rule
 * with one operand and no operator is that operand.
 */
final class Values {
    private final Parse.Result parse;
    private final Names names;
    private final List<String> findings;
    final Set<String> mnemonics = new HashSet<>(), files = new HashSet<>(), outerFiles = new HashSet<>();
    final List<Names> outer = new ArrayList<>();
    boolean cics;
    private final Expressions expressions = new Expressions(this);

    Values(Parse.Result parse, Names names, List<String> findings) {
        this.parse = parse;
        this.names = names;
        this.findings = findings;
    }

    /** Whether a node is one this class turns into a Value whole. */
    static boolean isValue(ParseTree node) {
        return VALUES.contains(node.getClass());
    }

    /** The rules read as one value, whole. */
    static final java.util.Set<Class<?>> VALUES = java.util.Set.of(CobolParser.IdentifierContext.class, CobolParser.LiteralContext.class,
            CobolParser.QualifiedDataNameContext.class, CobolParser.FileNameContext.class, CobolParser.ConditionContext.class,
            CobolParser.ArithmeticExpressionContext.class, CobolParser.ConditionNameReferenceContext.class, CobolParser.MnemonicNameContext.class,
            CobolParser.IntegerLiteralContext.class, CobolParser.IndexNameContext.class, CobolParser.FigurativeConstantContext.class,
            CobolParser.AssignmentNameContext.class);

    /** Any node this class knows to its Value; a rule it does not know, as an expression of its children. */
    Value of(ParseTree node) {
        return switch (node) {
            case CobolParser.IdentifierContext i -> identifier(i);
            case CobolParser.TableCallContext t -> table(t);
            case CobolParser.QualifiedDataNameContext q -> field(q, List.of(), null, null);
            case CobolParser.LiteralContext l -> literal(l);
            case CobolParser.FigurativeConstantContext f -> figurative(f);
            case CobolParser.IntegerLiteralContext n -> Literals.of(n.getText());
            case CobolParser.FileNameContext f -> file(upper(f.getText()));
            case CobolParser.ConditionNameReferenceContext c -> conditionName(c);
            case CobolParser.IndexNameContext x -> named(x.getText(), List.of(), x);
            case CobolParser.DataNameContext d -> named(d.getText(), List.of(), d);
            case CobolParser.MnemonicNameContext m -> new Value.Name("mnemonic", upper(m.getText()));
            case CobolParser.AssignmentNameContext a -> new Value.Name("assignment", upper(a.getText()));
            case CobolParser.ArithmeticExpressionContext a -> expressions.arithmetic(a);
            case CobolParser.ConditionContext c -> expressions.top(c);
            case ParserRuleContext r -> expression(r);
            default -> new Value.Name("word", node.getText());
        };
    }

    /** A file of this program, or a GLOBAL one of a program it is nested in. */
    Value file(String name) {
        if (files.contains(name) || !outerFiles.contains(name)) return new Value.File(name);
        return new Value.Name("global file", name);
    }

    private Value identifier(CobolParser.IdentifierContext i) {
        if (i.tableCall() != null) return table(i.tableCall());
        if (i.functionCall() != null) {
            List<Value> arguments = new ArrayList<>();
            for (var a : i.functionCall().argument()) arguments.add(argument(a));
            Value call = new Value.Expression("FUNCTION " + upper(i.functionCall().functionName().getText()), arguments);
            var refmod = i.functionCall().referenceModifier();
            if (refmod == null) return call;
            List<Value> slice = new ArrayList<>(List.of(call, of(refmod.characterPosition().arithmeticExpression())));
            if (refmod.length() != null) slice.add(of(refmod.length().arithmeticExpression()));
            return new Value.Expression("slice", slice);
        }
        CobolParser.SpecialRegisterContext s = i.specialRegister();
        if (s.identifier() != null) return new Value.Expression(upper(s.getChild(0).getText()) + " OF", List.of(identifier(s.identifier())));
        return new Value.Name("register", upper(s.getText()));
    }

    private Value argument(CobolParser.ArgumentContext a) {
        ParseTree first = a.getChild(0);
        return first instanceof ParserRuleContext ? of(first) : new Value.Name("keyword", upper(first.getText()));
    }

    /** A table call: subscripts and reference modification; DFHRESP(x) and DFHVALUE(x) are CICS names. */
    private Value table(CobolParser.TableCallContext t) {
        String head = upper(t.qualifiedDataName().getChild(0).getChild(0).getText());
        if (cics && (head.equals("DFHRESP") || head.equals("DFHVALUE"))) return new Value.Name("cics", upper(t.getText()));
        List<Value> subscripts = new ArrayList<>();
        for (var s : t.subscript()) subscripts.addAll(subscript(s));
        Value start = null, length = null;
        if (t.referenceModifier() != null) {
            start = of(t.referenceModifier().characterPosition().arithmeticExpression());
            if (t.referenceModifier().length() != null) length = of(t.referenceModifier().length().arithmeticExpression());
        }
        return field(t.qualifiedDataName(), subscripts, start, length);
    }

    /**
     * A subscript: ALL, a value, or a name with a relative offset (`I + 1`, `I - 1`) as that sum; the
     * operator "must be preceded and followed by a space" (LR 6.4 p. 74), so a name followed by a literal
     * alone is two subscripts (`W-1 10`, `I -1`), which the grammar lets look like an offset.
     */
    private List<Value> subscript(CobolParser.SubscriptContext s) {
        if (s.ALL() != null) return List.of(new Value.Name("keyword", "ALL"));
        if (s.arithmeticExpression() != null || s.getChildCount() == 1) return List.of(of(s.arithmeticExpression() != null ? s.arithmeticExpression() : s.getChild(0)));
        Value name = of(s.getChild(0));
        String offset = s.integerLiteral().getText();
        if (s.PLUSCHAR() == null && s.MINUSCHAR() == null) return List.of(name, Literals.of(offset));
        return List.of(new Value.Expression(s.MINUSCHAR() != null ? "-" : "+", List.of(name, Literals.of(offset))));
    }

    /** A data name with its qualifiers, to a Field, to a ConditionName when it names an 88, else a Name and a finding. */
    private Value field(CobolParser.QualifiedDataNameContext q, List<Value> subscripts, Value start, Value length) {
        CobolParser.QualifiedDataNameFormat1Context f = q.qualifiedDataNameFormat1();
        if (q.qualifiedDataNameFormat4() != null) return new Value.Expression("LINAGE-COUNTER OF", List.of(file(upper(q.qualifiedDataNameFormat4().inFile().fileName().getText()))));
        if (f == null) {
            findings.add(upper(q.getText()) + " at " + Origins.at(parse, q) + " names no data item");
            return new Value.Name("unresolved", upper(q.getText()));
        }
        String name = upper(f.getChild(0).getText());
        List<String> qualifiers = new ArrayList<>();
        String file = f.inFile() == null ? null : upper(f.inFile().fileName().getText());
        for (var in : f.qualifiedInData()) qualifiers.add(upper(in.inData() != null ? in.inData().dataName().getText() : in.inTable().tableCall().qualifiedDataName().getChild(0).getChild(0).getText()));
        int item = names.find(name, qualifiers, file, Origins.at(parse, q).toString());
        if (item != 0) return new Value.Field(item, subscripts, start, length);
        int owner = names.condition(name, qualifiers, file);
        if (owner != 0) return new Value.ConditionName(owner, name, subscripts);
        if (cics && (name.equals("DFHRESP") || name.equals("DFHVALUE"))) return new Value.Name("cics", upper(q.getParent().getText()));
        return named(name, qualifiers, q);
    }

    /**
     * A name that is no item here: a SPECIAL-NAMES mnemonic, a GLOBAL item of a program this one is
     * nested in, a CICS translator name (an EIB field, DFHRESP, DFHVALUE) in a program with EXEC CICS,
     * else unresolved and a finding.
     */
    private Value named(String text, List<String> qualifiers, ParserRuleContext where) {
        String name = upper(text);
        int item = names.find(name, qualifiers, null, Origins.at(parse, where).toString());
        if (item != 0) return new Value.Field(item, List.of(), null, null);
        if (mnemonics.contains(name)) return new Value.Name("mnemonic", name);
        for (Names o : outer) if (o.find(name, qualifiers, null, "") != 0) return new Value.Name("global", name);
        if (cics && (name.startsWith("EIB") || name.equals("DFHRESP") || name.equals("DFHVALUE"))) return new Value.Name("cics", name);
        findings.add(upper(text) + " at " + Origins.at(parse, where) + " names no item");
        return new Value.Name("unresolved", upper(text));
    }

    /** An 88 by name and qualifiers; else an item, a switch, a GLOBAL condition, or unresolved and a finding. */
    private Value conditionName(CobolParser.ConditionNameReferenceContext c) {
        String name = upper(c.conditionName().getText());
        List<String> qualifiers = new ArrayList<>();
        for (var in : c.inData()) qualifiers.add(upper(in.dataName().getText()));
        String file = c.inFile() == null ? null : upper(c.inFile().fileName().getText());
        int owner = names.condition(name, qualifiers, file);
        List<Value> subscripts = new ArrayList<>();
        for (var s : c.conditionNameSubscriptReference()) for (var sub : s.subscript()) subscripts.addAll(subscript(sub));
        if (owner != 0) return new Value.ConditionName(owner, name, subscripts);
        int item = names.find(name, qualifiers, file, Origins.at(parse, c).toString());
        if (item != 0) return new Value.Field(item, subscripts, null, null);
        if (!c.inMnemonic().isEmpty() || mnemonics.contains(name)) return new Value.Name("switch", name);
        for (Names o : outer) if (o.condition(name, qualifiers, file) != 0) return new Value.Name("global", name);
        findings.add(name + " at " + Origins.at(parse, c) + " names no condition");
        return new Value.Name("unresolved", name);
    }

    /** A literal, or the figurative constant it is (ZERO is also a numeric literal's word). */
    static Value literal(CobolParser.LiteralContext l) {
        if (l.figurativeConstant() != null) return figurative(l.figurativeConstant());
        if (l.numericLiteral() != null && l.numericLiteral().ZERO() != null) return new Value.Figurative("ZERO", null);
        return Literals.of(l.getText());
    }

    /** A figurative constant by its singular name; ALL with its literal, or ALL and a figurative's name (ALL QUOTE). */
    private static Value figurative(CobolParser.FigurativeConstantContext f) {
        if (f.ALL() != null && f.literal().figurativeConstant() != null) return new Value.Figurative("ALL " + ((Value.Figurative) figurative(f.literal().figurativeConstant())).name(), null);
        if (f.ALL() != null) return new Value.Figurative("ALL", Literals.of(f.literal().getText()));
        String word = upper(f.getText());
        return new Value.Figurative(switch (word) {
            case "SPACES" -> "SPACE";
            case "ZEROS", "ZEROES" -> "ZERO";
            case "HIGH-VALUES" -> "HIGH-VALUE";
            case "LOW-VALUES" -> "LOW-VALUE";
            case "QUOTES" -> "QUOTE";
            case "NULLS" -> "NULL";
            default -> word;
        }, null);
    }

    /** A rule shaped by the grammar: its own words the operator, its rules the operands; one operand and no words is that operand. */
    private Value expression(ParserRuleContext r) {
        StringBuilder operator = new StringBuilder();
        List<Value> operands = new ArrayList<>();
        for (ParseTree child : r.children == null ? List.<ParseTree>of() : r.children) {
            if (child instanceof TerminalNode t) operator.append(operator.isEmpty() ? "" : " ").append(upper(t.getText()));
            else if (child instanceof CobolParser.RelationalOperatorContext op) operator.append(operator.isEmpty() ? "" : " ").append(upper(DataMap.text(op)));
            else operands.add(of(child));
        }
        if (operator.isEmpty() && operands.size() == 1) return operands.get(0);
        return new Value.Expression(operator.isEmpty() ? r.getClass().getSimpleName().replace("Context", "") : operator.toString(), operands);
    }

    static String upper(String s) {
        return s.toUpperCase(Locale.ROOT);
    }
}
