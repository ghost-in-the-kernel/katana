package katana.rt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * The fixed-point and alphanumeric intrinsic functions (LR 6.4 ch. 30, PG pp. 798-799; research §15):
 * integer and mixed functions computed exactly over `Decimal`, as Appendix A's fixed-point rules carry
 * them; alphanumeric ones a new item of DISPLAY usage. The floating-point ones are `Floats`.
 */
public final class Functions {
    private Functions() {}

    private static final LocalDate DAY_ZERO = LocalDate.of(1600, 12, 31);

    /** A function's value as a term: its stated places (`Places`), or its own value's integer digits if more, so a value is never cut by its size. */
    public static Decimal.Term term(Decimal d, int places) {
        BigDecimal b = d.toBigDecimal();
        return Decimal.operand(d, Math.max(places, b.precision() - b.scale()));
    }

    private static Decimal of(BigDecimal b) {
        if (b.scale() < 0) b = b.setScale(0);
        return new Decimal(b.unscaledValue(), b.scale());
    }

    private static Decimal of(long n) {
        return new Decimal(BigInteger.valueOf(n), 0);
    }

    /** INTEGER: "the greatest integer value that is less than or equal to the argument" (LR 6.4 p. 574). */
    public static Decimal integer(Decimal d) {
        return of(d.toBigDecimal().setScale(0, RoundingMode.FLOOR));
    }

    /** INTEGER-PART: the integer part, toward zero. */
    public static Decimal integerPart(Decimal d) {
        return of(d.toBigDecimal().setScale(0, RoundingMode.DOWN));
    }

    /** MOD: "argument-1 - (argument-2 * FUNCTION INTEGER (argument-1 / argument-2))" (LR 6.4 p. 602). */
    public static Decimal mod(Decimal a, Decimal b) {
        BigInteger x = a.toBigDecimal().toBigInteger(), y = b.toBigDecimal().toBigInteger();
        return new Decimal(x.subtract(y.multiply(new BigDecimal(x).divide(new BigDecimal(y), 0, RoundingMode.FLOOR).toBigInteger())), 0);
    }

    /** REM: "argument-1 - (argument-2 * FUNCTION INTEGER-PART (argument-1 / argument-2))" (LR 6.4 p. 626). */
    public static Decimal rem(Decimal a, Decimal b) {
        BigDecimal x = a.toBigDecimal(), y = b.toBigDecimal();
        BigDecimal q = x.divide(y, 0, RoundingMode.DOWN);
        return of(x.subtract(y.multiply(q)));
    }

    /** FACTORIAL of a non-negative integer; FACTORIAL(0) is 1. */
    public static Decimal factorial(Decimal d) {
        BigInteger f = BigInteger.ONE;
        for (int k = 2; k <= d.toBigDecimal().intValue(); k++) f = f.multiply(BigInteger.valueOf(k));
        return new Decimal(f, 0);
    }

    public static Decimal max(Decimal... ds) {
        Decimal m = ds[0];
        for (Decimal d : ds) if (d.toBigDecimal().compareTo(m.toBigDecimal()) > 0) m = d;
        return m;
    }

    public static Decimal min(Decimal... ds) {
        Decimal m = ds[0];
        for (Decimal d : ds) if (d.toBigDecimal().compareTo(m.toBigDecimal()) < 0) m = d;
        return m;
    }

    public static Decimal range(Decimal... ds) {
        return of(max(ds).toBigDecimal().subtract(min(ds).toBigDecimal()));
    }

    public static Decimal sum(Decimal... ds) {
        BigDecimal s = BigDecimal.ZERO;
        for (Decimal d : ds) s = s.add(d.toBigDecimal());
        return of(s);
    }

    /** ORD-MAX and ORD-MIN: the position of the first greatest (least) argument, from 1. */
    public static Decimal ordMax(Decimal... ds) {
        int at = 0;
        for (int k = 1; k < ds.length; k++) if (ds[k].toBigDecimal().compareTo(ds[at].toBigDecimal()) > 0) at = k;
        return of(at + 1);
    }

    public static Decimal ordMin(Decimal... ds) {
        int at = 0;
        for (int k = 1; k < ds.length; k++) if (ds[k].toBigDecimal().compareTo(ds[at].toBigDecimal()) < 0) at = k;
        return of(at + 1);
    }

    /**
     * MAX, MIN, ORD-MAX and ORD-MIN of alphanumeric arguments: "the content of argument-1 having the
     * greatest value", compared "according to the rules for simple conditions", the leftmost of equals, its
     * own size (LR 6.4 p. 592): the position of the chosen one, from 1, `greatest` false for the least.
     */
    public static int textAt(boolean greatest, Collation co, Field... fs) {
        int at = 0;
        for (int k = 1; k < fs.length; k++) {
            int c = Compare.alphanumeric(fs[k], fs[at], co);
            if (greatest ? c > 0 : c < 0) at = k;
        }
        return at;
    }

    public static Field maxText(Collation co, Field... fs) {
        return fs[textAt(true, co, fs)];
    }

    public static Field minText(Collation co, Field... fs) {
        return fs[textAt(false, co, fs)];
    }

    public static Decimal ordMaxText(Collation co, Field... fs) {
        return of(textAt(true, co, fs) + 1);
    }

    public static Decimal ordMinText(Collation co, Field... fs) {
        return of(textAt(false, co, fs) + 1);
    }

    /** ORD: "the ordinal position of its argument in the collating sequence for the program. The lowest ordinal position is 1" (LR 6.4 p. 612). */
    public static Decimal ord(Field f, Collation co) {
        return of(co.weight(f.record().bytes()[f.offset()]) + 1);
    }

    /** CHAR: "a character in the program collating sequence having the ordinal position equal to the value of the argument" (LR 6.4 p. 534). */
    public static Field chr(Decimal n, Collation co, Codepage cp) {
        int want = n.toBigDecimal().intValue() - 1;
        Record r = Record.of(1, cp);
        for (int b = 0; b < 256; b++) {
            if (co.weight((byte) b) == want) {
                r.bytes()[0] = (byte) b;
                break;
            }
        }
        return new Field(r, 0, Attr.text(1, false));
    }

    /** LENGTH: an item's length in bytes (alphanumeric character positions). */
    public static Decimal length(Field f) {
        return of(f.attr().length());
    }

    /** UPPER-CASE, LOWER-CASE and REVERSE: a new item of the argument's length. */
    public static Field upperCase(Field f) {
        return text(f, new String(Literal.bytes(f), f.record().codepage().charset).toUpperCase(java.util.Locale.ROOT));
    }

    public static Field lowerCase(Field f) {
        return text(f, new String(Literal.bytes(f), f.record().codepage().charset).toLowerCase(java.util.Locale.ROOT));
    }

    public static Field reverse(Field f) {
        return text(f, new StringBuilder(new String(Literal.bytes(f), f.record().codepage().charset)).reverse().toString());
    }

    private static Field text(Field like, String s) {
        return Literal.text(s, like.record().codepage());
    }

    /** INTEGER-OF-DATE (YYYYMMDD) and INTEGER-OF-DAY (YYYYDDD): days from 31 December 1600, "integer date 1 represents Monday, 1 January 1601" (LR 6.4 p. 536); 0 for a date that is not one. */
    public static Decimal integerOfDate(Decimal d) {
        long v = d.toBigDecimal().longValue();
        try {
            return of(ChronoUnit.DAYS.between(DAY_ZERO, LocalDate.of((int) (v / 10000), (int) (v / 100 % 100), (int) (v % 100))));
        } catch (java.time.DateTimeException e) {
            return of(0);
        }
    }

    public static Decimal integerOfDay(Decimal d) {
        long v = d.toBigDecimal().longValue();
        try {
            return of(ChronoUnit.DAYS.between(DAY_ZERO, LocalDate.ofYearDay((int) (v / 1000), (int) (v % 1000))));
        } catch (java.time.DateTimeException e) {
            return of(0);
        }
    }

    /** DATE-OF-INTEGER (YYYYMMDD) and DAY-OF-INTEGER (YYYYDDD) of an integer date. */
    public static Decimal dateOfInteger(Decimal d) {
        LocalDate date = DAY_ZERO.plusDays(d.toBigDecimal().longValue());
        return of(date.getYear() * 10000L + date.getMonthValue() * 100L + date.getDayOfMonth());
    }

    public static Decimal dayOfInteger(Decimal d) {
        LocalDate date = DAY_ZERO.plusDays(d.toBigDecimal().longValue());
        return of(date.getYear() * 1000L + date.getDayOfYear());
    }

    /** CURRENT-DATE: 21 characters, YYYYMMDDhhmmsscc and the difference from Greenwich, +hhmm. */
    public static Field currentDate(Codepage cp) {
        java.time.ZonedDateTime now = Now.now();
        int offset = now.getOffset().getTotalSeconds() / 60;
        String s = String.format("%04d%02d%02d%02d%02d%02d%02d%s%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), now.getMinute(),
                now.getSecond(), now.getNano() / 10_000_000, offset < 0 ? "-" : "+", Math.abs(offset) / 60, Math.abs(offset) % 60);
        return Literal.text(s, cp);
    }
}
