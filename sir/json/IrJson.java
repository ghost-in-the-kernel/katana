package katana.sir.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Semantic IR as JSON and back (docs/ROADMAP.md 2.5): a record is an object of its components in
 * declaration order, an enum its name, a list an array, a member of a sealed interface an object whose
 * first key `is` is its simple name (no component of the IR is named `is`). Writing is deterministic, so the same IR is the same text, and
 * reading what was written gives an equal IR.
 */
public final class IrJson {
    private IrJson() {}

    /** The value as JSON text. */
    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    /** One value: null, a string, an int, a boolean, an enum, a list or a record. */
    private static void write(Object value, StringBuilder out) {
        switch (value) {
            case null -> out.append("null");
            case String s -> string(s, out);
            case Integer i -> out.append(i);
            case Boolean b -> out.append(b);
            case Enum<?> e -> string(e.name(), out);
            case List<?> list -> {
                out.append('[');
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) out.append(',');
                    write(list.get(i), out);
                }
                out.append(']');
            }
            case Record r -> record(r, out);
            default -> throw new IllegalArgumentException("not IR: " + value.getClass());
        }
    }

    /** A record as an object: `is` first for a member of a sealed interface, then each component. */
    private static void record(Record r, StringBuilder out) {
        out.append('{');
        boolean first = true;
        if (r.getClass().getInterfaces().length > 0 && r.getClass().getInterfaces()[0].isSealed()) {
            string("is", out);
            out.append(':');
            string(r.getClass().getSimpleName(), out);
            first = false;
        }
        for (RecordComponent c : r.getClass().getRecordComponents()) {
            if (!first) out.append(',');
            first = false;
            string(c.getName(), out);
            out.append(':');
            try {
                write(c.getAccessor().invoke(r), out);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        out.append('}');
    }

    /** A string quoted, with quotes, backslashes, line ends and characters outside printable ASCII escaped. */
    private static void string(String s, StringBuilder out) {
        out.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                default -> {
                    if (c < 0x20 || c > 0x7e) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        out.append('"');
    }

    /** JSON text as a value of a type (a record, enum, sealed interface, String, int, boolean or List of these). */
    public static <T> T read(String json, Class<T> type) {
        Parser p = new Parser(json);
        Object tree = p.value();
        return type.cast(convert(tree, type));
    }

    /** A parsed JSON value to the type asked for, through lists, enums, sealed interfaces and record constructors. */
    private static Object convert(Object tree, Type type) {
        if (tree == null) return null;
        if (type instanceof ParameterizedType p && p.getRawType() == List.class) {
            List<Object> list = new ArrayList<>();
            for (Object o : (List<?>) tree) list.add(convert(o, p.getActualTypeArguments()[0]));
            return List.copyOf(list);
        }
        Class<?> c = (Class<?>) type;
        if (c == String.class) return tree;
        if (c == int.class || c == Integer.class) return ((Number) tree).intValue();
        if (c == boolean.class || c == Boolean.class) return tree;
        if (c.isEnum()) return enumValue(c, (String) tree);
        Map<?, ?> object = (Map<?, ?>) tree;
        if (c.isSealed() && c.isInterface()) {
            for (Class<?> member : c.getPermittedSubclasses()) if (member.getSimpleName().equals(object.get("is"))) return convert(tree, member);
            throw new IllegalArgumentException("no " + c.getSimpleName() + " that is " + object.get("is"));
        }
        RecordComponent[] components = c.getRecordComponents();
        Object[] args = new Object[components.length];
        Class<?>[] types = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            args[i] = convert(object.get(components[i].getName()), components[i].getGenericType());
            types[i] = components[i].getType();
        }
        try {
            Constructor<?> constructor = c.getDeclaredConstructor(types);
            return constructor.newInstance(args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause() instanceof RuntimeException r ? r : new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumValue(Class<?> c, String name) {
        return Enum.valueOf((Class) c, name);
    }

    /** JSON to maps, lists, strings, numbers, booleans and null. */
    private static final class Parser {
        private final String s;
        private int at;

        Parser(String s) {
            this.s = s;
        }

        /** The next JSON value: an object, an array, a string, null, true, false or an integer. */
        Object value() {
            char c = s.charAt(at);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return text();
            if (s.startsWith("null", at)) {
                at += 4;
                return null;
            }
            if (s.startsWith("true", at) || s.startsWith("false", at)) {
                boolean b = s.startsWith("true", at);
                at += b ? 4 : 5;
                return b;
            }
            int start = at;
            while (at < s.length() && (Character.isDigit(s.charAt(at)) || s.charAt(at) == '-')) at++;
            return Long.parseLong(s.substring(start, at));
        }

        private Map<String, Object> object() {
            Map<String, Object> map = new LinkedHashMap<>();
            at++;
            while (s.charAt(at) != '}') {
                String key = text();
                at++;
                map.put(key, value());
                if (s.charAt(at) == ',') at++;
            }
            at++;
            return map;
        }

        private List<Object> array() {
            List<Object> list = new ArrayList<>();
            at++;
            while (s.charAt(at) != ']') {
                list.add(value());
                if (s.charAt(at) == ',') at++;
            }
            at++;
            return list;
        }

        /** A quoted string, its escapes undone. */
        private String text() {
            StringBuilder out = new StringBuilder();
            at++;
            while (s.charAt(at) != '"') {
                char c = s.charAt(at++);
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                char e = s.charAt(at++);
                switch (e) {
                    case 'n' -> out.append('\n');
                    case 'u' -> {
                        out.append((char) Integer.parseInt(s.substring(at, at + 4), 16));
                        at += 4;
                    }
                    default -> out.append(e);
                }
            }
            at++;
            return out.toString();
        }
    }
}
