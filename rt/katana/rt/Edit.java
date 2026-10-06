package katana.rt;

/**
 * An edited item's editing (the IR's mask, A4): one symbol a character position, the currency sign
 * written `$`, the point `.` and the comma `,` whatever DECIMAL-POINT says, CR and DB two positions
 * each; the program's currency sign and whether DECIMAL-POINT IS COMMA, which the `$`, `.` and `,`
 * print as; and BLANK WHEN ZERO.
 */
public record Edit(String mask, char currency, boolean decimalComma, boolean blankWhenZero) {}
