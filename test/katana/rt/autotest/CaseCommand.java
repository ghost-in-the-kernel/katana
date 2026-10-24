package katana.rt.autotest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * What an autotest case's expected check gives the run (TODO 3.1, J-278): the environment variables set before the program, its arguments, a file
 * for its input (`< name`) and a pipe that keeps the first lines (`| head -n N`), read by code from the check's shell text, which is never run
 * (R-045 as amended: the command is the case's own, read mechanically). The shapes it reads: `[export NAME=value]... [NAME=value]... [$COBCRUN_DIRECT] ./prog [words]
 * [< file] [2> file] [| head -n N]`, with quotes, backslash escapes and line continuations. Any other shape (`cat file | prog`, `&&`, `&`, an output
 * redirect, an expansion) is `unread`, with why: the run then has no environment and no arguments, as before.
 */
record CaseCommand(Map<String, String> env, List<String> args, String stdin, int head, String unread) {
    private static final Pattern ASSIGNMENT = Pattern.compile("[A-Za-z_][A-Za-z_0-9]*=.*", Pattern.DOTALL);

    /** A word or an operator of the shell text; `expands` when an unquoted or double-quoted `$` or a backtick (or a glob character) is in it. */
    private record Token(String text, boolean operator, boolean expands) {
        boolean word() {
            return !operator && !expands;
        }

        boolean is(String t) {
            return !operator && text.equals(t);
        }
    }

    /** A shape the reader does not read, and why. */
    private static final class Unread extends RuntimeException {
        Unread(String why) {
            super(why, null, false, false);
        }
    }

    /** A command nothing of which is read: no environment, no arguments, no input, no pipe. */
    static CaseCommand none(String why) {
        return new CaseCommand(Map.of(), List.of(), null, -1, why);
    }

    /** The check's shell text to what it gives the run. */
    // Decision: R-045
    static CaseCommand read(String shell) {
        try {
            List<List<Token>> statements = statements(new Lexer(shell).tokens());
            Map<String, String> env = new LinkedHashMap<>();
            for (List<Token> s : statements.subList(0, statements.size() - 1)) export(s, env);
            return command(statements.get(statements.size() - 1), env);
        } catch (Unread why) {
            return none(why.getMessage());
        }
    }

    /** The tokens split at the newlines into the statements that have any. */
    private static List<List<Token>> statements(List<Token> tokens) {
        List<List<Token>> statements = new ArrayList<>();
        List<Token> current = new ArrayList<>();
        for (Token t : tokens) {
            if (t.operator() && t.text().equals("\n")) {
                if (!current.isEmpty()) statements.add(current);
                current = new ArrayList<>();
            } else {
                current.add(t);
            }
        }
        if (!current.isEmpty()) statements.add(current);
        if (statements.isEmpty()) throw new Unread("no command");
        return statements;
    }

    /** `export NAME=value ...` into the environment. */
    private static void export(List<Token> s, Map<String, String> env) {
        if (!s.get(0).is("export")) throw new Unread("a line before the command that is not an export");
        for (Token t : s.subList(1, s.size())) {
            if (!t.word() || !ASSIGNMENT.matcher(t.text()).matches()) throw new Unread("an export that is not NAME=value");
            assign(t, env);
        }
    }

    private static void assign(Token t, Map<String, String> env) {
        env.put(t.text().substring(0, t.text().indexOf('=')), t.text().substring(t.text().indexOf('=') + 1));
    }

    /** What the command line collects after its program: arguments, the input file and the head pipe. */
    private static final class Tail {
        final List<String> args = new ArrayList<>();
        String stdin;
        int head = -1;
    }

    /** The command line: its assignments, the program, its words, then the redirects and the pipe. */
    private static CaseCommand command(List<Token> s, Map<String, String> env) {
        int i = 0;
        while (i < s.size() && !s.get(i).operator() && ASSIGNMENT.matcher(s.get(i).text()).matches()) {
            if (s.get(i).expands()) throw new Unread("an expansion in the value of " + s.get(i).text().substring(0, s.get(i).text().indexOf('=')));
            assign(s.get(i++), env);
        }
        boolean direct = i < s.size() && s.get(i).is("$COBCRUN_DIRECT");
        if (direct) i++;
        if (i >= s.size() || !s.get(i).word() || !direct && !s.get(i).text().startsWith("./")) throw new Unread("the command starts with `" + (i < s.size() ? s.get(i).text() : "") + "`, not the program");
        Tail tail = new Tail();
        for (i++; i < s.size(); i++) i = tail(s, i, tail);
        if (tail.stdin != null && (tail.stdin.startsWith("/") || tail.stdin.contains(".."))) throw new Unread("an input file outside the case's folder");
        return new CaseCommand(env, List.copyOf(tail.args), tail.stdin, tail.head, null);
    }

    /** One token after the program: an argument, or a redirect or the pipe with what follows it; the index of the last token used. */
    private static int tail(List<Token> s, int i, Tail tail) {
        Token t = s.get(i);
        boolean target = i + 1 < s.size() && s.get(i + 1).word();
        if (!t.operator()) {
            if (t.expands()) throw new Unread("an expansion in an argument");
            tail.args.add(t.text());
        } else if (t.text().equals("<") && target && tail.stdin == null) {
            tail.stdin = s.get(i + 1).text();
            return i + 1;
        } else if (t.text().equals("2>") && target) {
            return i + 1; // stderr to a file: the expectation is of stdout
        } else if (t.text().equals("|") && tail.head < 0 && headPipe(s, i)) {
            tail.head = Integer.parseInt(s.get(i + 3).text());
            return i + 3;
        } else {
            throw new Unread("`" + t.text() + "` is not read");
        }
        return i;
    }

    /** Whether the pipe at `i` is `| head -n N` and the end of the command. */
    private static boolean headPipe(List<Token> s, int i) {
        return s.size() - i == 4 && s.get(i + 1).is("head") && s.get(i + 2).is("-n") && s.get(i + 3).word() && s.get(i + 3).text().matches("\\d{1,6}");
    }

    /**
     * The shell text to words and operators. Single quotes keep everything, double quotes keep all but `\"`, `\\`, `\$` (and a `$` or backtick in them
     * expands), a backslash outside quotes takes the next character literally and a backslash-newline is gone. A quote that does not close is unread.
     */
    private static final class Lexer {
        private final String text;
        private final List<Token> out = new ArrayList<>();
        private final StringBuilder word = new StringBuilder();
        private boolean has, expands;
        private int at;

        Lexer(String text) {
            this.text = text;
        }

        List<Token> tokens() {
            while (at < text.length()) step(text.charAt(at++));
            flush();
            return out;
        }

        private void step(char c) {
            if (c == '\\' && at < text.length()) escape();
            else if (c == '\'') single();
            else if (c == '"') dquote();
            else if (c == ' ' || c == '\t') flush();
            else if ("\n|&;<>".indexOf(c) >= 0) operator(c);
            else add(c);
        }

        private void add(char c) {
            if (c == '$' || c == '`' || "(){}*?~".indexOf(c) >= 0) expands = true;
            word.append(c);
            has = true;
        }

        private void escape() {
            char next = text.charAt(at++);
            if (next == '\n') return;
            word.append(next);
            has = true;
        }

        private void single() {
            int end = text.indexOf('\'', at);
            if (end < 0) throw new Unread("a quote that does not close");
            word.append(text, at, end);
            at = end + 1;
            has = true;
        }

        private void dquote() {
            while (at < text.length() && text.charAt(at) != '"') {
                char d = text.charAt(at++);
                if (d == '\\' && at < text.length() && "\"\\$`\n".indexOf(text.charAt(at)) >= 0) {
                    if (text.charAt(at) != '\n') word.append(text.charAt(at));
                    at++;
                    continue;
                }
                if (d == '$' || d == '`') expands = true;
                word.append(d);
            }
            if (at >= text.length()) throw new Unread("a quote that does not close");
            at++;
            has = true;
        }

        private void flush() {
            if (has) out.add(new Token(word.toString(), false, expands));
            word.setLength(0);
            has = false;
            expands = false;
        }

        /** An operator, `2>` when a lone 2 stands before the `>`, and the doubled forms (`&&`, `||`, `>>`) whole. */
        private void operator(char c) {
            String fd = "";
            if (c == '>' && has && word.toString().equals("2") && !expands) {
                fd = "2";
                has = false;
                word.setLength(0);
            }
            flush();
            String op = String.valueOf(c);
            if ((c == '|' || c == '&' || c == '>') && at < text.length() && text.charAt(at) == c) {
                op += c;
                at++;
            }
            out.add(new Token(fd + op, true, false));
        }
    }
}
