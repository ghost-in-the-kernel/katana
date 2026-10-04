# COBOL semantics that break naive COBOL-to-Java translation (spec for the literal-lowering runtime)

Scope note for the report writer. Many primary IBM pages (ibm.com/docs for Enterprise COBOL 6.x Language Reference, Programming Guide, compiler options) returned HTTP 403 to the fetch tooling in this environment, and the IBM PDF mirrors (publibfp.dhe.ibm.com) were blocked at the proxy. Where I could not reach the IBM text directly I cite (a) secondary pages that quote it, (b) the IBM COBOL for Linux Programming Guide PDF (which did fetch and whose Appendix C is the same intermediate-results appendix), (c) GnuCOBOL's libcob source and its dialect configuration files, which encode the IBM behaviors as named flags, and (d) IBM APARs/forums. Each such claim is marked. Everything with no source is in Gaps, not in Findings. Minimal COBOL examples and the "wrong obvious Java" are my own constructions illustrating the cited rule; they are not quoted from sources.

Cross-cutting conclusion up front: the literal Java intermediate cannot be "Java with BigDecimal fields". It must be a byte-image machine: every data item is a view (offset, length, PICTURE/USAGE descriptor) over a shared `byte[]` per 01-level record, arithmetic is decimal with compiler-specified intermediate precision, and control flow is a paragraph dispatcher with PERFORM return-address semantics. This is exactly the architecture of GnuCOBOL's libcob (`cob_field` = data pointer + size + attribute; `cob_decimal` = GMP integer + scale) and of opensourcecobol4j's libcobj, which is a Java port of that design and passes 8,804 of 8,918 NIST COBOL-85 tests — so the design is proven feasible on the JVM.

---

## 1. Numeric semantics

### Takeaway
COBOL numerics are decimal, fixed-point, byte-encoded, and truncating by default; and the result of a COMPUTE depends on compiler-defined intermediate precision rules (IBM "dmax") and on compiler options (TRUNC, NUMPROC, ARITH). A naive `BigDecimal`/`long`/`double` lowering gets the value wrong in each of these dimensions. The exact runtime must (1) keep the byte image as the source of truth, (2) do arithmetic in arbitrary-precision decimal with the compiler's intermediate-truncation rule applied step by step, and (3) store back through a function that applies ROUNDED mode, high-order truncation per PICTURE (or per binary width under TRUNC(BIN)), and SIZE ERROR "keep on overflow".

### Cited Findings

**Representations and storage formats**
- Packed decimal (COMP-3) stores one decimal digit per nibble with a trailing sign nibble; conventional sign nibbles are C (positive), D (negative), F (unsigned/positive). Cobrix documents exactly these conventions and adds a `COMP-3U` extension for packed fields with no sign nibble ("bytes 0x12 0x34 encode the number 1234") — [AbsaOSS/cobrix](https://github.com/AbsaOSS/cobrix)
- Zoned-decimal signed overpunch: in EBCDIC, positive digits 0–9 are encoded as `{`, A–I (zone C) and negative as `}`, J–R (zone D); e.g. "10} is -100" and "45A is 451". ASCII representation "is not standardized" — PL/I-on-ASCII reuses the EBCDIC letters, while "GnuCOBOL and MicroFocus COBOL" use lowercase `p`–`y` for negative 0–9, and other systems use the 0x70 zone variant (`q` = 0x71 = negative 1) — [Wikipedia: Signed overpunch](https://en.wikipedia.org/wiki/Signed_overpunch)
- GnuCOBOL's IBM dialect sets `hostsign: yes`, documented in the config as "Allow Hex 'F' for NUMERIC test of signed PACKED DECIMAL field" (i.e. an F sign nibble in a signed packed item passes `IS NUMERIC` on IBM but not under default GnuCOBOL) — [GnuCOBOL config/default.conf and ibm-strict.conf (OCamlPro mirror)](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/ibm-strict.conf)
- Binary (COMP/COMP-4/BINARY) items are big-endian two's-complement on IBM; GnuCOBOL's IBM dialect uses `binary-size: 2-4-8` (PIC 1–4 digits → 2 bytes, 5–9 → 4 bytes, 10–18 → 8 bytes) and `binary-byteorder: big-endian`; default GnuCOBOL uses `1-2-4-8` — [GnuCOBOL config files](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/default.conf)
- Cobrix assumes big-endian for COMP and COMP-5 by default and introduces `COMP-9` as its little-endian extension — [AbsaOSS/cobrix](https://github.com/AbsaOSS/cobrix)
- IBM COBOL for Linux PG: binary items use two's complement; packed uses nibbles; zoned uses sign nibbles C/D/F; SIGN SEPARATE puts the sign in its own byte — [IBM COBOL for Linux on x86 1.1 Programming Guide (PDF)](https://www.ibm.com/docs/en/SS7FZ2_1.1.0/pdf/pglin.pdf)
- COMP-3 is exact decimal ("This is EXACTLY 0.01. No error."), whereas COMP-1 (hex float) is binary and approximate; summing 0.01 ten thousand times gives ≈100.00393 in COMP-1 vs exactly 100.00 in COMP-3 — [DataField.dev Intermediate COBOL ch. 5](https://datafield.dev/intermediate-cobol/part-01/chapter-05/)

**TRUNC compiler option (binary items)**
- TRUNC(STD): "Truncates to the number of digits in that field's PICTURE": `PIC S9(4) COMP` receiving 12345 becomes 2345. TRUNC(BIN): "Uses the full two-, four-, or eight-byte native binary field" (halfword −32,768..32,767; fullword ±2,147,483,647), so 12345 is kept. TRUNC(OPT): compiler assumes the value fits the PICTURE; "The result can depend on the generated code sequence, so OPT must not be used to obtain a particular out-of-range value." COMP-5 always behaves as BIN regardless of option — [mainframe-forum: COBOL TRUNC compiler option](https://mainframe-forum.blogspot.com/2013/08/cobol-trunc-compiler-option.html)
- IBM manual quote via forum: "Use the TRUNC(OPT) option only if you are sure that the data being moved into the binary areas will not have a value with larger precision than that defined by the PICTURE clause for the binary item. Otherwise, unpredictable results could occur. This truncation is performed in the most efficient manner possible; therefore, the results are dependent on the particular code sequence generated." Worked examples: `A1 PIC S9(4) BINARY VALUE 9999; ADD 9999 TO A1` → storage holds 19998 under both BIN and OPT but DISPLAY shows 19998 (BIN) vs 9998 (OPT); `COMPUTE A1 = LENGTH OF A2 + LENGTH OF A2` with A2 PIC X(8192) → 6384 (OPT) vs 16384 (BIN) — [ibmmainframes.com: TRUNC(BIN) to TRUNC(OPT)](https://ibmmainframes.com/about59717.html)
- Worked TRUNC(STD) vs TRUNC(OPT) divergence: moving 123451 to `S9(02) BINARY`: STD truncates decimally to 51; OPT keeps the rightmost two bytes of hex 0001E23B = E23B = −7621, then stores −21 — [tek-tips: TRUNC(OPT)](https://www.tek-tips.com/threads/trunc-opt.883811/)
- IBM performance webinar: TRUNC(STD) "enforces truncation according to the PICTURE clause after each operation that might exceed it"; TRUNC(OPT) "The compiler can assume that the PICTURE clause is never exceeded"; TRUNC(BIN) truncation follows the binary data type, intermediate results grow, 8.2% slower than OPT; "Data items from Db2 or CICS may not conform to their PIC clause—use COMP-5 for those items instead." NUMPROC(PFD) assumes preferred sign codes C, D, F — [IBM Enterprise COBOL performance tuning webinar (PDF)](https://www.ibm.com/docs/en/SS6SG3_6.5/Performance_Tuning/COBOL_performance_tuning_webinar.pdf)
- DB2: "DB2 uses the full size of the integers" without respecting PICTURE truncation; host variables should be COMP-5 or the program compiled TRUNC(BIN) to avoid truncation surprises — [IBM Db2 12 for z/OS: Host variables in COBOL](https://www.ibm.com/docs/SSEPEK_12.0.0/apsg/src/tpc/db2z_hostvariablecobol.html)
- GnuCOBOL encodes this as `binary-truncate: yes/no` ("Numeric truncation according to ANSI"); IBM dialect has `binary-truncate: no` (i.e. TRUNC(BIN)-like) — [GnuCOBOL config files](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/ibm-strict.conf)
- libcob implementation of store-to-binary: if the decimal value needs more bits than the field has, then with `COB_STORE_KEEP_ON_OVERFLOW` jump to overflow handling (size error, field unchanged); else if `COB_STORE_TRUNC_ON_OVERFLOW` take the value modulo 10^digits (`mpz_tdiv_r (d->value, d->value, cob_mpze10[digits])`), else take modulo 2^bits (`mpz_fdiv_r_2exp (d->value, d->value, bitnum)`); when the value fits the bits but `COB_FIELD_BINARY_TRUNC(f)` is set, it additionally checks `mpz_cmpabs (d->value, cob_mpze10[digits]) >= 0` and truncates to PIC digits — [GnuCOBOL libcob/numeric.c (OCamlPro mirror), lines ~1725–1800](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/libcob/numeric.c)

**ARITH option and digit limits**
- ARITH(COMPAT): max 18 digits; ARITH(EXTEND): max 31 digits, for packed, zoned, numeric-edited items and literals — [mainframe-forum: COBOL ARITH compiler option](https://mainframe-forum.blogspot.com/2013/08/cobol-arith-compiler-option.html?m=0); corroborated by [DataField.dev ch. 5](https://datafield.dev/intermediate-cobol/part-01/chapter-05/)
- GnuCOBOL IBM dialect: `numeric-literal-length: 31 # for ARITH(EXTENDED), otherwise 18` — [GnuCOBOL ibm-strict.conf](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/ibm-strict.conf)

**Intermediate results (the IBM appendix)**
- IBM COBOL for Linux PG Appendix C (same appendix family as Enterprise COBOL PG Appendix A): terminology i (integer digits), d (decimal digits), dmax, op1/op2, ir. Rules as extracted by the fetch tool: addition/subtraction "i = max(i1, i2) + 1; d = max(d1, d2)"; multiplication "i = i1 + i2; d = d1 + d2"; division "i = i1 + d2 + 1; d = dmax" (see caveat next bullet); integer exponent "i = i1 × exponent; d = d1 × exponent"; "When i + d exceeds 30 digits, decimal digits are dropped"; "dmax is the maximum number of decimal digits among all operands and receivers in the statement"; non-integer exponents are evaluated in floating point; "In arithmetic expressions in conditions, dmax is derived from the comparands" — [IBM COBOL for Linux on x86 1.1 Programming Guide (PDF), Appendix C](https://www.ibm.com/docs/en/SS7FZ2_1.1.0/pdf/pglin.pdf). Caveat: the tool paraphrased; the division decimal rule in IBM's text is more precisely "d = max(d1 − d2, dmax)"-style, as the next source states.
- The division decimal-places rule is stated by a GnuCOBOL-vs-IBM conformance bug report as `max(d2 - d1, dmax)` with dmax = max decimal places among operands and receiver, citing "Enterprise COBOL 6.4 Programming Guide SC27-8714-03, Appendix A". Reproducer: `A PIC 9 VALUE 1. B PIC 9 VALUE 3. C PIC 999 VALUE 300. R PIC 999V99. COMPUTE R = A / B * C` → IBM rule and GnuCOBOL `-std=ibm` (arithmetic-osvs) give 099.00 (1/3 held to dmax=2 places = 0.33, ×300 = 99.00); exact arithmetic (GnuCOBOL `-fno-arithmetic-osvs`) gives 099.99 — [squid-protocol/gitgalaxy issue #4287](https://github.com/squid-protocol/gitgalaxy/issues/4287)
- GnuCOBOL exposes this as the dialect flag `arithmetic-osvs` ("Compute intermediate decimal results like IBM OSVS"), `yes` in the IBM dialect, `no` by default — [GnuCOBOL config files](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/default.conf)
- Default GnuCOBOL division: `d1->scale -= d2->scale; shift_decimal (d1, COB_MAX_DIGITS + ((d1->scale < 0) ? -d1->scale : 0)); mpz_tdiv_q (d1->value, d1->value, d2->value);` — i.e. the quotient is computed to COB_MAX_DIGITS (38) extra decimal places, truncated toward zero, not to IBM's dmax — [GnuCOBOL libcob/numeric.c, cob_decimal_div](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/libcob/numeric.c)
- Worked multiplication chain: `PRINCIPAL PIC S9(9)V99 * RATE PIC SV9(6) * DAYS PIC 9(3)` yields a 20-digit intermediate (12 integer + 8 decimal) — [DataField.dev ch. 5](https://datafield.dev/intermediate-cobol/part-01/chapter-05/)
- Even IBM's own runtime has version-dependent intermediate behavior: APAR PH64935 — COBOL 5/6 rounds floating-point intermediate results to 33 decimal places vs 31 in COBOL 4.2, and mis-truncates float→decimal stores; example value "94384759660671573.6376" stored as 1573.6375999999999999 instead of 1573.6376000000000000; affects programs compiled with ARITH(EXTEND) or ARITH(COMPAT) — [IBM APAR PH64935](https://www.ibm.com/support/pages/apar/PH64935)
- Micro Focus has its own `ARITHMETIC"OSVS"` / `"MF"` directives governing intermediate precision (page exists but was robots-blocked, so content not verified) — [Micro Focus Visual COBOL: Intermediate Results](https://www.microfocus.com/documentation/visual-cobol/vc70/EclWin/HRLHLPDF603S005.html)

**ROUNDED, truncation, SIZE ERROR, division by zero**
- Default without ROUNDED: excess low-order decimal digits are truncated. ROUNDED default mode is NEAREST-AWAY-FROM-ZERO (first dropped digit 0–4 truncates, 5–9 rounds away from zero); COBOL 2002+ adds NEAREST-EVEN (33.345→33.34, 33.355→33.36), NEAREST-TOWARD-ZERO, TRUNCATION, TOWARD-GREATER, TOWARD-LESSER, etc. — [DataField.dev ch. 5](https://datafield.dev/intermediate-cobol/part-01/chapter-05/)
- libcob implements rounding by shifting the decimal to scale+1 and adding/subtracting 5 (`mpz_add_ui (d->value, d->value, 5UL)` / `mpz_sub_ui`) for NEAR-AWAY-FROM-ZERO; NEAR-EVEN checks the remainder is exactly 5 followed by zeros and the kept digit is even (`case 5: case 25: case 45: case 65: case 85: return 0;`); TOWARD-GREATER/LESSER add a unit only when the remainder is non-zero and the sign matches — [GnuCOBOL libcob/numeric.c, cob_decimal_do_round](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/libcob/numeric.c)
- SIZE ERROR: with ON SIZE ERROR, the target "remains unmodified"; NOT ON SIZE ERROR does not run; without the phrase, truncation occurs silently ("A $150,000 transaction becomes $0.00 with no error message"). Division by zero triggers the size error condition — [DataField.dev ch. 5](https://datafield.dev/intermediate-cobol/part-01/chapter-05/)
- ILE COBOL (IBM i) definition of size error conditions: result larger than the fixed-point receiver; division by zero; zero to the zero power; zero to a negative power; negative number to a fractional power; floating-point overflow/underflow — [IBM i 7.4 ILE COBOL: The ON SIZE ERROR Phrase](https://www.ibm.com/docs/ssw_ibm_i_74/rzase/cblsizeerr.htm)
- Division by zero without SIZE ERROR is "unpredictable" per the standard and Micro Focus docs; Micro Focus actually sets the result to zero, GnuCOBOL leaves the receiver unchanged and sets an exception; GnuCOBOL maintainers declined to emulate MF — [GnuCOBOL discussion: DIVIDE BY ZERO = ZERO](https://sourceforge.net/p/gnucobol/discussion/cobol/thread/1a32e850/)
- libcob: `cob_decimal_div` on a zero divisor sets `d1->scale = COB_DECIMAL_NAN` and `cob_set_exception (COB_EC_SIZE_ZERO_DIVIDE)`; `cob_decimal_get_field` returns early on NAN (so the receiver is not written) — [GnuCOBOL libcob/numeric.c](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/libcob/numeric.c)
- Enterprise COBOL v5+ enables IEEE divide-by-zero trapping in the LE enclave, so a Java program sharing the enclave crashed with CEE3224S instead of getting ±infinity; IBM's fix is a user handler IGZXDIVZ — [IBM APAR PH13539](https://www.ibm.com/support/pages/apar/PH13539)

**MOVE conversions**
- Numeric MOVE moves algebraic values, aligned by decimal point: moving 3.06 to `PIC 999V9` stores 0030 (i.e. 003.0 — low-order digit truncated). Alphanumeric MOVE: if sender is longer, "truncation occurs on the right"; if shorter, "the extra character positions on the right ... are filled with spaces" — [IBM Enterprise COBOL 6.4 PG: Assigning values to elementary data items (MOVE)](https://www.ibm.com/docs/en/SS6SG3_6.4.0/pg/tasks/tpbeg13.html)
- Group-to-group MOVE moves "as alphanumeric data, character-by-character"; de-editing moves from edited fields strip editing characters — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- A group MOVE "is handled as an alphanumeric transfer rather than treating individual fields within the group separately" — [IBM COBOL for Linux PG (PDF)](https://www.ibm.com/docs/en/SS7FZ2_1.1.0/pdf/pglin.pdf)
- GnuCOBOL IBM dialect sets `move-ibm: yes` ("MOVE like IBM (mvc); left to right, byte by byte") — this matters for overlapping source/destination (REDEFINES) moves — [GnuCOBOL config files](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/default.conf)

**Existing Java libraries**
- JRecord (LGPL-3.0): reads/writes COBOL data files from Java via cb2xml copybook parsing; supports COMP, COMP-3, mainframe zoned decimal, COMP-5, float; EBCDIC via `setFont("cp037")`; ODO (including complex ODO); "No mention is made of COBOL arithmetic semantics—the library appears designed purely for data I/O" — [bmTas/JRecord](https://github.com/bmTas/JRecord)
- Cobrix (Spark data source): copybook-driven EBCDIC decoding with COMP/COMP-3/COMP-5/COMP-9/COMP-3U, REDEFINES, OCCURS in `max_size`/`shift_record`/`pad_record` modes, RDW/BDW variable records, FILLER handling; data-only, no arithmetic — [AbsaOSS/cobrix](https://github.com/AbsaOSS/cobrix)
- opensourcecobol4j: full COBOL→Java compiler with runtime `libcobj.jar` (LGPL-3; translator GPL-3); supports MOVE/COMPUTE, IF/PERFORM/GO TO, DISPLAY/ACCEPT, CALL, sequential/indexed/relative files, SORT, embedded SQL (PostgreSQL), intrinsics; passes 8,804/8,918 NIST COBOL85 tests with zero failures; Java 11+ — [opensourcecobol/opensourcecobol4j](https://github.com/opensourcecobol/opensourcecobol4j)
- GnuCOBOL libcob (C, reference semantics): `cob_decimal` = GMP `mpz_t` value + `int scale`, with `COB_DECIMAL_NAN` for error; `align_decimal`/`shift_decimal`; conversions `cob_decimal_set/get_packed`, `_display`, `_binary`, `_ieee64dec`, `_ieee128dec`; store options `COB_STORE_ROUND`, `COB_STORE_KEEP_ON_OVERFLOW`, truncation — [GnuCOBOL doxygen: libcob/numeric.c](https://gnucobol.sourceforge.io/doxygen/gnucobol-2/numeric_8c.html)
- NACA (Publicitas, 2008–09): 4 MLOC COBOL transcoded to Java with runtime NacaRT+JLib (LGPL, ~153k lines/890 classes) and transcoder NacaTrans (GPL, ~83k lines); handled EBCDIC, "multi-level COBOL data structures in Java independently of UTF encoding (2 bytes per char)", COMMAREAs with multiple REDEFINES masks and COMP-X; the generated Java had "the exact same structure as the original COBOL" — [InfoQ: COBOL to Java Automatic Migration with GPL'ed Tools](https://www.infoq.com/news/2009/07/cobol-to-java/)

### Inferences

Minimal trap catalogue (examples are mine; rules cited above):

1. **Truncation on MOVE/arithmetic store.**
   COBOL: `01 A PIC 9(3). MOVE 12345 TO A.` → A = 345 (high-order truncation).
   Wrong Java: `int a = 12345;` or `BigDecimal a = new BigDecimal(12345)`.
   Required: `a = 12345 mod 10^3`, sign dropped for unsigned PIC. Runtime: `store(decimal, fieldDescriptor, opts)` that does `value.unscaledValue() mod 10^(digits)` after rescaling to the field's scale with truncation toward zero (unless ROUNDED). With TRUNC(BIN)/COMP-5 and a binary field, truncate mod 2^bits instead (libcob code above shows both paths and the KEEP_ON_OVERFLOW path).

2. **Unsigned receiver takes the absolute value.** `01 A PIC 99. MOVE -7 TO A` → 07. libcob: `if (!field_sign && sign == -1) mpz_abs(...)`. Wrong Java: stores −7.

3. **Scale truncation, not rounding.** `01 A PIC 9V9. COMPUTE A = 1.99 + 0` → 1.9. Wrong Java: `setScale(1, HALF_UP)` → 2.0. Required: `setScale(1, DOWN)` unless ROUNDED; ROUNDED default is HALF_UP away from zero (= Java `RoundingMode.HALF_UP`), NEAREST-EVEN = `HALF_EVEN`.

4. **Intermediate precision (the big one).** `COMPUTE R = A / B * C` with R PIC 999V99 → IBM gives 99.00, exact BigDecimal gives 99.99 (cited reproducer). Wrong Java: `a.divide(b, MathContext.DECIMAL128).multiply(c)` → 99.99…. Required: evaluate the expression tree bottom-up; after each binary op compute (i,d) by the IBM table; for division hold d = max(d1−d2, dmax) decimal places (dmax = max decimals over all operands and receivers of the statement, and with ROUNDED typically one more — this last point is in the IBM appendix but I could not quote it: Gap); when i+d > 30 (COMPAT) drop decimal places. Runtime: `IntermediateResult{BigInteger unscaled; int scale; int intDigits}` and an `ArithProfile` object {IBM_COMPAT, IBM_EXTEND, MF_OSVS, EXACT} selected per compile unit, mirroring GnuCOBOL's `arithmetic-osvs` switch.

5. **SIZE ERROR leaves the receiver untouched; division by zero is a SIZE ERROR.** `DIVIDE X BY ZERO-FIELD GIVING R ON SIZE ERROR ...` → R unchanged. Wrong Java: `ArithmeticException` propagates or `R = 0`. Required: compute into a temp; on overflow/zero-divide, skip the store and run the handler; without the phrase, the behavior is dialect-defined (IBM: truncated store / unchanged for zero-divide per libcob; MF: receiver set to zero). Runtime: store returns a status code, never throws.

6. **Binary width vs PICTURE (TRUNC).** `01 A PIC S9(4) COMP. COMPUTE A = 8192 + 8192` → 16384 under BIN, 6384 under STD, implementation-dependent under OPT. Wrong Java: `short a = ...` (wraps at 32767, matches neither STD nor cases where storage holds more). Required: a per-field or per-program `TruncMode` {STD, BIN} applied at store and at load (under STD a DISPLAY of an over-range binary shows the PICTURE-truncated digits — the ibmmainframes example). OPT must be lowered as STD with a warning: the program's result is formally unpredictable, so STD is the only defensible golden.

7. **Sign encodings are data, not metadata.** A `PIC S9(3)` DISPLAY field holding EBCDIC `12}` is −120; the same bytes under an ASCII compiler are not. `IS NUMERIC` on a signed packed field with F sign passes on IBM (hostsign) but not standard. Runtime: the byte codec per USAGE must be parameterized by code page and by `NUMPROC`/hostsign policy; the Java `byte[]` must hold the original encoding (EBCDIC if the files are EBCDIC), with conversion only at DISPLAY/ASCII boundaries.

8. **Float (COMP-1/COMP-2) is IBM hexadecimal floating point, not IEEE** on z/OS; mixed float/fixed expressions switch the whole intermediate to floating point. Java `double` is IEEE binary64; exactness against a z/OS golden is not achievable bit-for-bit without emulating HFP. Treat as a declared non-exactness class (see Gaps).

9. **De-editing.** `01 E PIC ZZ9.99-. MOVE E TO N` where N is numeric: the runtime must parse the edited string back (strip insertion chars, sign from trailing −/CR/DB). Wrong Java: `new BigDecimal(" 12.50-")` throws.

**Java representation choice (inference from the above):** a byte-backed `Field` view over a record `byte[]` plus a `Decimal` scratch type (`BigInteger` unscaled + `int scale`) for arithmetic is the only representation that is exact for all of REDEFINES, group moves, sign-nibble quirks, TRUNC modes and intermediate rules. `BigDecimal` can serve as the scratch type (it is unscaled BigInteger + scale) but the runtime must drive its scale explicitly; `MathContext` precision-based rounding must never be used because COBOL's limits are in digits-of-integer-part + digits-of-fraction, not significant digits. `long` fixed-point is adequate only as an optimization for ≤18-digit fields with proven bounds.

**What the literal lowering must do**
- Lower every data item to a `Field(record, offset, len, Attr{usage, digits, scale, signed, signSeparate, leading, picture})`; never to a Java scalar.
- Lower every arithmetic statement to: load operands as Decimal → evaluate with the selected `ArithProfile` (IBM dmax rules, 30/31-digit cap) → `store(target, ROUNDED mode, SizeErrorPolicy{KEEP|TRUNC|DIALECT}, TruncMode)`; COMPUTE with several receivers evaluates once and stores per receiver using each receiver's digits in dmax.
- Record the compiler options (TRUNC, NUMPROC, ARITH, code page) as a manifest in the Java output; they are part of the program's semantics.
- Emit ON SIZE ERROR / NOT ON SIZE ERROR as branches on the store status.

**What a refactoring pass may later change, and the proof it needs**
- Replace `Field` + Decimal with `BigDecimal`/`long` scalars for a variable only when a whole-program analysis proves: the item is never referenced via a REDEFINES, group MOVE, reference modification, or CALL BY REFERENCE; every value stored is within PICTURE (so truncation never fires) — provable by interval analysis or by asserting the SIZE ERROR status is never set across the golden test corpus plus a static bound; and the item is not read as raw bytes (I/O record, INSPECT).
- Replace step-wise IBM intermediate arithmetic with exact arithmetic only when the proof shows the dmax truncation cannot change the result (e.g. all divisions are exact or the expression has no division/non-integer exponent) — otherwise results differ (99.00 vs 99.99 case).
- Rounding-mode substitutions require the exact mode preserved (HALF_UP vs HALF_EVEN differ).

### Gaps
- I could not fetch IBM's Enterprise COBOL 6.x pages for the intermediate-results tables, TRUNC, NUMPROC, or MOVE rules (HTTP 403 in this environment). The exact IBM table text (including the "i+d > 30 and d < dmax" row behavior, the ROUNDED-adds-one-digit rule, exponentiation with decimal operands, and the floating-point intermediate rule for COMP-1/2 operands) must be verified from SC27-8714 Appendix A (Programming Guide) before the runtime is written. Links to verify: [IBM 6.4 appendix](https://www.ibm.com/docs/en/cobol-zos/6.4.0?topic=appendixes-intermediate-results-arithmetic-precision), [Fixed-point data and intermediate results, 6.3](https://www.ibm.com/docs/en/cobol-zos/6.3.0?topic=precision-fixed-point-data-intermediate-results).
- NUMPROC(PFD/NOPFD/MIG) detailed semantics (handling of non-preferred signs on compare/MOVE/NUMERIC test) not retrievable; only the one-line PFD description from the webinar.
- IBM hexadecimal floating point (COMP-1/2) formats and conversion rules to/from decimal were not researched; this is a likely non-exact class.
- Micro Focus intermediate-result rules (ARITHMETIC directive) were robots-blocked.
- No source found quantifying how often real programs hit the dmax-divergence; only the one reproducer.

---

## 2. Storage semantics

### Takeaway
COBOL data is a flat byte image with multiple simultaneous typed views (REDEFINES, groups, RENAMES, reference modification) and variable layout (ODO). Group MOVEs are byte copies; elementary MOVEs convert. INITIALIZE and VALUE leave specific things untouched. Java objects with one field per item cannot express this; the literal lowering needs record buffers and view descriptors.

### Cited Findings
- OCCURS DEPENDING ON: "The contents of items whose occurrence numbers exceed the value of the object are undefined." As a sending item with the ODO object outside the group, "only that part of the table area that is specified by the object at the start of the operation is used"; as a receiving item with the object inside the group, "the maximum length of the group item is used in the operation"; for unbounded groups "the actual length of the group is used for all references". "Any nonsubordinate item that follows an item described with an OCCURS DEPENDING ON clause is a variably located item" whose location depends on the ODO object's value. The object value must be within integer-1..integer-2 at operation time; behavior is undefined otherwise — [IBM Enterprise COBOL 6.4 LR: OCCURS DEPENDING ON clause](https://www.ibm.com/docs/en/cobol-zos/6.4.0?topic=clause-occurs-depending)
- When the ODO object changes after data has been stored, "the data beyond the new count is lost or reinterpreted" (IBM COBOL for Linux PG, "Effects of change in ODO object value") — [IBM COBOL for Linux PG (PDF)](https://www.ibm.com/docs/en/SS7FZ2_1.1.0/pdf/pglin.pdf)
- GnuCOBOL dialect switches encoding the IBM behaviors: `complex-odo: yes` ("Allow complex OCCURS DEPENDING ON"), `odoslide: yes` ("Adjust position of items following OCCURS DEPENDING"), `indirect-redefines: yes` ("Allow REDEFINES to other than last equal level number"), `relax-level-hierarchy: yes`, `init-justify: yes` in the IBM dialect; defaults are `no` — [GnuCOBOL config files](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/ibm-strict.conf)
- Cobrix supports OCCURS in three modes: `max_size` (default; arrays occupy their maximum space), `shift_record` (variable sizing), `pad_record` — evidence that both fixed-max and sliding interpretations occur in practice — [AbsaOSS/cobrix](https://github.com/AbsaOSS/cobrix)
- INITIALIZE: FILLER items not initialized; REDEFINES source item not reinitialized; alphanumeric → spaces, numeric → zero; all OCCURS occurrences initialized — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- Level 66 RENAMES regroups items "irrespective of how those items are structured"; level 88 condition names evaluate true/false against the parent's value — [Wikipedia: COBOL](https://en.wikipedia.org/wiki/COBOL)
- Group MOVE = alphanumeric byte transfer (see §1 findings) — [IBM COBOL for Linux PG](https://www.ibm.com/docs/en/SS7FZ2_1.1.0/pdf/pglin.pdf); [GnuCOBOL PG](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- IBM-style MOVE is left-to-right byte-by-byte (`move-ibm: yes`, "like IBM (mvc)") — observable when source and destination overlap through REDEFINES — [GnuCOBOL default.conf comments](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/default.conf)
- NACA had to manage "multi-level COBOL data structures in Java independently of UTF encoding (2 bytes per char)" and COMMAREAs with multiple data masks (REDEFINES) — [InfoQ on NACA](https://www.infoq.com/news/2009/07/cobol-to-java/)
- Binary/packed storage conventions and SIGN SEPARATE as in §1 — [IBM COBOL for Linux PG](https://www.ibm.com/docs/en/SS7FZ2_1.1.0/pdf/pglin.pdf)

### Inferences

Trap catalogue (examples mine):

1. **REDEFINES = aliasing.**
   `01 W. 05 N PIC 9(4). 05 R REDEFINES N PIC X(4). MOVE 12 TO N. DISPLAY R` → "0012" (EBCDIC F0F0F1F2 or ASCII 30303031 32 bytes). Wrong Java: two independent fields `int n; String r;`. Required: both are views of the same 4 bytes. Runtime: `Record{byte[] data}`; `Field` views; writing N re-encodes into the shared bytes; reading R decodes from them.

2. **Group MOVE is a byte copy, elementary MOVE converts.**
   `MOVE GROUP-A TO GROUP-B` where B's subordinate N is COMP-3 and A's is DISPLAY: bytes copied verbatim, N now holds garbage packed data; no error. Wrong Java: field-by-field conversion (which is what MOVE CORRESPONDING does, by name, elementary items only). Runtime: `moveGroup(src, dst)` = `arraycopy` + space-pad/truncate right; `moveElementary` dispatches on (srcAttr, dstAttr).

3. **Overlapping moves follow MVC semantics.** `MOVE X(1:9) TO X(2:9)` on IBM propagates byte 1 across (left-to-right, one byte at a time) — the "fill" idiom. Wrong Java: `System.arraycopy` (memmove semantics, non-propagating). Runtime: byte loop left-to-right when ranges overlap and the dialect is IBM.

4. **ODO: length is a function of a live variable.**
   `01 T. 05 CNT PIC 99. 05 E OCCURS 1 TO 50 DEPENDING ON CNT PIC X(10). 05 TAIL PIC X(5).`
   `MOVE 3 TO CNT. MOVE "abcde" TO TAIL. MOVE 5 TO CNT. DISPLAY TAIL` → TAIL now addresses bytes 52..56 (variably located), which hold whatever was there (undefined per IBM; concretely the previous E(4) bytes). Wrong Java: `List<E>` with `TAIL` as a separate field. Required: offset(TAIL) = 2 + 10*CNT computed at each reference. Group length as sender = 2+10*CNT(+5); as receiver with object inside = maximum. Runtime: `Field.offset()` becomes a closure over the record when any ODO precedes it (`odoslide`); group length has sender/receiver variants.

5. **INITIALIZE is not "zero everything".** FILLER and REDEFINES-ing items are skipped; VALUE clauses are NOT reapplied by INITIALIZE (unless WITH FILLER / TO VALUE on newer compilers) — the first half is cited; the VALUE part is a Gap (standard knowledge, unverified here). Wrong Java: `Arrays.fill(data, 0)`.

6. **VALUE applies once (program load / first CALL), not on each entry** — see §6.

7. **Level 88 with THRU/multiple values and level 66** are pure predicates/views over bytes: `88 OK VALUE 1 THRU 5 9.` → `v>=1 && v<=5 || v==9` on the decoded parent; RENAMES is an offset/length view across sibling boundaries. Both trivially exact once bytes are the truth.

8. **SYNC / slack bytes** change offsets inside a group and hence REDEFINES overlaps and record lengths; the IBM alignment rules (halfword/fullword for COMP by size) must be computed at lowering time. Rule text not retrieved: Gap.

**What the literal lowering must do**
- One `byte[]` per 01/77 (and per LINKAGE 01 pointing into the caller's buffer); every item is a `Field` view; REDEFINES/RENAMES/reference modification/subscripting produce derived views, ODO produces dynamically computed views.
- Separate `moveGroup` (bytes) and `moveElementary` (conversion); MOVE CORRESPONDING expands at compile time to elementary moves by matching names.
- Encode EBCDIC-vs-ASCII as a property of the record store; keep the original encoding in the bytes.
- Compute layout (offsets, SYNC slack, ODO max/actual lengths) with the IBM rules and emit it as a table the golden tests can check against the mainframe's compiler listing (data map).

**What a refactoring pass may later change, and the proof it needs**
- Collapse a 01 record into a Java class with typed fields only when no REDEFINES/RENAMES/ref-mod/ODO/group-MOVE/raw-I/O touches it (alias analysis over the whole program, including CALL BY REFERENCE escapes).
- Turn an ODO table into a `List` only if no variably located item follows it and no group MOVE spans it.
- Proof: the data-map (offset table) is unchanged, and a byte-level diff of every record after each golden test step is empty.

### Gaps
- IBM SYNC alignment rules and slack-byte computation text were not retrieved.
- Whether INITIALIZE honours VALUE (default no; `TO VALUE` / `WITH FILLER` phrases) not verified from a primary source here.
- IBM's exact text on MOVE with ODO receivers ("maximum length") retrieved; the ODO "sender uses current length" rule is cited, but the interaction with reference modification was not.

---

## 3. Control flow

### Takeaway
PERFORM is not a call: it sets a continuation at the end of a paragraph range and falls into the paragraphs in textual order. Overlapping active ranges, GO TO out of a range, recursive PERFORM, and ALTER are undefined or self-modifying. The published goto-elimination results (Erosa–Hendren) are correct but unusable for maintainable output; industry practice is a hybrid of pattern matching, an explicit program-counter switch, and tolerated residual gotos. The literal lowering should therefore be a paragraph dispatcher (program-counter loop) that reproduces IBM's PERFORM-return behavior exactly, and structured code is a refactoring goal with proof obligations.

### Cited Findings
- IBM LR: "Control is always returned to the statement following the PERFORM statement." Nested ranges: "an active PERFORM statement whose execution point begins within the range of performed procedures of another active PERFORM statement must not allow control to pass through the exit point of the other active PERFORM statement." Entering the range otherwise: "When control passes to the sequence of procedures by means other than a PERFORM statement, control passes through the exit point to the next executable statement, as if no PERFORM statement referred to these procedures." Recursion: "A PERFORM statement must not cause itself to be executed. This constitutes a recursive PERFORM, which can cause unpredictable results." — [IBM Enterprise COBOL 6.4 LR: Basic PERFORM statement](https://www.ibm.com/docs/en/SS6SG3_6.4.0/lr/ref/rlpsperf1.html)
- Classic implementation uses "continuation addresses" rather than a stack; with overlapping `PERFORM sub-1 THRU sub-n` the two PERFORMs "may interfere which each other's management of the continuation address in several ways"; example output "1 2 3" instead of expected "1 2 3 4 3". COBOL 2002: if control passes through the exit point of an earlier incomplete PERFORM "the behavior is undefined". "PERFORM cannot be used to write recursive code." ALTER ("ALTER X TO PROCEED TO Y") was deleted in the 2002 standard — [Wikipedia: COBOL](https://en.wikipedia.org/wiki/COBOL)
- GnuCOBOL exposes the alternative implementation as `perform-osvs` ("If yes, the exit point of any currently executing perform is recognized if reached"), `yes` in the IBM dialect — i.e. IBM-compatible behavior is that reaching the end of ANY active PERFORM's range returns, not only the innermost — [GnuCOBOL config files](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/default.conf)
- GnuCOBOL (its own dialect) supports recursive PERFORM with per-invocation context, EXIT PARAGRAPH/SECTION/PERFORM, ALTER; STOP RUN "terminates the entire program, returning control to the operating system", GOBACK returns to the caller (or OS if main) — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- Goto elimination on an 8 MLOC COBOL banking system (531,351 GOTOs) migrated to Java: pattern-based removed 21% (max nesting +1, mean +13%); Böhm–Jacopini top-level (explicit program counter + switch over top-level labels) removed 79% (max nesting +2, mean +46%, 22 s); Erosa–Hendren goto-movement removed 100% but with max nesting +134/+179 levels and mean nesting +232%/+381%, 117–144 hours, judged "mission impossible" to maintain; JGoto (placeholder calls restored to bytecode jumps after javac) preserved structure with 0% removal. Adopted: patterns + Böhm–Jacopini TL + JGoto bridge — [Ceccato et al., "Goto Elimination Strategies in the Migration of Legacy Code to Java", CSMR 2008](https://profs.scienze.univr.it/~ceccato/papers/2008/csmr2008.pdf)
- Erosa & Hendren, "Taming control flow: a structured approach to eliminating goto statements" (ICCL 1994) is the canonical goto-movement algorithm the above evaluates — [Erosa & Hendren PDF (Tufts mirror)](https://www.cs.tufts.edu/comp/150FP/archive/laurie-hendren/taming.pdf)
- In LLM-based translation, a `PERFORM VARYING ... UNTIL` was converted to a `while` with inverted condition and failed tests — [Hans & Kumar, "Automated Testing of COBOL to Java Transformation" (arXiv 2504.10548)](https://arxiv.org/pdf/2504.10548)

### Inferences

Trap catalogue (examples mine):

1. **Fall-through between paragraphs.**
   ```
   PERFORM P1.           *> not THRU
   ...
   P1. ADD 1 TO X.
   P2. ADD 10 TO X.
   ```
   A `PERFORM P1` executes only P1. But `GO TO P1` from elsewhere executes P1 then P2 then onward (no PERFORM active → "passes through the exit point ... as if no PERFORM statement referred to these procedures"). Wrong Java: `p1();` as a method and `p2();` as another method with no fall-through path. Required: paragraphs are labels in one flat sequence; PERFORM installs a return at the end of the named range.

2. **PERFORM THRU with internal GO TO (the "EXIT paragraph" idiom)** `PERFORM A THRU A-EXIT` with `GO TO A-EXIT` inside A is well-defined; `GO TO Z` (outside the range) leaves the PERFORM active forever: when Z's flow later reaches A-EXIT's end by falling through, control returns to the statement after the PERFORM (perform-osvs) — a surprising jump. Wrong Java: `return` from a method. Required: dispatcher semantics.

3. **Overlapping ranges**: `PERFORM A THRU C` where B contains `PERFORM B THRU C` → "1 2 3" vs "1 2 3 4 3" per the Wikipedia example; result depends on implementation (IBM: single continuation slot per paragraph exit). The literal lowering must pick one model and it must be IBM's: a per-paragraph-exit continuation slot (not a stack) when targeting z/OS fidelity; GnuCOBOL's `perform-osvs` flag is the executable spec of this.

4. **ALTER** rewrites a GO TO's target at runtime → the lowering gives every `GO TO` in an ALTERable paragraph a mutable target variable.

5. **PERFORM VARYING ... AFTER** has specific init/test/increment order (outer varied, inner reset to FROM on each outer step; UNTIL tested before each iteration unless WITH TEST AFTER). Easy to invert (the arXiv case). Exact lowering: desugar to the dispatcher loop with the standard's order, not to a `for`.

6. **STOP RUN vs GOBACK vs EXIT PROGRAM**: STOP RUN ends the run unit (closes files, returns RETURN-CODE to OS) even from a subprogram; GOBACK returns to the caller; a main program's GOBACK ends the run unit. Wrong Java: `return` for all three.

7. **Declaratives (USE AFTER ERROR)** are implicitly invoked on I/O status failures when no FILE STATUS/AT END handles them; they are a per-file hook table consulted by the I/O runtime.

**The known algorithm for structuring** (from the cited literature): (a) pattern rewrite of local gotos (loops/break/continue); (b) Böhm–Jacopini "program counter + switch" for the remainder; (c) Erosa–Hendren goto-movement only where nesting stays bounded; (d) tolerate residual gotos (JGoto) rather than explode nesting. For a *deterministic compiler with a golden*, (b) IS the literal intermediate: `for(;;) switch(pc) { case P1: ...; pc = P2; break; ... }` with a PERFORM continuation table `returnAt[paragraphExit] = resumePc`. Structuring is then a provable refactoring on top.

**What the literal lowering must do**
- Emit one method per COBOL program containing a dispatcher loop over paragraph/section ids; `GO TO` = `pc = X; continue`; `PERFORM A THRU B` = push/record `exit[B] = next`; at the end of each paragraph check the IBM rule (perform-osvs: any active range ending here returns); PERFORM counts/UNTIL/VARYING desugared to pc-level loops in the standard's evaluation order.
- Model ALTER as mutable goto targets; model declaratives as runtime hooks; model STOP RUN/GOBACK as distinct runtime exits.
- Give every paragraph a stable id so the golden trace (sequence of paragraph entries) can be compared against a mainframe trace (e.g. from a debug/coverage tool).

**What a refactoring pass may later change, and the proof it needs**
- Convert a paragraph set into a Java method only if: it is entered solely by PERFORM (never by GO TO or fall-through from above), every exit is through its range end (no GO TO out), no ALTER targets it, and it is not recursive — all are CFG properties checkable on the lowered dispatcher.
- Convert PERFORM UNTIL/VARYING into `while/for` only when the paragraph body has no GO TO leaving the range and does not modify the loop variable in ways that change the standard's evaluation order semantics.
- Proof: CFG bisimulation between dispatcher and structured code (same paragraph-entry trace on all golden inputs) and absence of the "undefined" patterns (overlapping active ranges, recursive PERFORM), which should be reported as findings rather than silently preserved.

### Gaps
- I could not retrieve the exact IBM text on what Enterprise COBOL actually does for the undefined overlapping-range cases (it is implementation behavior, not documented); GnuCOBOL's `perform-osvs` flag is the best available executable spec.
- No source retrieved on SECTION-level PERFORM end-of-range rules (sections' last paragraph) or on EXIT PARAGRAPH on IBM; GnuCOBOL's description is used.
- Ramshaw's goto-elimination paper was not fetched.

---

## 4. Strings and characters

### Takeaway
Comparison and collation are byte-and-code-page semantics (EBCDIC on z/OS), not Java `String` semantics; INSPECT/STRING/UNSTRING have position-based algorithms with specific termination and no padding. Every string operation in the literal lowering must operate on bytes in the program's code page.

### Cited Findings
- HIGH-VALUES "represents the highest character in the collating sequence (hexadecimal FF)"; comparisons pad the shorter operand with spaces; collating follows the system order (ASCII on GnuCOBOL) — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- STRING: POINTER is 1-based and advances; ON OVERFLOW when pointer <1 or > receiver length; "No automatic padding"; partial result remains on overflow. UNSTRING: DELIMITED BY ALL merges consecutive delimiters; COUNT IN, TALLYING, POINTER; OVERFLOW when receivers exhausted before source consumed. Alphanumeric comparison space-pads the shorter operand; NUMERIC class test on signed items checks the sign nibble validity — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- INSPECT REPLACING processes left to right; ALL/LEADING/FIRST/CHARACTERS; BEFORE/AFTER INITIAL delimit the inspected region; multiple operand pairs are applied per position in the coded order and previously replaced positions are not re-scanned; worked example: input `"COBOL LANGUAGE IS A HIGH LEVEL LANGUAGE"` with `REPLACING ALL SPACES BY "#" AFTER "LEVEL"  "LANGUAGE" BY "&&&&&&&&" BEFORE "HIGH"  "IS" BY "**"` → `"COBOL &&&&&&&& ** A HIGH LEVEL#LANGUAGE###########"` — [mainframestechhelp: INSPECT REPLACING](https://www.mainframestechhelp.com/tutorials/cobol/inspect-replacing-statement.htm)
- Sign overpunch encodings differ by code page and compiler (EBCDIC `{`/A–I vs `}`/J–R; ASCII `p`–`y` in GnuCOBOL/MF) — [Wikipedia: Signed overpunch](https://en.wikipedia.org/wiki/Signed_overpunch)
- NUMERIC test with F sign on signed packed is IBM-specific (`hostsign`) — [GnuCOBOL ibm-strict.conf](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/ibm-strict.conf)
- JRecord and Cobrix support EBCDIC code pages (cp037, cp1047, cp870, cp875, cp1025, cp1388, cp1364, cp300, cp838) for decoding; regional overpunch depends on the code page — [AbsaOSS/cobrix](https://github.com/AbsaOSS/cobrix); [bmTas/JRecord](https://github.com/bmTas/JRecord)

### Inferences

Trap catalogue (examples mine):

1. **Collation.** `IF "a" < "A"` is TRUE in EBCDIC (lowercase 0x81 < uppercase 0xC1) and FALSE in ASCII. `IF "1" < "A"`: EBCDIC digits (0xF1) sort AFTER letters (0xC1); ASCII the reverse. Wrong Java: `String.compareTo`. Required: unsigned byte comparison in the program's code page (or via PROGRAM COLLATING SEQUENCE table). Runtime: `compareAlnum(byte[],off,len, byte[],off,len, collatingTable)` with space padding in that code page (EBCDIC space = 0x40).
2. **HIGH-VALUES = 0xFF bytes**, not `"￿"` and not a valid character in any charset — Java `String` cannot hold it losslessly if decoded. Must stay bytes.
3. **Comparison padding** uses spaces, so `"ABC" = "ABC  "` is TRUE; but numeric-vs-alphanumeric comparison rules differ (numeric compared as unsigned integer DISPLAY). Wrong Java: `equals`.
4. **INSPECT** is a per-position algorithm with "first matching operand wins and the position is consumed"; a regex/`replace` chain gives different results when operands overlap (`REPLACING ALL "AB" BY "XY" "B" BY "Z"` on "ABB" → "XYZ", not "XYZ" by accident but "AZZ" if `B` is applied first). Runtime: literal implementation of the standard's loop.
5. **STRING does not space-fill** the remainder of the receiver; `MOVE` does. Wrong Java: building a `String` and assigning.
6. **Reference modification** `X(I:L)` is a byte view; out-of-range is undefined (IBM, without SSRANGE) — the runtime must at least match the compiler's checked/unchecked option.
7. **NUMERIC class test** depends on USAGE, sign presence, and NUMPROC/hostsign — a function of raw bytes, not of a parsed value.
8. **National (PIC N)/DBCS** are 2-byte-per-char fields with their own padding (DBCS space 0x4040); not researched in depth (Gap).

**What the literal lowering must do**
- All alphanumeric data remain `byte[]` in the source code page; comparisons, INSPECT, STRING/UNSTRING, figurative constants, and padding are implemented over bytes with a `Collation` object (native EBCDIC/ASCII or an ALPHABET table).
- Conversion to Java `String` only at DISPLAY/ACCEPT and external boundaries, through a declared code page (cp037/cp1047 or ASCII), and never for computation.

**What a refactoring pass may later change, and the proof it needs**
- Promote a field to `String` only if it never holds non-character bytes (HIGH/LOW-VALUES, binary via REDEFINES), is never compared with ordering operators (or the collation is proven equivalent for its value domain), and is never a group/ref-mod target. Proof: value-domain analysis plus byte-identical results on the golden corpus.

### Gaps
- IBM LR INSPECT general rules, STRING/UNSTRING exact rules, and PROGRAM COLLATING SEQUENCE text were 403; the GnuCOBOL guide and a tutorial page stand in.
- DBCS/national semantics (PIC N, NSYMBOL, DBCS shift codes in EBCDIC) not researched.

---

## 5. Files and I/O

### Takeaway
COBOL file verbs are a record-oriented API with status codes rather than exceptions, keyed (VSAM KSDS-like) access with START/READ NEXT and alternate keys with duplicates, fixed vs variable records, and SORT with user procedures. The literal lowering must map every verb to a runtime that returns a 2-character FILE STATUS and preserves the record area byte image; the physical store (VSAM, sequential dataset, DB) is behind an interface chosen per deployment.

### Cited Findings
- Organizations: sequential, indexed (C-ISAM/VSAM implementations), relative, plus non-standard line sequential — [Wikipedia: COBOL](https://en.wikipedia.org/wiki/COBOL)
- FILE STATUS is two characters; first digit is the class (0 success, 1 at end, 2 invalid key, 3 permanent error, 4 logic error, 9 implementor); e.g. 00 success, 02 duplicate key, 04 record length mismatch, 10 EOF, 21–24 key errors, 30/34/35/39 I/O errors, 41–49 open/close/access-state logic errors — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- SORT: INPUT PROCEDURE runs before sorting and RELEASEs records to the sort file; OUTPUT PROCEDURE runs after and RETURNs them — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- ACCEPT FROM DATE gives YYMMDD; FROM DAY gives YYYYDDD (the guide's rendering; the standard's DAY is YYDDD and DAY YYYYDDD — see Gaps); FROM DAY-OF-WEEK gives 1–7 Monday–Sunday — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- Variable-length mainframe records carry RDW/BDW prefixes (Cobrix exposes `rdw_adjustment`, endianness options, and `record_length_field`) — [AbsaOSS/cobrix](https://github.com/AbsaOSS/cobrix)
- opensourcecobol4j implements sequential, indexed, and relative files and SORT in its Java runtime (indexed via an embedded store) and passed the NIST suite's file tests — [opensourcecobol/opensourcecobol4j](https://github.com/opensourcecobol/opensourcecobol4j)
- GnuCOBOL IBM dialect: `assign-clause: external` (ASSIGN names are DD names resolved externally) vs default `dynamic`; `filename-mapping: yes` — [GnuCOBOL ibm-strict.conf](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/ibm-strict.conf)
- JRecord provides readers/writers for fixed-width, length-based, and text COBOL files with copybook layouts — [bmTas/JRecord](https://github.com/bmTas/JRecord)

### Inferences

Trap catalogue (examples mine):

1. **Status codes, not exceptions.** `READ F AT END ...` with no FILE STATUS and no declarative → abend on error; with FILE STATUS the program continues and must test it. Wrong Java: `IOException`. Runtime: every verb returns status, sets the FILE STATUS field (as bytes), and runs AT END/INVALID KEY/declaratives per the standard's precedence.
2. **The record area is shared storage.** After `READ`, the 01 under FD *is* the buffer (and under IBM, multiple 01s under one FD REDEFINE each other); `WRITE` after `MOVE ... TO record` writes those bytes. Wrong Java: returning a new object.
3. **Keyed access state machine.** `START F KEY >= K` then `READ F NEXT` iterates in key order; alternate keys WITH DUPLICATES return status 02 and iterate duplicates in insertion order; `READ F` (random) by primary key; `REWRITE` must follow a successful READ in sequential mode; `DELETE` likewise. The runtime needs a B-tree-like `IndexedFile` (opensourcecobol4j shows this is doable in Java).
4. **Fixed vs variable records**: `RECORD CONTAINS 10 TO 200 DEPENDING ON L` — length written = L; read sets L. RDW handling when the data physically comes from z/OS.
5. **SORT with procedures** is a coroutine: the lowering runs the input procedure to completion (RELEASE appends), sorts with COBOL collation on key byte images, then runs the output procedure where RETURN pops. Keys compare as bytes/decimal per USAGE, not as Java strings.
6. **DISPLAY/ACCEPT** formatting (sign, decimal point, leading zeros, DISPLAY of COMP under TRUNC(STD) shown PICTURE-truncated) must follow the dialect; ACCEPT FROM DATE/DAY/TIME formats are fixed-width digit strings.
7. **COPY ... REPLACING and nested COPY** are preprocessor-time; the lowering must start from the expanded source but keep a map back to copybooks for the data-map golden. REPLACING operates on text words (pseudo-text `==:PFX:==`) — a token-level replace, not string replace.

**What the literal lowering must do**
- Emit a `FileRuntime` call per verb returning status; share the record buffer; implement declaratives and AT END/INVALID KEY precedence; implement sequential/relative/indexed semantics (START/READ NEXT/PREV, alternates, duplicates) in a pluggable store; SORT/MERGE with byte-image keys and the program collating sequence.
- Resolve ASSIGN/DD names through an environment table (the JCL dependency made explicit).

**What a refactoring pass may later change, and the proof it needs**
- Swap the indexed-file store for a database only if a proof shows every observable (status codes, iteration order under START/READ NEXT, duplicate handling, record images) is preserved — this is behavioral, best proven by a differential test harness of file traces.
- Replace status-code handling with exceptions only where the program's own handling is total and never reads the status later.

### Gaps
- IBM VSAM-specific status codes (e.g., 9x), OPEN mode matrix (INPUT/OUTPUT/I-O/EXTEND by organization), and exact ACCEPT FROM DAY (YYDDD vs YYYYDDD) were not confirmed from IBM text.
- RELEASE/RETURN restrictions and MERGE rules from the standard were not fetched.
- COPY REPLACING token rules (pseudo-text matching) not fetched.

---

## 6. Program structure: CALL, LINKAGE, state

### Takeaway
A COBOL subprogram is a stateful module whose WORKING-STORAGE persists across CALLs unless IS INITIAL or CANCEL; BY REFERENCE passes an address into the caller's byte image (so the callee can write the caller's storage, and sizes need not match); BY CONTENT copies; RETURN-CODE is a global register. The literal lowering must preserve all of this with one long-lived instance per program and buffer-slice passing.

### Cited Findings
- Programs without INITIAL: "Data persists between calls"; with IS INITIAL: "All Working-Storage data items are reset to their initial values (VALUE clauses or default values) each time the program is called" — [mainframemaster: COBOL INITIAL clause](https://www.mainframemaster.com/tutorials/cobol/quick-reference/initial)
- CALL BY REFERENCE (default) "Passes address; called program accesses original data"; BY CONTENT "Passes copy; modifications don't affect caller's data"; BY VALUE "numeric items only"; CANCEL "terminates subprogram, releasing resources and resetting IS INITIAL state" — [GnuCOBOL Programmer's Guide](https://gnucobol.sourceforge.io/HTML/gnucobpg.html)
- GnuCOBOL IBM dialect: `sticky-linkage: yes` ("If yes, LINKAGE SECTION items remain allocated between invocations") — i.e. on IBM a callee invoked with fewer parameters can still address LINKAGE items from the previous call; default `no` — [GnuCOBOL config files](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/default.conf)
- CICS programs receive DFHCOMMAREA via LINKAGE with the caller's COMMAREA storage; NACA's runtime had to emulate COMMAREA passing with multiple REDEFINES masks — [InfoQ on NACA](https://www.infoq.com/news/2009/07/cobol-to-java/)
- CICS translation differences surfaced as a distinct error class in LLM translation (Java must catch a `CicsConditionException` rather than generic exceptions) — [Quality Evaluation of COBOL to Java Code Transformation (arXiv 2507.23356)](https://arxiv.org/html/2507.23356v1)

### Inferences

Trap catalogue (examples mine):

1. **Persistent WORKING-STORAGE.** `CALL "SUB"` twice where SUB does `ADD 1 TO CNT` (VALUE 0) → 1 then 2. Wrong Java: `new Sub().run()` per call → 1, 1. Required: one `Sub` instance per run unit; VALUE applied at first CALL (or at load); `IS INITIAL` → reinitialize on each CALL; `CANCEL "SUB"` → drop instance so next CALL re-initializes (and reloads for dynamic CALL).
2. **BY REFERENCE is a byte window.** Caller `01 BIG PIC X(100). CALL "SUB" USING BIG.` Callee `LINKAGE 01 L PIC X(10). ... MOVE ALL "X" TO L(1:50)`? — not allowed by L's length, but `01 L. 05 A PIC X(10). 05 B PIC X(90)` reinterprets caller's 100 bytes under a different layout. Wrong Java: pass a `String`/object copy or a typed DTO. Required: pass `(byte[] data, int offset)` views; the callee's LINKAGE 01 is a view over the caller's record. Also: a callee can write past its declared LINKAGE length into adjacent caller storage (undefined but real).
3. **BY CONTENT** makes a temporary copy of the caller's item *with the caller's length*; BY VALUE is for binary scalars (and the callee's LINKAGE must match). Easy to get right once views exist.
4. **GLOBAL/EXTERNAL**: EXTERNAL items are run-unit-wide named shared buffers (same name → same bytes across programs); GLOBAL is visible to nested programs. Runtime: a run-unit registry of named `byte[]`.
5. **RETURN-CODE** is a run-unit special register set by callee (`MOVE 8 TO RETURN-CODE`) and read by caller after CALL; STOP RUN returns it to the OS/JCL. Wrong Java: method return value (RETURN-CODE survives beyond the call and is also the job step condition code).
6. **Static vs dynamic CALL**: `CALL "LIT"` static-linked vs `CALL ID` dynamic; CANCEL only affects dynamically called programs (IBM). Lowering: a `ProgramRegistry.lookup(name)` with cancel semantics.
7. **Nested programs** share GLOBAL data of containers and are only callable from within; lowering as inner classes over the container's buffers.

**What the literal lowering must do**
- One Java class per program with a singleton instance per run unit, `firstCall` flag for VALUE application, `cancel()` support; `call(byte[][] argViews)` where each LINKAGE 01 is bound to a `(record, offset)` view; EXTERNAL buffers from a run-unit registry; RETURN-CODE as a run-unit register; preserve sticky LINKAGE for the IBM dialect.

**What a refactoring pass may later change, and the proof it needs**
- Make a subprogram stateless (static method, fresh locals) only if every WORKING-STORAGE item is written before read on every path from entry (so persistence is unobservable) and no EXTERNAL/GLOBAL sharing exists — dataflow proof.
- Replace BY REFERENCE buffer views with typed parameters only when the callee's LINKAGE layout equals the caller's argument layout at every call site and the callee never writes outside the parameter's bytes.

### Gaps
- IBM CALL statement page (static/dynamic rules, BY VALUE restrictions, RETURN-CODE rules) was 403; GnuCOBOL and a tutorial stand in.
- IBM rules on LINKAGE items addressed when not passed (sticky linkage) are inferred from GnuCOBOL's flag name/comment, not IBM text.

---

## 7. Environment-specific: CICS, DB2/SQL, IMS, JCL, compiler options

### Takeaway
EXEC CICS/EXEC SQL are preprocessor expansions into CALLs against a runtime API, with results reported in data areas (EIB/EIBRESP, SQLCA/SQLCODE), not exceptions. A translator must keep that call-plus-status shape against a facade; the binary host-variable/COMMAREA data must be byte-exact; and compiler options (TRUNC, NUMPROC, ARITH, code page) are inputs to the semantics and must be captured per compile unit.

### Cited Findings
- DB2 host variables: allowed types are COMP-1/COMP-2 (float), `PIC S9(4)/S9(9)` BINARY/COMP-4/COMP-5 (integers), `PIC S9(i)V9(d)` COMP-3 (decimal), `PIC X(m)` DISPLAY (char), level-49 length+data for VARCHAR ("You cannot use an intervening REDEFINE at level 49"); "DB2 uses the full size of the integers" so use COMP-5 or TRUNC(BIN); the precompiler replaces `SQL TYPE IS` with generated COBOL structures (e.g., VARBINARY → level-49 length+data) — [IBM Db2 12 for z/OS: Host variables in COBOL](https://www.ibm.com/docs/SSEPEK_12.0.0/apsg/src/tpc/db2z_hostvariablecobol.html)
- "Java does not have a direct equivalent for these error or non-zero SQL codes. Instead, Java uses exceptions to handle any SQL errors," which blocked precise path replication in COBOL-vs-Java equivalence testing — [arXiv 2504.10548](https://arxiv.org/pdf/2504.10548)
- EXEC CICS translation is a distinct semantic gap; the evaluation flagged "any Java middleware call that does not align with a COBOL call" as a hallucination and required `CicsConditionException` handling — [arXiv 2507.23356](https://arxiv.org/html/2507.23356v1)
- NACA's NacaRT emulates "functions of a classical transactional monitor like CICS from IBM" and handles COMMAREA with REDEFINES masks and COMP-X — [InfoQ on NACA](https://www.infoq.com/news/2009/07/cobol-to-java/)
- IBM webinar: CICS/DB2 can place values in binary items that do not conform to PICTURE; use COMP-5 — [IBM performance webinar PDF](https://www.ibm.com/docs/en/SS6SG3_6.5/Performance_Tuning/COBOL_performance_tuning_webinar.pdf)
- Compiler option effects documented above: TRUNC (§1), NUMPROC(PFD) assumption of preferred signs (§1), ARITH 18 vs 31 digits (§1). GnuCOBOL's IBM dialect file enumerates further semantic switches (perform-osvs, arithmetic-osvs, move-ibm, hostsign, sticky-linkage, odoslide, complex-odo, indirect-redefines, binary-size 2-4-8, binary-truncate no, init-justify, assign-clause external) — [GnuCOBOL ibm-strict.conf](https://raw.githubusercontent.com/OCamlPro/gnucobol/gcos4gnucobol-3.x/config/ibm-strict.conf)
- opensourcecobol4j supports embedded SQL only for PostgreSQL; no CICS — [opensourcecobol/opensourcecobol4j](https://github.com/opensourcecobol/opensourcecobol4j)

### Inferences

1. **EXEC SQL**: the lowering should treat the statement as a call `sql.exec(stmtId, hostVarViews[], indicatorViews[])` that fills SQLCA bytes (SQLCODE, SQLSTATE, SQLERRD) and never throws; `WHENEVER` clauses become generated `IF SQLCODE ...` branches (that is what the precompiler does). Host variables are byte views (COMP-3 decimals, COMP-5 ints, level-49 VARCHAR); the JDBC adapter converts at the boundary, with explicit attention to DB2 writing full-width integers (TRUNC(BIN) behavior on those items regardless of program option). Cursors are runtime objects keyed by name with OPEN/FETCH/CLOSE state.
2. **EXEC CICS**: lower to `cics.<verb>(args..., RESP view, RESP2 view)` against a facade interface (the shape of IBM's own translator output: a CALL to DFHEI1 with an argument list, EIB in LINKAGE — not retrievable here, see Gaps). HANDLE CONDITION → a per-program condition→label table consulted by the facade (a GO TO into the dispatcher, which the §3 design makes natural). COMMAREA = byte view shared across LINK/XCTL/RETURN TRANSID.
3. **IMS DL/I**: `CALL 'CBLTDLI' USING fn, pcb, io-area, ssa...` is already a CALL; the PCB status code is a 2-byte field — fits the same facade pattern. (Not researched further: Gap.)
4. **JCL**: DD names (ASSIGN ... external), PARM (→ LINKAGE on the main program), condition codes (RETURN-CODE), SYSIN/SYSOUT must be surfaced as an explicit environment manifest consumed by the Java run unit.
5. **Compiler options as semantics**: TRUNC, NUMPROC, ARITH, code page (CODEPAGE), SSRANGE (bounds checks), ZWB (zero-when-blank comparison), NUMCHECK, DYNAM/NODYNAM, APOST/QUOTE (literal delimiter; affects parsing only), RMODE, and OPT level (which for TRUNC(OPT) changes results!) must be recorded per compile unit and drive the runtime profile. If a program's options are unknown, the lowering cannot be exact and should say so.

**What the literal lowering must do**
- Treat every EXEC block as a call to a facade with status-in-data; keep EIB/SQLCA/PCB as byte records; record compile options in a manifest; make DD-name resolution and PARM explicit; flag TRUNC(OPT) as "semantics depend on code generation" and lower as STD with a finding.

**What a refactoring pass may later change, and the proof it needs**
- Replace facade calls with native JDBC/JPA or Java EE equivalents only after proving every status path the program tests is reproduced (SQLCODE 100 vs exception, +/− codes, warnings in SQLWARN) — a differential test against the recorded SQLCA traces, plus a check that no untested status leads to a different control path (dead-status analysis).

### Gaps
- IBM CICS translator pages (what EXEC CICS expands to; DFHEIBLK/DFHCOMMAREA insertion; RESP/NOHANDLE semantics) were 403; the design above is inferred from the standard shape of translated programs and from NACA's runtime description.
- IMS DL/I semantics were not researched.
- A definitive list of semantics-changing Enterprise COBOL options (NUMPROC details, ZWB, SSRANGE, NUMCHECK, INTDATE, DATEPROC) was not retrieved from IBM.

---

## Overall gaps and verification plan for the report writer
- Primary IBM texts (Enterprise COBOL 6.x LR/PG and the Appendix A intermediate-results tables) must be re-fetched from an unblocked network before the runtime spec is frozen; everything in §1 on dmax/30-digit rules is corroborated by the IBM COBOL for Linux PG and a conformance bug report, but the exact table wording is not quoted here.
- GnuCOBOL's `config/ibm-strict.conf` plus `libcob/numeric.c` are the most useful *executable* specification found: each IBM quirk is a named flag with a comment, and the store/round/divide code is directly portable to Java `BigInteger`. opensourcecobol4j is proof that this design runs on the JVM and passes NIST.
- The classes of behavior that cannot be made exact by construction and should be declared as such in the feasibility study: TRUNC(OPT) programs; programs relying on undefined PERFORM-range overlap or recursive PERFORM; division by zero without SIZE ERROR; ODO data beyond the current count; IBM hexadecimal float (COMP-1/2) bit patterns; reads of uninitialized storage (no VALUE) — each should become a compile-time finding rather than a silent lowering.
