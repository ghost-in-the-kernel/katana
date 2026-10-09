# IBM Enterprise COBOL dialect options: sourced facts

Collected 2026-10-04 for Katana's dialect profile. Each fact is tagged **[verbatim]** (quoted
from the named source), **[paraphrase]** (the source's content, reworded by the digest tool),
or **[unverified]** (recollection of the Programming Guide, not reached this session; treat as
a hypothesis to confirm against the manual).

**Access note.** The Programming Guide (PG) and Language Reference (LR) PDFs are reachable but the
fetch tool digests only their first ~30 pages (the contents), so the PG option chapter and
Appendix A were not read on 2026-10-04 (Appendix A was read in full on 2026-10-05, §4, S19); `ibm.com/docs` HTML is 403 except a few indexed pages. Primary sources
read in full: 6.4 Performance Tuning Guide, V4R2 Performance Tuning paper, 6.4 Customization
Guide, the 6.2/6.5 PG option table, 6.2/6.3 What's New, IBM APARs, an IBM webinar deck.

## Defaults (primary)

[verbatim, PG 6.5 SC27-8714-04, "COBOL compiler options" table, S1] rows: `ARITH ARITH(COMPAT)
AR(C | E)`; `CODEPAGE CODEPAGE(1140) CP(ccsid)`; `NUMPROC NUMPROC(NOPFD) None`; `TRUNC TRUNC(STD)
None`; `ZONEDATA ZONEDATA(PFD) ZD(PFD) | ZD(MIG) | ZD(NOPFD)`; `INVDATA NOINVDATA`; `NUMCHECK
NONUMCHECK`. The 6.2 table (S2) agrees for ARITH/CODEPAGE/NUMPROC/TRUNC and has ZONECHECK, not
ZONEDATA. [paraphrase, Customization Guide 6.4 SC27-8712-03 Table 1, S3] IBM-supplied defaults:
TRUNC=STD, NUMPROC=NOPFD, ARITH=COMPAT, CODEPAGE=1140, INVDATA=NO, NUMCHECK=NO. COLLSEQ does not
appear in any 6.x option table read; treat it as absent from Enterprise COBOL 6.x.

## 1. TRUNC

[verbatim, V4R2 Performance Tuning paper, pp. 9-10, S4]
- STD: "the final intermediate result of an arithmetic expression, or the sending field in the
  MOVE statement, is truncated to the number of digits in the PICTURE clause of the binary (COMP)
  receiving field" (base-10 truncation).
- OPT: "the compiler assumes that the data conforms to the PICTURE and USAGE specifications and
  manipulates the result based on the size of the field in storage (halfword, fullword or
  doubleword)."
- BIN: "all binary (COMP) sending fields are treated as either halfword, fullword, or doubleword
  values ... code is generated to truncate all binary receiving fields to the corresponding
  halfword, fullword, or doubleword boundary" (base-2 truncation).
- COMP-5: "If TRUNC(BIN) processing is only needed for a few binary data items, consider defining
  them as COMP-5 and using TRUNC(OPT)."

[verbatim, PG (version not stated, quoted by Bill Woodger on ibmmainframes.com, S5); the same
wording is in the 6.x PG TRUNC(OPT) section] "Use the TRUNC(OPT) option only if you are sure
that the data being moved into the binary areas will not have a value with larger precision than
that defined by the PICTURE clause for the binary item. Otherwise, unpredictable results could
occur. This truncation is performed in the most efficient manner possible; therefore, the results
are dependent on the particular code sequence generated. It is not possible to predict the
truncation without seeing the code sequence generated for a particular statement."

[verbatim, Performance Tuning Guide 6.4 / 6.3 HTML, S6] "Changing the TRUNC option can lead to
different behaviour if you have invalid data. Use the TRUNC(OPT) option only if you are sure that
the data being moved into the binary areas will not have a value with larger precision than that
defined by the PICTURE clause for the binary item. Otherwise, unpredictable results could occur."
Recommends "TRUNC(OPT), as this allows the compiler the most freedom"; "The default is TRUNC(STD)".

[paraphrase, IBM webinar "Performance Tuning with Enterprise COBOL", Mike Chase, slide 45, S7]
COMP obeys the PIC clause; COMP-5 truncates by the underlying binary size; prefer COMP, use COMP-5
only for external data from DB2/IMS.

[paraphrase, APAR PI90560, S8] 6.2 under OPT(1|2)+TRUNC(OPT) "inadvertently assumed that binary
data items conform to their PIC clause"; confirms out-of-range values under OPT are undefined.

[unverified] The PG TRUNC(BIN) text also says: binary receiving items behave as COMP-5; values up
to the full capacity of the halfword/fullword/doubleword are stored; recommended for data from
"CICS and DB2" when it may not conform to the PICTURE; and that TRUNC has no effect on COMP-5.

## 2. NUMPROC

[verbatim, V4R2 Performance Tuning paper, pp. 5-6, S4]
- PFD: "generates significantly more efficient code for numeric comparisons ... the compiler
  assumes that the data has the correct sign and bypasses this sign fix-up processing."
- NOPFD: "sign fix-up processing is done for all references to these numeric data items"
  (COMP-3 and DISPLAY numeric).
- MIG: "sign fix-up processing is done only for receiving fields (and not for sending fields) of
  arithmetic and MOVE statements."

[verbatim, Performance Tuning Guide 6.4 p. 22, S6] "When your numeric data exactly conforms to
the IBM system standards as detailed in NUMPROC in the Enterprise COBOL for z/OS Programming
Guide, use NUMPROC(PFD)". "NUMPROC(PFD) improves performance as the compiler no longer has to
generate code to correct input sign configurations. This is particularly important when your
application contains unsigned internal decimal and zoned decimal data".

Preferred signs. [paraphrase, IBM webinar slide 26, S7] with NUMPROC(PFD) the compiler assumes
all data uses preferred sign codes: C positive, D negative, F unsigned; [verbatim] "if C is the
only positive sign code, values can be compared using a bitwise comparison." Zoned equivalents
[unverified]: zone nibble C/D in the low-order byte for signed (SIGN TRAILING, no SEPARATE), F
for unsigned.

Class test. [verbatim, APAR PM02897, S9] NUMPROC(PFD) "enforces the rule that respects the
picture definition" (C or D for signed fields, F for unsigned) on a NUMERIC test; NUMPROC(NOPFD)
or NUMPROC(MIG) "passes numeric test regardless of picture definition" (any valid sign A-F
accepted).

MIG in 6.x. [verbatim, IBM community thread, Mike Chase (IBM), 2021-04-14, S10] "NUMPROC(MIG)
had two main behaviours in COBOL 4: guaranteeing that all comparisons of USAGE DISPLAY data items
were numeric, and avoiding cleaning the sign code for senders in arithmetic expressions or
comparisons." NUMPROC(MIG) is not supported by Enterprise COBOL 6 (a warning; default used);
NOPFD is "the only correct choice in COBOL 6 unless you ... can guarantee that you will ALWAYS
have preferred sign codes."

## 3. ARITH

[verbatim, Performance Tuning Guide 6.4 p. 15, S6] "Use ARITH(EXTEND) only if the larger maximum
number of digits enabled by this option is required (31 instead of 18)." "In addition to allowing
larger variables to be declared, ARITH(EXTEND) also raises the maximum number of digits
maintained for intermediate results."

[paraphrase, V4R2 paper p. 3, S4] COMPAT: max 18 digits for packed-decimal, zoned-decimal and
numeric-edited items; EXTEND: 31 digits, "will cause some degradation in the performance for all
decimal data types due to larger intermediate results."

[paraphrase, APAR PH64935, S11] under ARITH(EXTEND) floating-point intermediates are rounded to
31 decimal digits (a 6.x defect rounded to 33); under ARITH(COMPAT) a floating-point sender
stored into a shorter decimal receiver follows the LR alignment (truncation) rules.

[unverified] PG ARITH text: COMPAT caps numeric literals and data items at 18 digits and
intermediate results at 30 digits; EXTEND raises all three to 31; EXTEND also changes how
floating-point values are converted to fixed point (31 digits kept).

## 4. Appendix A, "Intermediate results and arithmetic precision" (read 2026-10-05, primary)

Read in full from the Enterprise COBOL for z/OS 6.4 Programming Guide, SC27-8714-03 (S19), pages
793-801, once the environment allowed IBM's PDF host (J-095). Everything in this section is
[verbatim] from it, page numbers given; the [unverified] recollection that stood here was wrong in two
places (the cap, and which operand of a division is which) and is gone.

- [verbatim, p. 793] "In compatibility mode ... A maximum of 30 digits is used for fixed-point
  intermediate results." "In extended mode ... A maximum of 31 digits is used for fixed-point
  intermediate results."
- [verbatim, p. 793] Intermediate results arise "In an ADD or SUBTRACT statement that contains more
  than one operand immediately after the verb; In a COMPUTE statement that specifies a series of
  arithmetic operations or multiple result fields; In an arithmetic expression contained in a
  conditional statement or in a reference-modification specification; In an ADD, SUBTRACT, MULTIPLY,
  or DIVIDE statement that uses the GIVING option and multiple result fields; In a statement that uses
  an intrinsic function as an operand; In a statement that contains the ROUNDED phrase".
- [verbatim, p. 794, terms] "i: The number of integer places carried for an intermediate result. (If
  you use the ROUNDED phrase, one more integer place might be carried for accuracy if necessary.)"
  "d: The number of decimal places carried for an intermediate result. (If you use the ROUNDED
  phrase, one more decimal place might be carried for accuracy if necessary.)" "dmax: In a particular
  statement, the largest of the following items: The number of decimal places needed for the final
  result field or fields; The maximum number of decimal places defined for any operand, except
  divisors or exponents; The outer-dmax for any function operand". "op1: The first operand in a
  generated arithmetic statement (in division, the divisor). op2: The second operand in a generated
  arithmetic statement (in division, the dividend)." "i1, i2: The number of integer places in op1 and
  op2" "d1, d2: The number of decimal places in op1 and op2".
- [verbatim, pp. 794-795] "COMPUTE Y = A + B * C - D / E + F ** G ... 1. Exponentiate F by G yielding
  ir1. 2. Multiply B by C yielding ir2. 3. Divide E into D yielding ir3. 4. Add A to ir2 yielding ir4.
  5. Subtract ir3 from ir4 yielding ir5. 6. Add ir5 to ir1 yielding Y."
- [verbatim, p. 795, the precision theoretically possible]

  | Operation | Integer places | Decimal places |
  | --- | --- | --- |
  | + or - | (i1 or i2) + 1, whichever is greater | d1 or d2, whichever is greater |
  | * | i1 + i2 | d1 + d2 |
  | / | i2 + d1 | (d2 - d1) or dmax, whichever is greater |

- [verbatim, pp. 795-796, places carried for +, -, *, /; C is 30 under ARITH(COMPAT) and 31 under
  ARITH(EXTEND), the two tables being the same but for that number]

  | Value of i + d | Value of d | Value of i + dmax | Number of places carried for ir |
  | --- | --- | --- | --- |
  | <C or =C | Any value | Any value | i integer and d decimal places |
  | >C | <dmax or =dmax | Any value | C-d integer and d decimal places |
  | >C | >dmax | <C or =C | i integer and C-i decimal places |
  | >C | >dmax | >C | C-dmax integer and dmax decimal places |

- [verbatim, p. 796, exponentiation] "When op2 is expressed with decimals, floating-point instructions
  are used." "When op2 is an integral literal or constant, the value d is computed as d = d1 * |op2|
  and the value i is computed based on the characteristics of op1: When op1 is a data-name or
  variable, i = i1 * |op2|; When op1 is a literal or constant, i is set equal to the number of
  integers in the value of op1 ** |op2|." Under COMPAT: i + d < 30, "i integer and d decimal places
  are carried"; = 30 with op1 of an odd number of digits, the same; = 30 with an even number, and > 30,
  "Same action as when op2 is an integral data-name or variable" (the exception: "a 30-digit integer
  raised to the power of literal 1"). Under EXTEND: < 31 carried; = 31 or > 31 as for a data-name
  exponent (the same exception at 31). "If op2 is negative, the value of 1 is then divided by the
  result produced by the preliminary computation." [p. 797] "When op2 is an integral data-name or
  variable, dmax decimal places and 30-dmax (compatibility mode) or 31-dmax (extended mode) integer
  places are used. op1 is multiplied by itself (|op2| - 1) times for nonzero op2. If op2 is equal to 0,
  the result is 1. ... Fixed-point exponents with more than nine significant digits are always
  truncated to nine digits." The internal results of the repeated multiplication "are much more
  precise" than ir (p. 797).
- [verbatim, pp. 797-798] "Whenever the number of digits in an intermediate result exceeds 30 in
  compatibility mode or 31 in extended mode, the compiler truncates to 30 (compatibility mode) or 31
  (extended mode) digits and issues a warning. If truncation occurs at run time, a message is issued
  and the program continues running."
- [verbatim, p. 798] "If an operation that involves binary operands requires intermediate results
  longer than 18 digits, the compiler converts the operands to internal decimal before performing the
  operation."
- [verbatim, pp. 798-799] Integer functions: outer-dmax "always zero"; FACTORIAL "30 in compatibility
  mode, 31 in extended mode"; MOD "min(i1 i2)"; INTEGER "one more digit than in the argument";
  INTEGER-PART "same number of digits as in the argument". MAX, MIN, RANGE, REM, SUM: "the outer-dmax
  is always equal to the inner-dmax", each evaluated by the steps given there (REM: divide, remove the
  noninteger digits, multiply by argument two, subtract from argument one).
- [verbatim, p. 800, floating point] "Floating-point instructions are used to compute an arithmetic
  expression if any of the following conditions is true of the expression: A receiver or operand is
  COMP-1, COMP-2, external floating point, or a floating-point literal. An exponent contains decimal
  places. An exponent is an expression that contains an exponentiation or division operator, and dmax
  is greater than zero. An intrinsic function is a floating-point function." COMPAT: "Single precision
  is used if all receivers and operands are COMP-1 data items and the expression contains no
  multiplication or exponentiation operations. In all other cases, long precision is used." EXTEND:
  single as COMPAT; "Long precision is used if all receivers and operands are COMP-1 or COMP-2 data
  items, at least one receiver or operand is a COMP-2 data item, and the expression contains no
  multiplication or exponentiation operations. In all other cases, extended precision is used."
- [verbatim, p. 801, conditions] "In an explicit relation condition where at least one of the
  comparands is an arithmetic expression, dmax is the maximum number of decimal places for any operand
  of either comparand, excluding divisors and exponents." "When the comparison between an arithmetic
  expression and another data item or arithmetic expression does not use a relational operator (that
  is, there is no explicit relation condition), the arithmetic expression is evaluated without regard
  to the attributes of its comparand." "Abbreviated IF statements are handled as though the statements
  were not abbreviated."

What the text leaves open, and the trial against cobc `-std=ibm` that settled it (J-097): an
intermediate's dropped decimal places are truncated (`A / B * B`, 2 and 3, into 9V99 is 1.98; rounding
would give 2.01). ROUNDED's extra place goes to the last operation only (J-099, correcting J-097):
`A / B` into 9V9 is 0.6 and ROUNDED 0.7, so the place exists; `A / B * B` into 9V9 ROUNDED is 1.8, so it
does not reach the earlier intermediates (with it there, 2.0). That is cobc's behaviour; on IBM itself
"might be carried for accuracy if necessary" (p. 794) and APAR PH41526 (ROUNDED with intermediates
over 31 digits, S13) are not settled without a run on IBM. [verbatim, Language Reference 6.4 SC27-8713-03,
p. 296, S20] "When the size of the fractional result exceeds the number of places provided for its
storage, truncation occurs unless ROUNDED is specified. When ROUNDED is specified, the least
significant digit of the resultant identifier is increased by 1 whenever the most significant digit of
the excess is greater than or equal to 5."

The measured reproducer, under this table: `COMPUTE X = 1 / 3 * 300` into `999V99`. dmax is 2 (the
receiver). `1 / 3`: op1 the divisor (i1 1, d1 0), op2 the dividend (i2 1, d2 0); i = i2 + d1 = 1,
d = max(d2 - d1, dmax) = 2: 0.33. `* 300`: i = 1 + 3, d = 2 + 0: 99.00. cobc `-std=ibm` prints 99.00
(oracle/nist/IBM-CHANGES.md); the exact value is 100, and a decimal-exact evaluation 99.99 or 100.00.

## 5. CODEPAGE / COLLSEQ

Primary: default `CODEPAGE(1140)` (S1-S3). Nothing else primary reached. [unverified] CODEPAGE
names the EBCDIC CCSID of alphanumeric/DBCS literals and data and is the source CCSID for
national (UTF-16) conversions; PROGRAM COLLATING SEQUENCE affects alphanumeric comparisons and
SORT/MERGE keys unless COLLATING SEQUENCE is given on the SORT; COLLSEQ is not an Enterprise COBOL
6.x option (it is COBOL for AIX/Linux).

## 6. ZONEDATA / INVDATA (6.1-6.4)

[verbatim, 6.2 What's New, S14] "PI90571: ZONEDATA: The ZONEDATA option is updated to affect the
behaviour of MOVE statements, comparisons, and computations for USAGE DISPLAY or PACKED-DECIMAL
data items that could contain invalid digits, an invalid sign code, or invalid zone bits."
[verbatim, APAR PI90571, S15] "The compiler was fixed to generate an alphanumeric compare when
comparing a PACKED-DECIMAL and USAGE DISPLAY data item when compiling with ZONEDATA(NOPFD) or
ZONEDATA(MIG)." [paraphrase, APARs PH29821/PH31472, S16] ZONEDATA(PFD): compiler assumes valid
data; ZONEDATA(NOPFD|MIG): sign code forced to x'F' on unsigned DISPLAY senders before SRP.
[verbatim, IBM "Pitfalls of invalid programs and data", 2020, S17] migration map: V4
NUMPROC(MIG) -> ZONEDATA(MIG) + any NUMPROC; V4 NUMPROC(NOPFD) -> ZONEDATA(NOPFD)+NUMPROC(NOPFD);
V4 NUMPROC(PFD) -> ZONEDATA(NOPFD)+NUMPROC(PFD); "If you can guarantee that your USAGE DISPLAY
data is all correct, use ZONEDATA(PFD)".
[verbatim, APAR PH37328, S18] ZONEDATA deprecated for INVDATA(FORCENUMCMP|NOFORCENUMCMP,
CLEANSIGN|NOCLEANSIGN): FORCENUMCMP ensures "invalid zone nibbles do not influence the result";
CLEANSIGN sets "sign nibbles for unsigned USAGE DISPLAY, COMP-3 and PACKED-DECIMAL data items" to
x'F' on input; V4 NUMPROC(MIG) ~ INVDATA(FORCENUMCMP,NOCLEANSIGN).

## 7. Division by zero / SIZE ERROR

No primary LR text reached (SIZE ERROR phrases topic ID unknown; `rlpsadd.html` reached but only
cross-references it). [unverified, LR "SIZE ERROR phrases"] division by zero always raises the
size error condition; with ON SIZE ERROR the receiver is unchanged and the imperative runs;
without it, for division by zero the result is undefined ("unpredictable") and for ordinary
overflow the stored value is truncated per TRUNC/USAGE rules. The 6.x LR adds that with
TRUNC(BIN)/COMP-5 size error is detected against the binary capacity, not the PICTURE.

## Sources

- S1 https://www.ibm.com/docs/en/SS6SG3_6.5/pg/ref/rpcoshst.html (PG 6.5 option table)
- S2 https://www.ibm.com/docs/en/SS6SG3_6.2.0/pg/ref/rpcoshst.html
- S3 https://www.ibm.com/docs/en/SS6SG3_6.4.0/pdf/cg.pdf (Customization Guide 6.4, SC27-8712-03)
- S4 https://www.ibm.com/support/pages/system/files/support/swg/swgdocs.nsf/0/bfd525ab04ab4479852576e80064bc8a/$FILE/COBPF420.PDF (V4R2 Performance Tuning, 2010-03-15)
- S5 https://ibmmainframes.com/about59717.html (PG TRUNC(OPT) passage quoted by Bill Woodger)
- S6 https://www.ibm.com/docs/en/SS6SG3_6.4.0/pdf/perfguide.pdf (SC27-9202-02); https://www.ibm.com/docs/SS6SG3_6.3.0/perf/TRUNC.html
- S7 https://www.ibm.com/docs/en/SS6SG3_6.5/Performance_Tuning/COBOL_performance_tuning_webinar.pdf
- S8 https://www.ibm.com/support/pages/apar/PI90560
- S9 https://www.ibm.com/support/pages/apar/PM02897
- S10 https://community.ibm.com/community/user/discussion/pfd-nopfd-zonedata-and-cobol-v63
- S11 https://www.ibm.com/support/pages/apar/PH64935
- S12 https://datafield.dev/intermediate-cobol/part-01/chapter-05/
- S13 https://www.ibm.com/support/pages/apar/PH41526 ; /PH42579
- S14 https://www.ibm.com/docs/en/SS6SG3_6.2.0/pdf/new.pdf ; 6.3: /SS6SG3_6.3.0/pdf/new.pdf
- S15 https://www.ibm.com/support/pages/apar/PI90571
- S16 https://www.ibm.com/support/pages/apar/PH29821 ; /PH31472
- S17 https://www.ibm.com/support/pages/pitfalls-invalid-programs-and-data
- S18 https://www.ibm.com/support/pages/apar/PH37328
- S19 https://publibfp.dhe.ibm.com/epubs/pdf/igy6pg40.pdf (Enterprise COBOL for z/OS 6.4 Programming Guide, SC27-8714-03; Appendix A, pp. 793-801, read in full 2026-10-05)
- S20 https://publibfp.dhe.ibm.com/epubs/pdf/igy6lr40.pdf (Enterprise COBOL for z/OS 6.4 Language Reference; the ROUNDED phrase, p. 296, read 2026-10-05)
- Unreachable this session: PG 6.4 pgmvs.pdf / igy6pg40.pdf / igy3pg*.pdf (digest stops at TOC);
  `ibm.com/docs/en/cobol-zos/6.4.0?topic=...` (403); Migration Guide mg.pdf 6.4/6.5 (403);
  web.archive.org (blocked); cadcobol.com.br mirror (suspended); Micro Focus docs (robots).

## 8. DFSORT padding and truncation (read 2026-10-04, primary)

[verbatim, DFSORT Application Programming Guide, z/OS 2.2, "Padding and truncation", S19] "DFSORT
truncates fixed-length records on the right when the SORTOUT LRECL is smaller than the
SORTIN/SORTINnn LRECL provided that: The application is not a conventional merge or tape work data
set sort. TRUNC=RC16 is not in effect." "DFSORT pads fixed-length records with binary zeros on the
right when the SORTOUT LRECL is larger than the SORTIN LRECL provided that: The Blockset technique is
selected. The application is a sort or copy. PAD=RC16 is not in effect." "DFSORT does not pad or
truncate records returned from an E15 or E35 user exit". Variable-length: VLLONG/NOVLLONG.
[verbatim, same guide, "Specifying EXEC/DFSPARM PARM options", S20] PAD: "RC0 specifies that DFSORT
should issue message ICE171I (once), set a return code of 0 and continue processing when the SORTOUT
LRECL is larger than the SORTIN/SORTINnn LRECL"; RC4 likewise with return code 4; "RC16 specifies
that DFSORT should issue message ICE196A, terminate and give a return code of 16". TRUNC: the same
three for "smaller than". Default: "Usually the installation default." SOLRF "specifies whether
DFSORT should set the SORTOUT LRECL to the reformatted record length when the SORTOUT LRECL is
unknown." [unverified] the IBM-supplied installation default is PAD=RC0, TRUNC=RC0; the rule applies
to the reformatted (OUTREC/OUTFIL) length when SORTOUT LRECL is specified.
- S19 https://www.ibm.com/docs/en/SSLTBW_2.2.0/com.ibm.zos.v2r2.icea100/ice2ca_Padding_and_truncation.htm
- S20 https://www.ibm.com/support/knowledgecenter/SSLTBW_2.2.0/com.ibm.zos.v2r2.icea100/spexec.htm


## 9. Initial values without VALUE, and WSCLEAR (read 2026-10-06, primary; R-057)

Language Reference 6.4, p. 103 (PROGRAM-ID paragraph, the initial state of a program):

> "If a VALUE clause is used in the description of the data item, the data item is initialized to the
> defined value. If a VALUE clause is not associated with a data item, the initial value of the data
> item is undefined."

Language Reference 6.4, p. 245 (VALUE clause):

> "The data item assumes the specified value at the beginning of program execution. If the initial
> value is not explicitly specified, the value is unpredictable."
>
> "Initialization is independent of any BLANK WHEN ZERO or JUSTIFIED clause that is specified."

R-057's C-1 (an item without VALUE has an unpredictable initial value) is quoted here. The second
quotation is a rule the lowering must follow: a VALUE is placed without JUSTIFIED (J-118).

WSCLEAR: the z/OS Programming Guide 6.4 names it once, p. 864 (Appendix G, COBOL SYSADATA file
contents, an option byte): "Bit 1 = WSCLEAR, Bit 0 = NOWSCLEAR (AIX only)". It is not among the z/OS
compiler options the guide describes. R-057's C-2 (WSCLEAR, Enterprise COBOL 6.2 and later, clears
WORKING-STORAGE to binary zeros) is therefore not supported for z/OS by this manual: a finding for
Fable (HANDOFF), not a verification. LE's STORAGE runtime option, which can set the bytes of fresh
storage, is not described in the Programming Guide and was not read.

LE's STORAGE option (read 2026-10-06, primary; R-057 C-2 restated). z/OS Language Environment
Programming Reference, z/OS 3.1, SA38-0683-60, pp. 69-70:

> "STORAGE controls the initial content of storage when allocated and freed. [...] If you specify one
> of the parameters in the STORAGE runtime option, all allocated storage that is processed by that
> parameter is initialized to the specified value. Otherwise, it remains uninitialized."
>
> "The default value for non-CICS applications is STORAGE(NONE,NONE,NONE,0K)."
>
> heap_alloc_value: "Two hex digits without quotation marks. If you specify two hex digits, every byte
> of the allocated heap storage is initialized to that value. [...] A heap_alloc_value of 00
> initializes heap storage to X'0000...00'." "NONE. If you specify NONE, the allocated heap storage is
> not initialized. NONE is the default."

Enterprise COBOL Programming Guide 6.4, p. 40 (HEAP):

> "COBOL WORKING-STORAGE is allocated from heap storage when the COBOL program is compiled with the RENT
> option and is in one of the following cases: [...] A COBOL 6.1 or later program"

So for a RENT program, WORKING-STORAGE without VALUE holds STORAGE's heap_alloc_value, or nothing set
under the default: `ibm.wsinit` is that value (`NONE`, or the two hex digits). A NORENT program's
WORKING-STORAGE is not covered by these pages. (A single quoted character is also allowed by LE, the
character's EBCDIC byte; the field records the byte.)

## 10. The authority behind each discovery (read 2026-10-06, primary; R-058)

Each line verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR, `igy6lr40.pdf`, S20)
or Programming Guide (PG), checked word for word against the PDF's text; `docs/DISCOVERIES.md` cites
them by row. Appendix A's arithmetic is §4; Table 48 (DISPSIGN) is the PG's, quoted in `rt/Display`.

- LR p. 198 (MOVE, alphanumeric receivers): "If the sending item is larger than the receiving item, the
  leftmost character positions are truncated" (a JUSTIFIED receiver). D-013.
- LR p. 223 (floating insertion editing): "Any simple-insertion symbols (B 0 / ,) within or to the
  immediate right of the string of floating insertion symbols are considered part of the floating
  character-string." D-007.
- LR p. 224 (zero suppression): IBM suppresses "any leading zero in the data that appears in the same
  character position". D-006.
- LR p. 238 (USAGE BINARY): "1 through 4 2 bytes (halfword)". D-012.
- LR p. 297 (SIZE ERROR, COMPUTATIONAL-5): "the largest value that the resultant data item can contain is the value
  implied by the item's decimal PICTURE character-string, regardless of the TRUNC compiler option". D-009.
- LR p. 307: "When the FROM phrase is omitted, the system input device is assumed." pp. 307-308: "If EOF
  is encountered before any data has been moved to the receiving area, padding will not take place and
  the contents of the receiving area are unchanged." D-015.
- LR p. 437 (SEARCH VARYING): "the occurrence number represented by index-name-1 is increased by the same
  amount as the search index-name and at the same time"; for an integer item, "whenever the search index
  is increased, the specified data item is simultaneously increased by 1". D-014.
- LR p. 74 (relative subscripting): "The operators + and - must be preceded and followed by a space."
  (Katana's own grammar bug, J-125; cobc agrees with IBM.)
- PG pp. 796-797 (exponentiation): "If op2 is equal to 0, the result is 1"; "If op2 is negative, the
  value of 1 is then divided by the result produced by the preliminary computation". D-016.
- LR p. 16 (LOW-VALUE): "Represents one or more occurrences of the character that has the lowest ordinal
  position in the collating sequence used"; "For other alphanumeric data, the value depends on the
  collating sequence in effect." D-017.
- LR p. 123 (PROGRAM COLLATING SEQUENCE): it "determines the truth value of the following alphanumeric
  comparisons: Those explicitly specified in relation conditions; Those explicitly specified in
  condition-name conditions". D-018.
- LR pp. 307-308 (ACCEPT from the system input device): "The system input device is read until the
  receiving area is filled or EOF is encountered. If the length of the receiving area is not an even
  multiple of the system input device record length, the final record will be truncated as required."
  "Each input record is concatenated with the previous input record." D-020.
- LR p. 770 (Appendix E, Table 83, Reserved words): the words from NEXT run NEXT, NO, NOT, NULL, NULLS;
  NOTHING is not among them. D-021.
- LR p. 706 (EXCEPTION/ERROR declarative, INPUT): "the procedure is executed for all files opened in
  INPUT mode or in the process of being opened in INPUT mode that get an error". D-025.
- LR p. 302 (Table 34, 42): "A CLOSE statement was attempted for a file not in the open mode." D-026.
- LR pp. 297-298 (Multiple results): "When an arithmetic statement has multiple results, execution
  conceptually proceeds as follows: 1. The statement performs all arithmetic operations to find the
  result to be placed in the receiving items, and stores that result in a temporary location. 2. A
  sequence of statements transfers or combines the value of this temporary result with each single
  receiving field. The statements are considered to be written in the same left-to-right order in
  which the multiple results are listed." Its example, `ADD A, B, C, TO C, D(C), E.`, "is equivalent
  to" `ADD A, B, C GIVING TEMP.` `ADD TEMP TO C.` `ADD TEMP TO D(C).` `ADD TEMP TO E.`, and "When the
  addition operation for D(C) is performed, the subscript C contains the new value of C." (p. 297's
  "Overlapping operands" paragraph comes just before it.) D-022.

## 11. LINAGE (read 2026-10-08, primary; for the SQ module's 39 LINAGE programs)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference.

- LR p. 189 (LINAGE clause): integer-8 or data-name-5 is "The number of lines that can be written or
  spaced on this logical page. The area of the page that these lines represent is called the page
  body." WITH FOOTING AT "specifies the first line number of the footing area within the page body";
  "If the FOOTING phrase is omitted, its assumed value is equal to that of the page body". LINES AT
  TOP and LINES AT BOTTOM: the margins, "The value can be zero", zero when omitted. p. 190: "The
  logical page size specified in the LINAGE clause is the sum of all values specified in each phrase
  except the FOOTING phrase"; "Each logical page immediately follows the preceding logical page, with
  no additional spacing provided." The data-names "determine the page body, first footing line, top
  margin, and bottom margin for the first logical page only" at OPEN OUTPUT; "At the time a WRITE
  statement with the ADVANCING PAGE phrase is executed or a page overflow condition occurs, the values
  of data-name-5, data-name-6, data-name-7, and data-name-8 if specified, are used to determine" those
  of the next logical page. "A LINAGE clause under an SD is syntax checked, but has no effect".
- LR pp. 23-24 (LINAGE-COUNTER): "A separate LINAGE-COUNTER special register is generated for each FD
  entry that contains a LINAGE clause. When more than one is generated, you must qualify each
  reference to a LINAGE-COUNTER with its related file-name." "The value in LINAGE-COUNTER at any given
  time is the line number at which the device is positioned within the current page"; "it must not be
  modified by" PROCEDURE DIVISION statements; "initialized to 1 when an OPEN statement for its
  associated file is executed"; an integer LINAGE makes it "a binary item with the same number of
  digits as that integer", a data-name one "the same PICTURE and USAGE as that data-name".
- LR p. 474 (WRITE, LINAGE-COUNTER rules): "1. If ADVANCING PAGE is specified, LINAGE-COUNTER is reset
  to 1. 2. If ADVANCING identifier-2 or integer is specified, LINAGE-COUNTER is increased by the value
  in identifier-2 or integer. 3. If the ADVANCING phrase is omitted, LINAGE-COUNTER is increased by 1.
  4. When the device is repositioned to the first available line of a new page, LINAGE-COUNTER is
  reset to 1." "When the ADVANCING phrase is omitted, automatic line advancing is provided, as if AFTER
  ADVANCING 1 LINE had been specified." Rule 6 of ADVANCING: with PAGE and a LINAGE clause, "the
  repositioning is to the first printable line of the next page, as specified in that clause".
- LR p. 475 (END-OF-PAGE): "An END-OF-PAGE condition is reached when execution of a WRITE END-OF-PAGE
  statement causes printing or spacing within the footing area of a page body. This occurs when
  execution of such a WRITE statement causes the value in the LINAGE-COUNTER special register to equal
  or exceed the value specified in the WITH FOOTING phrase"; "The WRITE statement is executed, and then
  the END-OF-PAGE imperative-statement is executed." "An automatic page overflow condition is reached
  whenever the execution of any given WRITE statement ... cannot be completely executed within the
  current page body. This occurs when a WRITE statement, if executed, would cause the value in the
  LINAGE-COUNTER to exceed the number of lines for the page body ... In this case, the line is printed
  BEFORE or AFTER (depending on the option specified) the device is repositioned to the first
  printable line on the next logical page ... If the END-OF-PAGE phrase is specified, the END-OF-PAGE
  imperative-statement is then executed." Both conditions can occur at once.

**Where the text leaves room: the rivals, named before any run (R-052).**

- L1, the counter after an overflow. (a) The repositioning replaces the advance: the line goes on the
  new page's first printable line, LINAGE-COUNTER 1 ("the line is printed BEFORE or AFTER ... the
  device is repositioned"; rule 4). (b) The advance is applied on the new page: 1 + n.
- L2, END-OF-PAGE's comparison. (a) After the write's increment ("causes the value ... to equal or
  exceed"). (b) Before it.
- L3, data-name values. (a) Read at OPEN and again at each ADVANCING PAGE or overflow (the text).
  (b) Read once at OPEN.

**The trial, each case's value under each reading, written before the run.** One file, `LINAGE 5
WITH FOOTING 4 LINES AT TOP 1 LINES AT BOTTOM 1`, LINAGE-COUNTER displayed after each step and END-OF-
PAGE marked when its statement runs:

| Step | Statement | (a)(a) | L1(b) | L2(b) |
| --- | --- | --- | --- | --- |
| 1 | OPEN OUTPUT | 1 | 1 | 1 |
| 2 | WRITE (no ADVANCING) | 2 | 2 | 2 |
| 3 | WRITE BEFORE 1 | 3 | 3 | 3 |
| 4 | WRITE AFTER 1 AT EOP | 4, EOP | 4, EOP | 4, no EOP |
| 5 | WRITE AFTER 2 AT EOP (6 > 5, overflow) | 1, EOP | 3, EOP | 1, EOP |
| 6 | WRITE AFTER PAGE | 1 | 1 | 1 |
| 7 | WRITE BEFORE 3 | 4 | 4 | 4 |
| 8 | WRITE BEFORE 2 (6 > 5, overflow) | 1 | 3 | 1 |

L3 on a second file, `LINAGE IS LN` with LN 5 at OPEN, then LN set to 2 and eight WRITEs with no
ADVANCING: (a) 2, 3, 4, 5, then the overflow opens a 2-line page: 1, 2, then 1, 2; (b) the body stays
5 lines: 2, 3, 4, 5, 1, 2, 3, 4. cobc is run on the same program; where it differs from
(a)(a) it is a D-number and the lowering keeps IBM's reading.

**cobc's print-line form for a LINAGE file (the lane's profile, R-019; not IBM semantics).** What a
LINAGE file holds on disk is cobc's choice, as R-019's ADVANCING line feeds are. J-149's run gave,
for its LINAGE file, 22 lines: each record on a line of its own with trailing spaces trimmed, no form
feed, the same WRITEs on a file without LINAGE giving R-019's form (records adjacent, spaces kept, a
form feed for PAGE). One reading fits all 22 lines, B1 (stream): OPEN writes the top margin as blank
lines; every record is a line of its own; AFTER n puts n-1 blank lines before it, BEFORE n n-1 after
it; a page change (overflow or PAGE) writes the rest of the page body (body - LINAGE-COUNTER), the
bottom margin and the next top margin as blank lines; CLOSE writes nothing. Its rival B2 (physical
lines, an overprint pushed down) puts J-149's L8 on line 18, not 19, and is out. What the run did not
separate, named before the second run:

- Z, ADVANCING 0 (valid, LR p. 473): (z1) a line of its own, as B1; (z2) the record joins the line
  before it.
- C, CLOSE: (c1) nothing, as B1; (c2) the rest of the page as blank lines.
- T, spaces: (t1) trailing spaces trimmed, inner kept, as seen; (t2) all spaces kept.

The trial, `LINAGE 4 WITH FOOTING 3 LINES AT TOP 2 LINES AT BOTTOM 1`, records of four characters:
OPEN; WRITE "A" AFTER 0; WRITE "B" BEFORE 0; WRITE "C" AFTER 3; WRITE "D" AFTER 2 (overflow); WRITE
"E" BEFORE PAGE; WRITE "F G " (no ADVANCING); CLOSE. Under B1 with z1, c1, t1 the file's lines are:
1-2 blank, 3 A, 4 B, 5-6 blank, 7 C, 8-10 blank, 11 D, 12 E, 13-18 blank, 19 `F G`, and no more.
With z2: A and B join the line before them (line 3 `AB`, every later line one higher). With c2: two
more blank lines after line 19 (the body's last two lines) and one for the bottom margin. With t2:
line 19 is `F G ` and lines 3, 4, 7, 11, 12 carry their records' trailing spaces.

## 12. FILE STATUS and EXCEPTION/ERROR declaratives (read 2026-10-09, primary; the SQ module)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference.

- LR p. 299 (File status key): "a value is placed in the specified file status key ... during
  execution of any request on that file; the value indicates the status of that request. The value is
  placed in the file status key before execution of any EXCEPTION/ERROR declarative, INVALID KEY
  phrase, or AT END phrase associated with the request."
- LR pp. 300-302 (Table 34), for sequential files: 00 success; 04 a READ whose record length is outside
  the FD's; 05 "An OPEN statement was successfully executed, but the referenced optional file was
  unavailable"; 10 "no next logical record existed ... Or the first READ was attempted on an optional
  input file that was unavailable"; 30 no further information; 34 a boundary violation; 35 OPEN INPUT,
  I-O or EXTEND "on a nonoptional file that was unavailable"; 37 the open mode not supported; 38 "An
  OPEN statement was attempted on a file previously closed with lock"; 39 attributes in conflict; 41
  "An OPEN statement was attempted for a file in the open mode"; 42 "A CLOSE statement was attempted
  for a file not in the open mode"; 46 "A sequential READ statement was attempted on a file open in the
  input or I-O mode and no valid next record had been established because ... The preceding READ
  statement caused an at-end condition"; 47 "a READ statement was attempted on a file not open in the
  input or I-O mode"; 48 "a WRITE statement was attempted on a file not open in the I-O, output, or
  extend mode".
- LR p. 57: "The USE statement itself is never executed"; "After the procedure is executed, control is
  returned to the routine that activated it." (p. 57, PROCEDURE DIVISION structure: execution begins
  after the declaratives.)
- LR pp. 705-706 (EXCEPTION/ERROR declarative): file-name-1, "the procedure is executed only for the
  files named"; it "takes precedence over a declarative statement specifying the open mode of the
  file"; INPUT, OUTPUT, I-O, EXTEND: "executed for all files opened in INPUT mode or in the process of
  being opened in INPUT mode that get an error" (and so for each mode). "The EXCEPTION/ERROR procedure
  is executed: Either after completing the system-defined input/output error routine, or Upon
  recognition of an INVALID KEY or AT END condition when an INVALID KEY or AT END phrase has not been
  specified in the input/output statement". "After execution of the EXCEPTION/ERROR procedure ... the
  input/output control system returns control to the next executable statement following the
  input/output statement whose execution caused the exception."
- PG 6.4 p. 179 (QSAM): with no FILE STATUS key and no declarative, serious errors end the run (J-144);
  with either, the program continues.

**Where the text leaves room: the rivals, named before the run.**

- S1, an unsuccessful OPEN: (a) the file stays not open, so a READ is 47; (b) it counts as open.
- S2, a mode declarative after a failed OPEN: (a) "in the process of being opened in INPUT mode" covers
  an OPEN INPUT that fails (38), but a later CLOSE of the still-closed file is no INPUT-mode error; (b)
  the mode of the last OPEN attempt stays with the file, so that CLOSE runs the INPUT declarative too.
- S3, a READ after AT END: (a) 46 and, as an error, the INPUT declarative; (b) 10 again.

**The trial, STATRIAL.** OPTF is `SELECT OPTIONAL` of an absent file, NOPT a non-optional absent file,
WF a file to write; each has its FILE STATUS item. Declaratives: one USE AFTER ERROR ON NOPT, which
shows `D2` and NOPT's status, and one ON INPUT, which shows `DI` and the three statuses. WF has no
declarative of its own. Each step then shows its file's status. Values under (a)(a)(a), the text:

| Step | Statement | Lines shown |
| --- | --- | --- |
| 1 | OPEN INPUT OPTF | 05 |
| 2 | READ OPTF AT END (shows AE) | AE, 10 |
| 3 | READ OPTF AT END (shows AE) | DI, 46 |
| 4 | CLOSE OPTF | 00 |
| 5 | OPEN INPUT NOPT | D2 35, 35 |
| 6 | READ NOPT AT END | D2 47, 47 |
| 7 | CLOSE NOPT | D2 42, 42 |
| 8 | OPEN OUTPUT WF | 00 |
| 9 | OPEN OUTPUT WF | 41 (no declarative: the run goes on) |
| 10 | WRITE WF | 00 |
| 11 | READ WF AT END | 47 (no declarative for OUTPUT) |
| 12 | CLOSE WF WITH LOCK | 00 |
| 13 | OPEN INPUT WF | DI, 38 |
| 14 | CLOSE WF | 42 |
| 15 | WRITE WF | 48 |

Under S1(b), step 6 is 10 or AE and no D2. Under S2(b), step 14 shows DI before 42. Under S3(b), step 3
shows AE and 10, no DI. Step 3 under (a): a READ attempted after at end is error 46, not an at-end
condition, so the AT END phrase does not run and the INPUT declarative does.
