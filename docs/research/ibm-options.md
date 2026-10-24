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

**REWRITE on a sequential file (read 2026-10-09, LR 6.4 pp. 300-302, 411, 432-433).** "When the
REWRITE statement is executed, the associated direct-access file must be open in I-O mode"; "For files
in the sequential access mode, the last prior input/output statement executed for this file must be a
successfully executed READ statement. When the REWRITE statement is executed, the record retrieved by
that READ statement is logically replaced." "The number of character positions in record-name-1 must
equal the number of character positions in the record being replaced." "The file position indicator is
not affected by execution of the REWRITE statement." Table 49 (p. 411): REWRITE only in I-O open mode.
Table 34: 43 "the last input-output statement executed for the associated file prior to the execution
of a REWRITE statement was not a successfully executed READ statement"; 44 a record not the size of
the one replaced; 49 "a DELETE or REWRITE statement was attempted on a file not open in the I-O mode";
05 for OPEN I-O of an absent OPTIONAL file, which "had been created".

Rivals, named before the run: W1, a second REWRITE after one READ: (a) 43, its last prior statement
being a REWRITE; (b) allowed. W2, a REWRITE after a READ at end (10): (a) 43, status 10 not being a
successful READ; (b) the last record replaced. W3, the READ after a REWRITE: (a) the next record, the
position untouched; (b) the rewritten one again.

The trial, REWTRIAL: a file of three 2-character records R1, R2, R3, then OPEN I-O and, showing the
status (and the record, after a READ): 1 REWRITE "X0" with no READ: 43; 2 READ: 00 R1; 3 REWRITE "N1":
00; 4 REWRITE "M1": 43; 5 READ: 00 R2; 6 READ: 00 R3; 7 READ: 10; 8 REWRITE "N3": 43; 9 CLOSE: 00; then
OPEN INPUT and the three records read back: N1, R2, R3; 10 a READ and a REWRITE on the INPUT file: 00
then 49. Under W1(b): step 4 is 00 and the first record reads back M1. Under W2(b): step 8 is 00 and
the third reads back N3. Under W3(b): step 5 shows N1 (and 6 R2, 7 R3).

## 13. Variable-length records (read 2026-10-09, primary; SQ106A, SQ107A, SQ134A, SQ212A, SQ218A-SQ224A, SQ227A, SQ228A)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR) and Programming Guide (PG).

- LR p. 186 (RECORD clause): "When the RECORD clause is omitted, the compiler determines the record
  lengths from the record descriptions. When one of the entries within a record description contains
  an OCCURS DEPENDING ON clause, the compiler uses the maximum value of the variable-length item to
  calculate the number of bytes needed to store the record internally."
- LR pp. 186-187: Format 1 "specifies the number of bytes for fixed-length records"; Format 2 (RECORD
  CONTAINS integer-4 TO integer-5) "for either fixed-length or variable-length records. Fixed-length
  records are obtained when all 01 record description entry lengths are the same"; Format 3 (RECORD IS
  VARYING ... DEPENDING ON data-name-1) "is used to specify variable-length records". Without
  integer-6 the minimum "is equal to the least number of bytes described for a record in that file";
  without integer-7 the maximum is "the greatest".
- LR p. 188: "After the successful execution of a READ or RETURN statement for the file, the contents
  of the data item referenced by data-name-1 indicate the number of bytes in the record just read." "The
  execution of a DELETE, RELEASE, REWRITE, START, or WRITE statement or the unsuccessful execution of a
  READ or RETURN statement does not alter the content of the data item referenced by data-name-1."
  "During the execution of a RELEASE, REWRITE, or WRITE statement, the number of bytes in the record is
  determined by the following conditions: If data-name-1 is specified, by the content of the data item
  referenced by data-name-1; If data-name-1 is not specified and the record does not contain a variable
  occurrence data item, by the number of bytes positions in the record; If data-name-1 is not specified
  and the record contains a variable occurrence data item, by the sum of the fixed position and that
  portion of the table described by the number of occurrences at the time of execution of the output
  statement." For READ ... INTO the bytes moved are "If data-name-1 is specified, by the content of the
  data item referenced by data-name-1; If data-name-1 is not specified, by the value that would have
  been moved into the data item referenced by data-name-1 had data-name-1 been specified."
- LR p. 425 (READ INTO): "If the file description entry contains a RECORD IS VARYING clause, the
  implied move is a group move."
- LR p. 426 (Processing files with variable-length records): "After a READ statement is executed, only
  those data items within the range of the current record are replaced; data items stored beyond that
  range are undefined." "If the length of the current record that is read is less than the minimum
  size specified by the record description entries for file-name-1, the portion of the record area
  which is to the right of the last valid character read is undefined. If the length of the current
  record exceeds the record description entries for file-name-1, the record is truncated on the right
  to the maximum record definition size." "In either of the previous cases, the READ statement is
  successful, and the I-O status is set to either 00 ... or 04 ..., depending on the VLR compiler
  option setting."
- LR p. 300 (Table 34): 04, a READ was successful "but the length of the record being processed did
  not conform to the fixed file attributes for that file", the character positions read "less than the
  minimum size or ... greater than the maximum size specified by the record description entries"; p.
  302: 44 "an attempt was made to write or rewrite a record that was larger than the largest or smaller
  than the smallest record allowed by the RECORD IS VARYING clause of the associated file-name".
- PG pp. 422-423 (VLR): "Default is: VLR(STANDARD)". "VLR(STANDARD) checks the size of the read record
  against the declaration of the FD level 01 clause"; Table 52: a record of length 15 under "varying in
  size from 10 to 80" with level-01 records of 20 and 50 is FS=04 under VLR(STANDARD), FS=00 under
  VLR(COMPAT).
- PG p. 169: with "the RECORD CONTAINS integer clause (format-1 RECORD clause) ... the file is always
  fixed format with record length integer even if there are multiple level-01 record description
  entries with different lengths". PG p. 170: the recording mode is V for RECORD IS VARYING, for RECORD
  CONTAINS integer-1 TO integer-2, or with no RECORD clause and "multiple level-01 records ... that are
  of different sizes or contain an OCCURS DEPENDING ON clause".
- PG p. 171 (Layout of format-V records): each logical record is preceded by "cc", 4 bytes: "ll
  Represents 2 bytes designating the logical record length (including the cc field). bb Represents 2
  bytes reserved for system use." "Files in the z/OS UNIX file system are never blocked."

What a program can see, and the rivals named before the run (V3 is the text's "undefined"):

| | Question | (a), the text | (b) |
| --- | --- | --- | --- |
| V1 | the length a WRITE writes | DEPENDING ON's value, else the record-name's bytes, else the fixed part and the current occurrences (p. 188) | always the maximum |
| V2 | DEPENDING ON after a READ | the length read; unchanged after an unsuccessful READ or a WRITE (p. 188) | unchanged |
| V3 | the record area past a short record | undefined (p. 426): kept from before, or spaces, or else | |
| V4 | READ of a record shorter than every 01 but within VARYING | 04, VLR(STANDARD) the default (PG Table 52) | 00 (VLR(COMPAT)) |
| V5 | WRITE outside VARYING's bounds | 44, nothing written (p. 302) | written |
| V6 | READ INTO from a variable file | a group move of the record's length (pp. 188, 425) | of the 01 record's length |
| V7 | REWRITE of a variable record | 44 for another length; the next REWRITE 43, its last prior statement not a READ (§12) | allowed |

The trial, VARTRIAL (test/katana/lower/programs): VF varying 2 to 10 depending on L, records R5 and R10;
F2 "RECORD CONTAINS 4 TO 8" with S4 and S8; F3 with no RECORD clause and one record, N and an ODO
table of N. Predicted under (a), status then L (or the record shown): 1 WRITE L=10: 00 10; 2 L=5: 00
05; 3 L=3: 00 03; 4 L=11: 44 11; 5 L=1: 44 01; 6 READ: 00 10 AAAAAAAAAA; 7 READ: 00 05 BBBBB and five
undefined; 8 READ: 04 03 CCC and seven undefined; 9 READ: at end, 10 03; 10 READ INTO W(12): 00
"AAAAAAAAAA  "; 11: 00 "BBBBB       "; 12-14 F2 read back with S8 filled with dots first: 1111 then
four undefined, 22222222, 3333 then four undefined; 15 at end, 10; 16 F3 READ INTO W: 00 2 "2ab" and
nine spaces; 17: 00 5 "5vwxyz" and six spaces; 18 at end, 10; 19 OPEN I-O, READ, REWRITE with L=5: 44;
20 REWRITE with L=10: 43. Under V1(b): 7 reads B's to ten, 13-14 show F2's whole area, 16 shows
"2ab###". Under V2(b): 6-8 show L 00. Under V4(b): 8 is 00. Under V5(b): 4 and 5 are 00 and two more
records read back. Under V6(b): 11 shows ten bytes of R10. Under V7(b): 19 is 00.

On disk, a variable file in the Java lane is IBM's format-V unblocked: each record after its 4-byte
"cc", ll big-endian and bb zero (PG p. 171). No program can see it; it is the lane's choice, from IBM.

The run (J-164), under cobc 3.1.2 `-std=ibm`: V1, V2, V6 and V7 the text's (a) at every step; V3, the
area past a short record, kept from before ("BBBBBAAAAA", "CCCBBAAAAA"), which the Java lane follows,
the text leaving it undefined; V5 (a) below the minimum (44 for L 1) and neither reading above it: L
11 is 00 and a 10-byte record written, D-027; V4 (b), 00 for the 3-byte record, VLR(COMPAT)'s, where
the default VLR(STANDARD) gives 04, D-028. VARTRIAL is an IBM reading (the text's (a), V3 kept).

## 14. COPY REPLACING and REPLACE (read 2026-10-16, primary; NIST SM)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference.

- LR p. 693 (COPY, debugging lines): "Debugging lines are permitted in library text and pseudo-text.
  Text words within a debugging line participate in the matching rules as if the letter "D" did not
  appear in the indicator area."
- LR p. 701 (REPLACE, comparison rules): "Whenever a match occurs between pseudo-text-1 and the source
  text, the corresponding pseudo-text-2 replaces the matched text in the source text. ... The source
  text word that immediately follows the rightmost text word that participated in the match is then
  considered as the leftmost source text word. The comparison cycle starts again, beginning with the
  first occurrence of pseudo-text-1"; "If no match occurs, the comparison is repeated with each
  successive occurrence of pseudo-text-1 ... until either a match is found or no further REPLACING
  operands exist"; "For matching purposes, each occurrence of a separator comma, a separator semicolon,
  or a sequence of one or more separator spaces is considered to be a single space."
- LR p. 702: "Comment lines, inline comments, or blank lines in the source text, pseudo-text-1, or
  partial-word-1 are ignored for purposes of matching."

So a replacement is never matched again (SM206A's REPLACING ==1== BY ==5== ==5== BY ==7== gives 5), the
replaceables are tried in the order written, and a debugging line's words take part in a match whether
or not WITH DEBUGGING MODE is written. cobc -std=ibm fails SM206A's PST-TEST-009, a debugging line inside
the copied text that the pseudo-text names: D-029.

## 15. Intrinsic functions (read 2026-10-16, primary; NIST IF)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR) and Programming Guide (PG).

- LR p. 498 (Types of functions): "Numeric functions are of class and category numeric. The returned
  value is always considered to have an operational sign and is a numeric intermediate result."
  "Integer functions are of class and category numeric. The returned value is always considered to
  have an operational sign and is an integer intermediate result." "Alphanumeric functions are of class
  and category alphanumeric. The value returned has an implicit usage of DISPLAY."
- LR p. 499: "A numeric function can be used only where an arithmetic expression can be specified."
  "An integer function can be used only where an arithmetic expression can be specified."
- LR pp. 509-514 (Table 59, Table of functions): each function's arguments and type (I integer, N
  numeric, X alphanumeric); MAX, MIN, RANGE and SUM "the type of function depends on the arguments".
- PG pp. 798-799 (Intrinsic functions evaluated in fixed-point arithmetic): "Integer intrinsic functions
  return an integer; thus their outer-dmax is always zero"; digit precision DATE-OF-INTEGER 8,
  DAY-OF-INTEGER 7, FACTORIAL "30 in compatibility mode", INTEGER-OF-DATE 7, INTEGER-OF-DAY 7, LENGTH 9,
  MOD "min(i1 i2)", ORD 3, ORD-MAX 9, ORD-MIN 9; INTEGER "For a fixed-point argument: one more digit
  than in the argument", INTEGER-PART "same number of digits as in the argument". "A mixed intrinsic
  function is a function whose result type depends on the type of its arguments. A mixed function is
  fixed point if all of its arguments are numeric and none of its arguments is floating point"; MAX,
  MIN, RANGE, REM and SUM by "the rules for fixed-point arithmetic and intermediate results ... to each
  step in the algorithm" (REM: "Divide argument one by argument two. Remove all noninteger digits from
  the result ... Multiply ... by argument two. Subtract ... from argument one").
- PG p. 800: "If any operation in an arithmetic expression is computed in floating-point arithmetic, the
  entire expression is computed as if all operands were converted to floating point"; floating point is
  used when "An intrinsic function is a floating-point function". PG p. 801: "In compatibility mode,
  floating-point intrinsic functions always return a long (64-bit) floating-point value." PG p. 57:
  floating-point functions "return a long (64-bit) or extended-precision (128-bit) floating-point value
  (depending on whether you compile using the default option ARITH(COMPAT) or using ARITH(EXTEND))".

The floating-point functions, read by elimination: every numeric (N) function of Table 59 that PG pp.
798-799 does not list as integer or mixed: ACOS, ANNUITY, ASIN, ATAN, COS, LOG, LOG10, MEAN, MEDIAN,
MIDRANGE, NUMVAL, NUMVAL-C, PRESENT-VALUE, RANDOM, SIN, SQRT, STANDARD-DEVIATION, TAN, VARIANCE. An
expression holding one is computed in Java `double`, IEEE binary64, where IBM's long is hexadecimal
(a 56-bit fraction): R-023's approximation, with its finding NE-06.

The trial is R-023 C-2's, named there before this pass: NIST IF through the Java lane. Predicted, under
C-2 (IEEE is close enough): every IF test cobc passes passes in the Java lane, apart from what cobc's own
departures explain. Under its rival (the hexadecimal fraction matters): some float test fails in the
Java lane where cobc passes, at its last digit. RANDOM's sequence is IBM's own and named nowhere; a test
that prints or compares its values is a reading of the lane, recorded when met.

- LR pp. 501-502 (Arguments): "Specifying ALL as a subscript is equivalent to specifying all table
  elements possible using every valid subscript in that subscript position." "If there are multiple ALL
  subscripts, Table-name(ALL, ALL, ALL), the first implicit argument is Table-name(1, 1, 1), where each
  ALL has been replaced by 1. The next argument is Table-name(1, 1, 2), where the rightmost subscript has
  been incremented by 1." cobc 3.1.2 refuses an ALL subscript in a function argument: D-030.
- PG p. 52 (Conversions that result in rounding): "If a USAGE COMP-1 data item, a USAGE COMP-2 data item,
  an external floating-point data item, or a floating-point literal is moved to a fixed-point data item,
  rounding occurs in the low-order position of the target data item." IBM's text names moves; Katana
  reads the store of a floating-point expression's value into a fixed-point receiver the same way (the
  value is a long floating-point one, p. 801), a reading of R-023, not IBM's words.

Result (J-174): C-2 held. Every IF program cobc compiles reports what cobc reports through the Java lane
(33 of 45); the other 12 are those cobc refuses at an ALL subscript (D-030), and 11 of them pass every
test in the Java lane (IF402M reports no tests). One reading was refuted on the way: a floating-point
value cut, not rounded, into its receiver gave .42899 for NUMVAL(".429") (IF125A); rounding, as above,
gives .429.

## 16. Segmentation (read 2026-10-16, primary; NIST SG)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR).

- LR p. 264 (Section-header): "A section-name followed by the keyword SECTION, optionally followed by a
  priority-number, followed by a separator period." Priority-number: "An integer or a positive signed
  numeric literal ranging in value from 0 through 99. Priority-number identifies a fixed segment or an
  independent segment that is to contain the section."
- LR p. 265 (Segments): "A segment consists of all sections in a program that have the same
  priority-number." "Segments with a priority-number of 0 through 49 are fixed segments. Segments with a
  priority-number of 50 through 99 are independent segments." "In fixed segments, procedures are always
  in last-used state. In independent segments, procedures are in initial state each time the segment
  receives control from a segment with a different priority-number, except when the transfer of control
  results from the execution of a GOBACK or EXIT PROGRAM statement." "Enterprise COBOL does not support
  the overlay feature of the 85 COBOL Standard segmentation module."
- LR p. 318 (ALTER, Segmentation considerations): "Altered GO TO statements in independent segments are
  returned to their initial state when control is transferred to the independent segment that contains
  the ALTERED GO TO from another independent segment with a different priority-number. This transfer of
  control can take place because of: The effect of previous statements; An explicit transfer of control
  with a PERFORM or GO TO statement; A sort or merge statement with the INPUT or OUTPUT phrase specified".
- LR p. 123 (SEGMENT-LIMIT): "The SEGMENT-LIMIT clause is syntax checked but has no effect on the
  execution of the program."

What a procedure's state is: in Katana's lowering, only an altered GO TO has one (a PERFORM's return is
the dispatcher's, not the segment's). So segmentation is: when does an independent segment's ALTERs
reset. IBM names no segment for a section without a priority-number, nor for paragraphs outside any
section; Katana reads them as in no independent segment, as only 50 through 99 name one.

The two pages differ, and neither says whether the end of a PERFORM, returning to an independent
segment, is a transfer into it. The rivals, before the run:

- R1, p. 265: an independent segment resets when it receives control from any segment with a different
  priority-number, fixed or independent (falling into it, GO TO, PERFORM).
- R2, p. 318: only when control comes from another independent segment.
- P1: the return at a PERFORM's end, to a statement in an independent segment, from a range in another
  segment, is such a transfer and resets it. P2: it is not (the segment never gave up control; the
  "explicit transfer ... with a PERFORM" is the entry into the range).

Predicted: R1 and P2. Under them, the Java lane passes every SG test of the programs it can run (SORT,
in SG104A, SG105A, SG106A and SG204A, waits for phase 4.2), including those cobc -std=ibm fails (in
SG102A, SG103A, SG201A, SG203A), where a failure that R1 and P2 explain is a cobc departure. A test that
fails under R1 and passes under R2, or needs P1, refutes the prediction, and is recorded as such.

- LR p. 347 (Altered GO TO): "The altered GO TO statement transfers control to the first statement of
  the paragraph named in the ALTER statement." "An ALTER statement referring to the paragraph that
  contains the altered GO TO statement should be executed before the GO TO statement is executed.
  Otherwise, the GO TO statement acts like a CONTINUE statement." Format 3: `paragraph-name. GO TO.`

Result (J-175): R1 held, R2 refuted; P1 and P2 not told apart. Under R1 and P2 the Java lane passes
every test of SG102A, SG103A, SG201A and SG203A, 31 of which cobc fails (D-031); under R2 it fails
exactly what cobc fails in SG102A, SG201A and SG203A, and 2 of SG103A's 3. Under P1 it passes them all as
under P2, so no SG test returns from a PERFORM into an independent segment whose ALTERs it changed: P2
stays a reading. SG203A's SEGM-TEST-18 failed under every reading until the altered GO TO (p. 347), which
the IR had dropped, was built as a GO TO naming no procedure.

## 17. Debugging (read 2026-10-16, primary; NIST DB)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR).

- LR p. 122 (WITH DEBUGGING MODE): "Activates a compile-time switch for debugging lines written in the
  source text." "To specify a debugging line in your program, code a D in column 7 (indicator area)."
  "The presence or absence of the DEBUGGING MODE clause is logically determined after all COPY and
  REPLACE statements have been processed." "If a debugging line contains only spaces in Area A and in
  Area B, the debugging line is treated the same as a blank line."
- LR p. 760: "When WITH DEBUGGING MODE is specified, all debugging sections and debugging lines are
  compiled. When WITH DEBUGGING MODE is omitted, all debugging sections and debugging lines are treated
  as comments." "The USE FOR DEBUGGING declarative procedures are activated when DEBUG is in effect and
  inhibited when NODEBUG is in effect." "The debugging lines (lines with "D" or "d" in column 7) are not
  affected by the DEBUG or NODEBUG option; they are always active if they have been compiled."
- LR p. 759: "A debugging section executes only if both the compile-time switch and the object-time
  switch are activated." "A debugging section can be called, for example, by a PERFORM statement that
  causes repeated execution of a procedure. Any associated procedure-name debugging declarative section
  is executed once for each repetition."
- LR p. 707: "Automatic execution of a debugging section is not caused by a statement that appears in a
  debugging section." LR p. 708, Format 2: `USE FOR DEBUGGING ON procedure-name-1 ... | ALL PROCEDURES`,
  no other operand. Table 74: procedure-name-1, "Before each execution of the named procedure" and
  "After the execution of an ALTER statement referring to the named procedure"; ALL PROCEDURES, "Before
  each execution of each nondebugging procedure in the outermost program" and "After the execution of
  each ALTER statement in the outermost program (except ALTER statements in declarative procedures)".
- LR pp. 19-20 (DEBUG-ITEM): DEBUG-LINE X(6), DEBUG-NAME X(30), DEBUG-SUB-1 to -3 S9999 SIGN LEADING
  SEPARATE, DEBUG-CONTENTS X(n), a space between each. "Before each debugging section is executed,
  DEBUG-ITEM is filled with spaces." DEBUG-NAME: "The first 30 characters of the name that caused
  execution of the debugging section. Any qualifiers are separated by the word 'OF'." DEBUG-SUB-n:
  "Always set to spaces." Table 2: an ALTER, DEBUG-CONTENTS the TO PROCEED TO name; a GO TO, spaces; a
  PERFORM, "PERFORM LOOP"; a USE procedure, "USE PROCEDURE"; falling in from the procedure before,
  "FALL THROUGH"; the first nondeclarative procedure, "START PROGRAM"; DEBUG-NAME the procedure-name.

What IBM has is procedure debugging alone: the 85 Standard's USE FOR DEBUGGING ON a data-name, file-name
or cd-name (NIST DB201A, DB202A, DB203A, DB205A, DB303M, DB304M) is not IBM syntax, and the lowering
refuses it with that reason. DEBUG-LINE is "the source-statement sequence number (or the
compiler-generated sequence number, depending on the compiler option chosen)": NIST reads it only by
inspection (INSPT), never as a pass or a fail; Katana writes the source line of the statement, a reading.

Predicted, before the run: with procedure debugging lowered as above (the declarative performed before
each entry to the procedure it names, after an ALTER of it, the DEBUG-ITEM filled per Table 2; run only
when WITH DEBUGGING MODE is written and the object-time switch is on, the lane's COB_SET_DEBUG standing
for IBM's DEBUG runtime option; debugging lines code only with WITH DEBUGGING MODE), DB101A, DB102A,
DB103M (both runs) and DB105A report what cobc reports. The rival for DEBUG-NAME: a paragraph named in a
section is its name alone (predicted: the name as the program's reference wrote it, qualified only when
the reference was), against the name with OF and its section always.
- LR p. 760 (Usage note): "If you include a COPY statement as a debugging line, the letter "D" must
  appear on the first line of the COPY statement. The compiler treats the copied text as the debugging
  line or lines. The COPY statement is executed, regardless of whether WITH DEBUGGING MODE is specified
  or not."

Result (J-176): the prediction held. DB101A, DB102A, DB103M (both runs) and DB105A report what cobc
reports, and DB301M to DB305M with them: DB 10 of 16. DEBUG-NAME is the procedure's name alone; no test
reads a qualifier. One reading was refuted on the way: a section's debugging section run whenever its
first paragraph is entered from outside it failed 6 of DB105A's tests, which PERFORM a section's only
paragraph by its own name and expect the paragraph's entry alone. A section is executed when a PERFORM
or GO TO names it, when control falls through its header, or at the run's start (`Debug.section`).
DB103M, without WITH DEBUGGING MODE, ran its debugging lines as code until they were comments; SM101A's
`D` line `COPY K1SEA.` copies debugging text, the usage note above.

## 18. Obsolete elements (read 2026-10-16, primary; NIST OB)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR).

- LR Preface pp. xxii-xxiii: the elements "categorized as obsolete by the 85 COBOL Standard", among them
  the ALTER statement, the DATA RECORDS clause, the ENTER statement, "GO TO without a specified
  procedure-name", the LABEL RECORDS clause, the MEMORY SIZE clause, the MULTIPLE FILE TAPE clause,
  the "STOP literal format of the STOP statement" and the VALUE OF clause; "This does not imply that
  IBM will remove the 85 COBOL Standard obsolete elements from a future release of Enterprise COBOL."
- LR p. 698: "The ENTER statement is syntax checked but has no effect on the execution of the program."
- LR p. 158: MULTIPLE FILE TAPE: "This clause is syntax checked, but has no effect on the execution of the
  program."
- LR p. 188: LABEL RECORDS: "the LABEL RECORDS clause is syntax checked, but has no effect on the
  execution of the program." LR p. 189: VALUE OF: "The VALUE OF clause is syntax checked, but has no
  effect on the execution of the program." DATA RECORDS: "syntax checked but serves only as
  documentation".
- LR p. 457 (STOP): "When STOP literal is specified, the literal is communicated to the operator, and
  object program execution is suspended. Program execution is resumed only after operator
  intervention, and continues at the next executable statement in sequence."

A lane has no operator. The java lane reads STOP literal as the operator's reply given at once: the
literal is displayed and the run goes on at the next statement (LANE-14), as cobc's does with nothing to
answer it.

Predicted, before the run: every OB program reports what cobc reports, the elements above having no
effect or the effect already lowered (ALTER and the altered GO TO, J-175), OBNC1M's STOP-GF-9-TEST
failing in both lanes as RECONCILIATION records ("EXECUTION DID NOT HALT"). The rival: a program that
the Java lane cannot run for a reason no OB element holds (a CALL, OBIC1A).

Result (J-177): the prediction held where it could be tried. OBNC1M, OBNC2M and the four OBSQ programs
report what cobc reports: OB 6 of 7. OBIC1A, the rival, CALLs OBIC2A (phase 4.3). Two failures on the way
held no obsolete element: STOP literal was lowered as STOP RUN, which left OBNC1M's report unwritten; and
a GO TO in OBNC2M's QUAL-SECTION-2 PARA-6B, which an ALTER qualified IN that section changes, was not
read from the ALTER table, the lowering finding its paragraph by name and taking QUAL-SECTION-1's.

## 19. Report Writer (read 2026-10-16, primary; NIST RW)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR).

- LR p. 785 (Industry specifications): "The Report Writer optional module of the standard is supported with
  the optional IBM COBOL Report Writer Precompiler and Libraries (5798-DYR)." The modules the compiler
  supports itself are Intrinsic Functions, Debug (1 DEB) and Segmentation.
- LR p. 761 (Appendix E): "Words marked X under Standard only are 85 COBOL Standard reserved words for
  function not implemented in Enterprise COBOL. (Some of the function is implemented in the Report Writer
  Precompiler.) Use of these words as user-defined names is flagged with an S-level message." RD,
  REPORT, REPORTS, REPORTING, INITIATE, CONTROL and CONTROLS are marked Standard only (pp. 764-772);
  GENERATE is reserved (p. 768), for XML GENERATE.
- PG 6.4 p. 1022 lists the precompiler's own manuals (Programmer's Manual SC26-4301), outside the
  compiler's library.

So a program with a REPORT SECTION is not an Enterprise COBOL program: the compiler refuses it, and a site
that writes reports this way runs the precompiler first, whose output is the program Enterprise COBOL
compiles. Katana's input is what the compiler compiles; the REPORT SECTION and the REPORT clause are
findings (`sir/build/Build.java`, `sir/build/Files.java`), as the COMMUNICATION SECTION is. cobc
-std=ibm accepts Report Writer and passes the four RW programs that report tests, 40 of 40
(`oracle/nist/programs-ibm.golden`); RW301M and RW302M end at OPEN with status 35 in its golden.

Predicted, before the run: RW through the Java lane is 0 of 6. Each program's findings name its REPORT
SECTION and its REPORT clause as Report Writer, not in Enterprise COBOL, and no run reports what cobc's
does: the Report Writer statements (INITIATE, GENERATE, TERMINATE) are not lowered and fail where they
stand. The rival: a program the Java lane runs to cobc's report, which would mean the report file and the
tests' report are apart and the Report Writer statements are never reached, or an RW program with no
REPORT SECTION finding, which would mean the front end read the section as something else.

Result (J-178): the prediction was refuted in part. RW is 2 of 6, not 0: RW301M and RW302M end at the OPEN
of TFIL with status 35 in both lanes, before any Report Writer statement runs, so they report what cobc
does without trying Report Writer at all (the rival's first form). The other four each fail at their first
INITIATE, which is not lowered. All six programs' findings name the REPORT SECTION and the REPORT clause as
Report Writer, not in Enterprise COBOL; `NistJavaLaneIntegrationTest` holds that line for each. cobc
accepting Report Writer is not counted a discovery: a site with the precompiler gets the report too.

## 20. Relative files (read 2026-10-16, primary; NIST RL)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR).

- LR p. 147 (Relative organization): "Think of the file as a string of record areas, each of which contains
  a single record. Each record area is identified by a relative record number ... the first record area is
  addressed by relative record number 1 and the 10th is addressed by relative record number 10."
- LR p. 150 (ACCESS MODE): "In the sequential access mode, the sequence in which records are accessed is
  the ascending order of the relative record numbers of all records that exist within the file. In the
  random access mode ... A specific record is accessed by placing its relative record number in the
  RELATIVE KEY data item ... In the dynamic access mode, you can change as needed from sequential access
  to random access."
- LR p. 152 (RELATIVE KEY): "If a value is placed in data-name-4, and a START statement is not executed,
  the value is ignored and processing begins with the first record in the file."
- LR pp. 300-302 (Table 34): 14, "A sequential READ statement was attempted for a relative file, and the
  number of significant digits in the relative record number was larger than the size of the relative key
  data item"; 22, "An attempt was made to write a record that would create a duplicate key in a relative
  file"; 23, "An attempt was made to randomly access a record that does not exist in the file. Or a START or
  random READ statement was attempted on an optional input file that was unavailable"; 24, "An attempt was
  made to write beyond the externally defined boundaries of a relative or indexed file. Or a sequential
  WRITE statement was attempted for a relative file and the number of significant digits in the relative
  record number was larger than the size of the relative key data item"; 43, "For relative and indexed
  files in the sequential access mode, the last input-output statement executed for the file prior to the
  execution of a DELETE or REWRITE statement was not a successfully executed READ statement."
- LR pp. 303-304 (Invalid key condition): the status is set; "If the INVALID KEY phrase is specified ...
  Any EXCEPTION/ERROR declarative procedure specified for this file is not executed"; without it, "an
  applicable EXCEPTION/ERROR procedure ... is executed. The NOT INVALID KEY phrase, if specified, is
  ignored."
- LR p. 332 (DELETE): "the associated file must be open in I-O mode"; sequential access: "the previous
  input/output statement must be a successfully executed READ statement ... the system removes the record
  that was retrieved by that READ statement"; random or dynamic: the record "identified by the contents of
  ... the RELATIVE KEY data item ... If the file does not contain such a record, an INVALID KEY condition
  exists." "The file position indicator is not affected by execution of the DELETE statement."
- LR p. 410 (OPEN): "Execution of an OPEN INPUT or OPEN I-O statement sets the file position indicator ...
  For sequential and relative files, to 1." EXTEND "positions the file immediately after the last record
  ... (The record with the highest ... relative key value for relative files is considered the last
  record.)" LR p. 412, Table 50: which statement each access mode allows in each open mode (WRITE in I-O
  only for random and dynamic; START only in INPUT and I-O; DELETE and REWRITE only in I-O).
- LR pp. 427-428 (READ): sequential, "the next logical record in the key sequence ... ascending values of
  relative record numbers for records that exist in the file"; "If the RELATIVE KEY clause is specified
  for this file, READ statement execution updates the RELATIVE KEY data item"; random, "sets the file
  position indicator pointer to the record whose relative record number is contained in the RELATIVE KEY
  data item, and makes that record available. If the file does not contain such a record, the INVALID KEY
  condition exists"; dynamic, "the NEXT phrase must be specified for sequential record retrieval".
- LR p. 434 (REWRITE, relative): "The number of character positions in record-name-1 can be different from
  the number of character positions in the record being replaced"; random or dynamic, "the record to be
  replaced is specified in the RELATIVE KEY data item. If the file does not contain the record specified,
  an invalid key condition exists".
- LR p. 456 (START, relative): "the key data item used in the comparison is the RELATIVE KEY data item.
  Numeric comparison rules apply. The file position indicator points to the logical record in the file
  whose key satisfies the specified comparison."
- LR p. 478 (WRITE for relative files): sequential access, "The first record released has relative record
  number 1, the second ... 2 ... the relative record number of the record just released is placed in the
  RELATIVE KEY"; random or dynamic, "the RELATIVE KEY must contain the required relative record number ...
  this record is placed at the specified relative record number position in the file"; p. 475, an invalid
  key on WRITE when "the RELATIVE KEY data item specifies a record that already exists in the file".

What IBM's text leaves open, and the 85 Standard settles: where a sequential READ goes after a random READ
or a START. The Standard's file position indicator points *at* a record after OPEN or START (the next READ
returns that record) and *past* a record after a READ (the next READ returns the one after); the text above
is read that way.

Predicted, before the run. As lowered today, every file is a sequential file, so a relative file's random
READ, WRITE at a key, DELETE and START are not lowered or wrong: at most the programs that use sequential
access alone and neither DELETE nor START can report what cobc does. With a relative file in `rt` (record
areas by relative record number, the statuses above, the INVALID KEY phrases and the position indicator as
the Standard reads it), every one of the 35 RL programs reports what cobc -std=ibm reports, RL401M's
abnormal end (status 38 on REL1) included. The rivals: a program where cobc's position indicator after an
unsuccessful READ or START departs from the Standard's (both lanes then fail tests, differently); and the
key-size statuses 14 and 24, where cobc may not check the RELATIVE KEY's digits as IBM does.

Result (J-180): the prediction held for the runtime, and its bound on the baseline was refuted. As lowered
before, with every file sequential, RL was already 13 of 35, not "at most the programs that use sequential
access alone": RL119A (random), RL201A and RL204A (dynamic) read their keys in ascending order, which a
sequential file gives too. With `rt/RelFile`, 34 of 35 at once; RL206A's file is RECORD IS VARYING IN SIZE
FROM 120 TO 140 DEPENDING ON WRK-SIZE, and with `rt/Varying` giving a WRITE its length and a READ its
DEPENDING ON, 35 of 35, RL401M's abnormal end included. Neither rival was met: no RL test separates cobc's
position after an unsuccessful READ or START from the Standard's, and none writes or reads a record number
wider than its RELATIVE KEY.

## 21. Indexed files (read 2026-10-16, primary; NIST IX)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR).

- LR pp. 146-147 (Indexed organization): "Each record in an indexed file must have an embedded prime key
  data item ... the value in each prime key data item must be unique and must not be changed when the
  record is updated ... each record in an indexed file can contain one or more embedded alternate key data
  items ... The key used for any specific input-output request is known as the key of reference."
- LR p. 150 (ACCESS MODE, indexed): sequential access is "the ascending order of the record key value. The
  order of retrieval within a set of records that have duplicate alternate record key values is the order
  in which records were written into the set"; random access, "If a set of records has duplicate alternate
  record key values, only the first record written is available."
- LR pp. 150-152 (RECORD KEY, ALTERNATE RECORD KEY): "Regardless of the category of the key data item, the
  key is treated as an alphanumeric item. The collation order of the key is determined by the item's binary
  value order"; "The identical character positions referenced by data-name-2 in any one record description
  entry are implicitly referenced as keys for all other record description entries for that file";
  without DUPLICATES an alternate key's values "must be unique among records in the file".
- LR pp. 300-301 (Table 34): 02, "applies only to indexed files with alternate keys that allow duplicates
  ... For a READ statement, the key value for the current key of reference was equal to the value of the
  same key in the next record within the current key of reference. For a REWRITE or WRITE statement, the
  record just written created a duplicate key value for at least one alternate record key for which
  duplicates are allowed"; 21, "A sequence error exists for a sequentially accessed indexed file. The
  prime record key value was changed by the program between the successful execution of a READ statement
  and the execution of the next REWRITE statement for that file. Or the ascending requirements for
  successive record key values were violated"; 22, "an attempt was made to write or rewrite a record that
  would create a duplicate prime record key or a duplicate alternate record key without the DUPLICATES
  phrase"; 39, "a conflict was detected between the fixed file attributes and the attributes specified for
  that file in the program. These attributes include the organization of the file ..., the prime record
  key, the alternate record keys, the code set, the maximum record size, the record type ... and the
  blocking factor."
- LR pp. 427-428 (READ): sequential, "the key sequence is the sequence of ascending values of the current
  key of reference"; "When an ALTERNATE RECORD KEY with DUPLICATES is the key of reference, file records
  with duplicate key values are made available in the order in which they were placed in the file"; random,
  "the value of the key of reference [is] compared with the value of the corresponding key data item in
  the file records, until the first record having an equal value is found ... If the KEY phrase is not
  specified, the prime RECORD KEY becomes the key of reference ... When dynamic access is specified, this
  key of reference is used for subsequent executions of sequential READ statements".
- LR p. 433 (REWRITE, indexed): sequential access, the record's prime key "must equal the value of the
  prime record key data item in the last record read from this file"; random or dynamic, the record is the
  one with the prime RECORD KEY's value; "Values of ALTERNATE RECORD KEY data items in the rewritten record
  can differ from those in the record being replaced."
- LR pp. 455-456 (START): "When the KEY phrase is not specified, KEY IS EQUAL (to the prime record key) is
  implied"; data-name-1 is the prime key, an alternate key, or an item "whose leftmost character position
  corresponds to the leftmost character position of that record key ... less than or equal to the length
  of the record key"; "If the operands in the comparison are of unequal lengths, the comparison proceeds as
  if the longer field were truncated on the right to the length of the shorter field ... the PROGRAM
  COLLATING SEQUENCE clause, if specified, has no effect"; it becomes the key of reference; "If the
  comparison is not satisfied by any record in the file, an invalid key condition exists; the position of
  the file position indicator is undefined".
- LR p. 477 (WRITE for indexed files): "When ACCESS IS SEQUENTIAL is specified ... records must be released
  in ascending order of RECORD KEY values"; with DUPLICATES "the system stores the records so that later
  sequential access to the records allows retrieval in the same order in which they were stored". p. 475:
  an invalid key when, with sequential access and OPEN OUTPUT, "the value of the prime record key is not
  greater than that of the previous record", or the prime key "equals that of an already existing record".

Predicted, before the run. As lowered today an indexed file is a sequential file, and START and DELETE are
lowered for a relative file only (J-180), so most IX programs fail to compile or read the wrong record:
fewer than ten of 42 runs report what cobc does. With an indexed file in `rt` (records by prime key in
binary order, each alternate key an index with duplicates in the order written, the key of reference set by
READ KEY and START and kept for READ NEXT, START's truncated comparison, the statuses above, the
attributes kept with the file for 39) every one of the 42 runs reports what cobc -std=ibm reports, IX301M's
and IX401M's abnormal ends included. The rivals: status 02, which cobc's indexed handler may not give
where IBM does (on a READ whose next record has the same alternate key, on a WRITE or REWRITE that makes a
duplicate); where the file points after an unsuccessful START (undefined); and the order of duplicates
after a REWRITE that leaves an alternate key unchanged.

Result (J-182): the prediction held for the runtime, and its bound on the baseline was refuted again. As
lowered before, IX was 14 of 42, not fewer than ten: an indexed file read in prime-key order as it was
written passes a sequential file's test. With `rt/IdxFile`, 39 of 42 at once. The other three held nothing
indexed: IX110A declares `01 STATUS-TEST-10 PIC P VALUE ZERO`, a picture of a scaling position alone, and
the runtime wrote its zero digits with a format that has no width (LR p. 210: the value is zero in place of
the P); IX205A and IX206A test I-O-CONTROL's SAME RECORD AREA and SAME AREA, which the IR did not carry
(LR pp. 156-157: "For VSAM files, the SAME clause is treated as if equivalent to the SAME RECORD AREA
clause"). With both, 42 of 42, IX301M's and IX401M's abnormal ends (35 and 39) included. Of the rivals,
none was met where cobc and the lane could differ: status 02 on READ and on WRITE agreed with cobc's
reports, no test reads after an unsuccessful START, and none rewrites a duplicate's alternate key.

## 22. CardDemo's KSDS jobs through the Java lane, and CEE3ABD (read 2026-10-16, primary; phase 4.1)

Verbatim from z/OS 3.1 Language Environment Programming Reference, p. 111 (CEE3ABD):

- "CEE3ABD requests that Language Environment terminate the enclave with an abend ... There is no return
  from this service, nor is there any condition associated with it." `CEE3ABD (abcode, clean-up)`:
  abcode "A fullword integer, no greater than 4095, specifying the abend code that is issued".
- clean-up 0: "Issue the abend without clean-up ... termination activities are not performed"; 1: "Issue
  the abend with normal enclave termination processing"; "If an illegal value for clean-up is passed, the
  abend is issued without clean-up." Usage note 2: "When TRAP(OFF) is specified, CEE3ABD behaves in a
  similar manner to clean-up 0."

CardDemo's batch programs call it in one place each, their abend paragraph, with clean-up (TIMING) 0 and
ABCODE 999 (CBTRN02C lines 709-711). The cobc lane's stub (`oracle/lane/Generated.cee3abd`) prints
`CEE3ABD abend U0999` and ends the run with the code; the Java lane's `rt/Abend` prints the same line
(LANE-08). Without clean-up IBM closes no file: what a program wrote to a QSAM file before the abend may be
in a buffer never written. No CardDemo run in the goldens reaches the paragraph.

The KSDS semantics are §21's (an indexed file); what is new is the dataset: the cobc lane loads each KSDS
from its records in key order and unloads it to its records in key order (R-025, logical unload), through
GnuCOBOL's own indexed file. The Java lane's dataset is `rt/IdxFile`'s (LANE-15), loaded and unloaded the
same way, by the same records.

Predicted, before the run. With the Java lane taking indexed datasets through `rt/IdxFile` and CALL
'CEE3ABD' lowered as LE's service, POSTTRAN (CBTRN02C), INTCALC (CBACT04C) and TRANREPT-STUDY (IDCAMS,
SORT, CBTRN03C) report in the Java lane what the cobc lane's golden holds: every step's return code, every
stdout's lines and hash, every dataset's records and hash, the KSDS files unloaded byte for byte. So do the
fixtures with no program step (COMBTRAN, PRTCATBL, PRTCATBL-INLINE, SORTTEST) and TRANREPT, which stops at
its IDCAMS card in both lanes. CREASTMT stays refused: CBSTM03A CALLs the module CBSTM03B (phase 4.3). The
rivals: a program's FD record shorter or longer than its dataset's LRECL, which `IdxFile` reads as a
conflict of attributes (39) where GnuCOBOL may not; and a numeric edit, a DISPLAY or a date in the three
programs that the NIST modules never exercised.

Result (J-183): the prediction held for the jobs and was refuted on the way in three places, each a gap the
NIST modules never showed. (1) The rival named last was met, and larger than named: CardDemo's ASCII data
holds its signs as EBCDIC overpunch translated byte for byte (`0000009303L`, X'D3', -930.33), which cobc
-std=ibm reads as the digit 0, positive, measured over every printable byte, rewriting the byte as `0` in
the record when it reads it. The cobc goldens had posted five returns as purchases over their limit. On
Ghost's decision the fixtures say so (`signs=`, FIXTURE-10, R-061) and the lane loads those bytes in its own
form; both lanes then read IBM's values: POSTTRAN posts 262, not 257, the study's frozen unloads (F-02) no
longer the oracle. (2) The Java runtime read the machine's clock, not the lane's (LANE-03), returned no
RETURN-CODE (it started as the spaces of a new record, 8224, exit 32), and gave a PROCEDURE DIVISION USING
no PARM; LANE-17. (3) CBACT04C declares its cross-reference's alternate key without DUPLICATES over an
index defined NONUNIQUEKEY, which `IdxFile` read as 39; IBM's list of 39's attributes does not say
DUPLICATES is one, and CardDemo opens it on z/OS, so it is not (an `// Unsure`). The rival of the FD's
LRECL was not met. TRANREPT-STUDY's stdout differs only as D-005 says (cobc's separate trailing sign
where DISPSIGN(COMPAT) shows the bytes), held line by line. With those: POSTTRAN, INTCALC, TRANREPT,
TRANREPT-STUDY, PRTCATBL, PRTCATBL-INLINE, COMBTRAN and SORTTEST report in the Java lane what the cobc
lane's golden holds; CREASTMT is refused for CBSTM03B.

## 23. SORT and MERGE (read 2026-10-16, primary; NIST ST)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR).

- LR p. 451 (SORT, DUPLICATES): "If the DUPLICATES phrase is specified, and the contents of all the key
  elements associated with one record are equal ... the order of return of these records is ... The order
  of the associated input files as specified in the SORT statement. Within a given file the order is that
  in which the records are accessed from that file. The order in which these records are released by an
  input procedure ... If the DUPLICATES phrase is not specified, the order of these records is undefined."
- LR pp. 450-452 (keys and sequence): the keys' significance is their order in the statement; ASCENDING
  "from the lowest key value to the highest", DESCENDING the reverse; alphanumeric keys by the COLLATING
  SEQUENCE phrase, else "the PROGRAM COLLATING SEQUENCE clause (if specified) ... When both ... are
  omitted, the EBCDIC collating sequence is used"; other keys "according to the rules for comparison of
  operands in a relation condition".
- LR p. 452 (USING): "all the records in file-name-2, ... are transferred automatically to file-name-1. At
  the time the SORT statement is executed, these files must not be open. The compiler opens, reads, makes
  records available, and closes these files automatically." (GIVING): "all the sorted records in
  file-name-1 are automatically transferred to the output files"; p. 453, each output file is opened as
  OUTPUT, written "as if a WRITE statement without any optional phrases had been executed", and closed.
  (INPUT PROCEDURE): "control is passed to the input procedure before the file referenced by file-name-1 is
  sequenced ... When control passes the last statement in the input procedure, the records that have been
  released ... are sorted."
- LR p. 429 (RELEASE): "the current contents of record-name-1 are placed in the sort file"; FROM is
  "MOVE identifier-1 to record-name-1. RELEASE record-name-1." LR p. 430 (RETURN): "the next record from
  file-name-1 is made available for processing by the OUTPUT PROCEDURE"; AT END when there is none.
- LR p. 396 (MERGE): it "combines two or more identically sequenced files ... and makes records available
  in merged order to an output procedure or output file", by the same keys and COLLATING SEQUENCE rules.

The lane's reading: alphanumeric keys compare in the codepage's own order unless a COLLATING SEQUENCE
phrase or PROGRAM COLLATING SEQUENCE says otherwise, as relation conditions do (`rt/Collation`); under IBM's
codepage that is EBCDIC's, IBM's default. Katana's sort is stable (R-026), which is the DUPLICATES order
whether or not the phrase is written; MERGE takes equal keys in the order of the USING files.

Predicted, before the run. As lowered today SORT, MERGE, RELEASE and RETURN are not lowered and an SD is
declared as a sequential file, so no ST program with a SORT or MERGE reports what cobc does. With a sort
file in `rt` (records collected by USING or RELEASE, sorted stably by the keys, given to GIVING files or to
RETURN), every ST run that cobc compiles reports what cobc -std=ibm reports, ST301M's abnormal end (status
30 on SORT1) included: 34 of 40. The six cobc refuses (ST115A, ST137A, ST139A, ST140A, ST144A, ST147A)
cannot be the same. The rivals: a key compared differently from cobc (a numeric key of another usage, a
signed key, a COLLATING SEQUENCE alphabet), and the order of equal keys where a program relies on it
without DUPLICATES, which IBM leaves undefined.

Result (J-184): the prediction's count was refuted upward, and one rival was met where it was not named.
Before the runtime, ST was 4 of 40 (the four never reach a SORT). With `rt/SortFile` and `lower/SortVerbs`,
29 at once; then 32, with three faults of the lowering's own: a SAME RECORD AREA group took its storage from
a record declared later (J-182's, ST131A's NullPointerException); a USING record was moved into the SD's first
01, shorter than its record area, so a 100-byte record lost its last 50 bytes; and the SD's length was its
first 01's. The six "cobc refuses" were not cobc's to refuse: each names an item CHAR, which cobc -std=ibm
calls "a reserved word, but isn't supported", where IBM's reserved words (LR 6.4 Appendix E, Table 83, pp.
762-763) have CH, CHARACTER and CHARACTERS and not CHAR: D-032. Five of them pass every test in the Java
lane, and ST117A, which checks the file ST115A writes, passes the test cobc fails for want of that file:
32 the same and 6 more where cobc departs, 38 of 40. ST115A writes a file and no report, so the lane cannot
count it; ST301M, a flagging test whose PERFORM runs a RELEASE and a RETURN outside any SORT, ends in cobc
on status 30 for its own sort work file, which IBM's text leaves undefined. No key was compared differently
from cobc, and no program relied on the order of equal keys without DUPLICATES.

## 24. CALL, CANCEL and nested programs (read 2026-10-16, primary; NIST IC, phase 4.3)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR) and Programming Guide (PG).

- LR p. 318 (CALL): "The CALL statement transfers control from one program to another within a run unit."
  LR p. 320 (USING): "This correspondence is positional." BY REFERENCE: "the corresponding data item in the
  calling program occupies the same storage area as the data item in the called program." LR p. 321 (BY
  CONTENT): "Changes to the parameter in the called program do not affect the corresponding argument in the
  calling program"; for an alphanumeric literal "the called subprogram should describe the parameter as PIC
  X(n) USAGE DISPLAY, where n is the number of characters in the literal."
- LR p. 323 (ON EXCEPTION): "An exception condition occurs ... When the called subprogram cannot be made
  available"; with the phrase, control goes to imperative-statement-1, and without it "the NOT ON
  EXCEPTION phrase, if specified, is ignored." LR p. 324: "The ON OVERFLOW phrase has the same effect as the
  ON EXCEPTION phrase."
- LR p. 343 (EXIT PROGRAM): under an active CALL, control returns after the CALL, and "The state of the
  called program or method is not altered except that the ends of the ranges of all executed PERFORM
  statements are considered to have been reached"; "If control reaches an EXIT PROGRAM statement, and no
  CALL statement is active, control passes through the exit point to the next executable statement";
  "When there is no next executable statement in a called program, an implicit EXIT PROGRAM statement is
  executed."
- LR p. 103 (INITIAL): a program is in its initial state "The first time the program is called in a run
  unit", "Every time the program is called, if it possesses the initial attribute", and "The first time the
  program is called after the execution of a CANCEL statement that references the program"; in it "If a
  VALUE clause is used in the description of the data item, the data item is initialized to the defined
  value", "Files with internal file connectors ... are not in the open mode", and PERFORMs and altered GO
  TOs are reset. LR p. 343: EXIT PROGRAM in an INITIAL program "is equivalent also to executing a CANCEL".
- LR p. 326 (CANCEL): it "ensures that the referenced subprogram is entered in initial state the next time
  that it is called"; p. 327: it "closes all open files that are associated with an internal file connector
  in the program", and "No action is taken when a CANCEL statement is executed if the specified program:
  Has not been dynamically called in this run unit by another COBOL program; Has been called and
  subsequently canceled."
- PG p. 366 (DYNAM): "Use DYNAM to cause nonnested, separately compiled programs invoked through the CALL
  literal statement to be loaded for CALL, and deleted for CANCEL, dynamically at run time"; "CALL
  identifier statements always result in a runtime load of the target program and are not affected by this
  option"; "Default is: NODYNAM". PG p. 552: under NODYNAM, "with the CALL literal statement, the called
  subprogram will be link-edited with the main program into one program object. The CALL identifier
  statement results in the dynamic invocation of a separate program object. When a dynamic CALL statement
  and a static CALL statement to the same subprogram are issued within one program, a second copy of the
  subprogram is loaded into storage. Because this arrangement does not guarantee that the subprogram will
  be left in its last-used state, results can be unpredictable." PG p. 554: "When control is transferred the
  second time from the statically linked program, SUBPROG is made available in its last-used state".
- LR pp. 86-87 (program-names): a program directly contained in another "can be referenced only by
  statements included in that containing program", a COMMON one also by its siblings and their contents;
  a CALL names a program "contained within the program that includes the CALL statement" before any other.
  LR p. 197 (GLOBAL): "a data-name is available to every program contained within the program that defines
  it, as long as the contained program does not itself have a definition for that name." (EXTERNAL): "the
  storage associated with a data item is associated with the run unit rather than with any particular
  program ... In a run unit, there is only one representative of an external data item."

The lane's reading. A run unit holds each called program once per way it was reached: a nested program
in its containing program; a separately compiled program called by a literal under NODYNAM in the one
program object (static); one called by an identifier, or by a literal under DYNAM, as a separate dynamic
copy, which CANCEL alone resets. Under the ibm profile's NODYNAM, a program reached both ways has two
copies, as PG p. 552 says, each in its own last-used state. ibm.profile gains `ibm.dynam NODYNAM`, IBM's
default (R-049), as the options CardDemo states none of. A BY REFERENCE argument is the caller's storage;
a BY CONTENT one a copy made at the CALL; a LINKAGE record with no argument stays unaddressed. EXIT
PROGRAM and GOBACK return from a called program, its PERFORM ranges ended; STOP RUN ends the run unit;
EXIT PROGRAM in the main program passes on. A program that cannot be found raises the exception
condition; without ON EXCEPTION the run ends abnormally.

Predicted, before the run. As lowered today the Java lane builds no called module, so every IC main
program fails: 0 of 25 the same as cobc -std=ibm. With CALL lowered (a run unit in `rt`, LINKAGE bound
to the arguments, CANCEL, INITIAL, nested programs, GLOBAL and EXTERNAL items) and the lane run under
DYNAM, the 24 runs that leave a report report what cobc does; IC401M, a flagging test that cobc ends
without one (exit 1), cannot be compared. Under the profile's NODYNAM the static and dynamic copies part
where a test reads a subprogram's state back across both kinds of CALL: IC203A 17 of 21 (CNCL-TEST-01's
three checks, where CANCEL "IC204A" leaves the static copy in its last-used state, and CNCL-TEST-05, where
IC205A's CANCEL "IC206A" does nothing), and IC201A, IC223A and IC225A each fail the checks that read
IC202A's or the -1 program's call counter (expected 5, 6, 7, CALL-COUNT): 20 of 25 the same, the other
five a discovery against cobc, which calls dynamically. OB's OBIC1A, which CALLs OBIC2A and OBIC3A,
reports what cobc does (OB 7 of 7). CardDemo's CREASTMT passes its CALLs to CBSTM03B and stops at
CBSTM03A's POINTER to the PSA, which phase 4.4's z/OS storage is for. The rivals: a LINKAGE item bound at
the wrong place in its argument; a nested program's GLOBAL reference, IC233A's and IC234A's GLOBAL files
and USE GLOBAL declaratives, and IC227A's EXTERNAL file, any of which may be left a finding.

Result (J-186): the prediction held where it named numbers, and its rivals split. Before the run IC was 0 of
25. With CALL lowered, under DYNAM 22 of 25 report what cobc does; under the profile's NODYNAM 18, and the
four that part are the four named, each failing only checks that read a subprogram's state across a static
and a dynamic CALL: IC203A 17 of 21, as counted; IC201A and IC223A 1 of 11, IC225A 33 of 36 (D-033). The
24 the prediction gave DYNAM were 22: IC233A and IC234A open, read and USE GLOBAL a containing program's
GLOBAL file, which is not lowered and is now a finding (it was a lowering crash on the first run). IC401M
ends at its CALL of FIC401M, a program that does not exist, with abend U4038 where cobc exits 1; neither
leaves a report. GLOBAL items, EXTERNAL records, IC227A's EXTERNAL file, INITIAL and nested programs needed
nothing past the first build. OBIC1A reports what cobc does: OB 7 of 7. CREASTMT was refuted in part:
CBSTM03A stops at its SET ADDRESS OF the PSA (line 266) before its first CALL, so CBSTM03B is lowered and
never reached; the steps before STEP040 match the golden.

## 25. The z/OS storage facade: POINTER, SET ADDRESS OF, and the PSA, TCB and TIOT (read 2026-10-16; phase 4.4, R-027)

Verbatim from the Enterprise COBOL for z/OS 6.4 Language Reference (LR).

- LR p. 242 (POINTER): "A pointer data item is a 4-byte elementary item or an 8-byte elementary item
  depending on whether the LP(32) or LP(64) is in effect"; p. 243: "A pointer data item can be the subject
  or object of a REDEFINES clause", "A VALUE clause for a pointer data item can contain only NULL or NULLS".
- LR p. 17 (NULL): "Represents a value used to indicate that data items defined with USAGE POINTER ... do
  not contain a valid address ... NULL has the value zero."
- LR p. 19 (ADDRESS OF): "For 01 and 77 level items in the LINKAGE SECTION, the ADDRESS OF special register
  can be used as either a sending item or a receiving item ... The ADDRESS OF special register is implicitly
  defined as USAGE POINTER."
- LR p. 443 (SET, format 5): "the current value of the receiving field is replaced by the address value
  contained in the sending field"; for ADDRESS OF identifier-5, a LINKAGE 01 or 77, "The addresses of these
  items are set to the value of the operand specified in the TO phrase."
- LR p. 22 (LENGTH OF): "The LENGTH OF special register contains the number of bytes used by a data item."

Not read: IBM's MVS Data Areas (the PSA, TCB and TIOT mappings). This container cannot reach ibm.com,
publibfp.dhe.ibm.com or bitsavers.org, so what follows about the blocks is what CBSTM03A declares (lines
235-261, R-027 C-1) and recollection, each a hypothesis claim with its trial (R-063, on Ghost's call
"build it as hypothesis"):

- H1 (declared by CBSTM03A): the current TCB's address is the fullword at PSA+536 (X'218'); the TIOT's at
  TCB+12 (X'0C'); the TIOT begins with the job name, the step name and the procedure step name, 8 bytes
  each (24); each DD entry is 20 bytes, its length in its first byte and its DD name at +4, its UCB address
  in its last three; a fullword of zeros follows the last entry. Recalled: IBM's PSATOLD (X'21C') and
  PSATNEW (X'218') both hold the running TCB of a one-task step.
- H2 (recalled): the TIOT lists the step's DDs in the order the JCL codes them; a DD on a device (DASD,
  VSAM) has a UCB address; SYSOUT, an in-stream DD and DUMMY have none (zeros). The fixture leaves out the
  JCL's STEPLIB, a load library no lane reads, and whatever DDs JES adds; the facade lists the fixture's DDs.
- H3 (the program's expectation, wsinit NONE): PSAPTR, a POINTER without VALUE, starts as NULL, address
  zero, the PSA's; under R-057 the lane starts a pointer as INITIALIZE would leave it, which LR does not
  give for a pointer (its INITIALIZE rules name none), so the lane starts it NULL.

The lane's reading. A POINTER is 4 bytes holding an address big-endian, as BINARY is (so BUMP-TIOT, a
binary S9(8) that REDEFINES TIOT-INDEX, steps it). `rt/Storage` is the step's z/OS storage: the PSA at 0,
a TCB and a TIOT built from the fixture's job, step and DDs; SET ADDRESS OF a LINKAGE record binds it to
the storage at the pointer's address, as a CALL binds it to an argument (LANE-19). An address no block
holds is the run's abend (S0C4 on z/OS, recalled), never invented storage.

Predicted, before the run. CREASTMT's STEP040 prints, in its java lane stdout, `Running JCL : CREASTMT
Step STEP040 `, `DD Names from TIOT: `, then `: SYSPRINT --  null UCB` and `: SYSOUT   --  null UCB` from
the loop, the five DASD DDs `-- valid UCB` from TRNXFILE to STMTFILE, and HTMLFILE from the IF after the
loop (the loop's END-OF-TIOT reads the next entry's first fullword, so it stops one entry early). The step
then runs to its end, CBSTM03B doing its file access through LANE-19's CALL, writes STATEMNT.PS and
STATEMNT.HTML, and ends with return code 0: CREASTMT runs end to end in the java lane, R-027 C-2's trial.
The cobc lane is unchanged (abend SIGSEGV, R-027). The rivals: CBSTM03A's ALTERed GO TOs and CBSTM03B's
file access by a DD name it is passed; an OCCURS table of 51 cards of 10 transactions each overflowing;
a POINTER compared or moved where the lane holds bytes.

Result (J-187): the prediction held, line for line. CBSTM03A lowered with no finding once POINTER, SET
ADDRESS OF and LENGTH OF were; STEP040 printed the job, the step and the eight DDs as predicted, HTMLFILE
from the IF after the loop, ran to its end with return code 0, and wrote 1212 statement records and 6082
HTML records. With no cobc run to compare (it abends at the PSA), the statements were held to their own
input: 50 statements, one for each card of the cross-reference file, listing all 262 transactions of the
sorted file once each, and every Total EXP the sum of its lines. None of the rivals appeared: the ALTERed GO
TOs, CBSTM03B's file access by the DD name it is passed (through LANE-19's CALL) and the 51-by-10 table ran
as written. The blocks' layouts remain R-063's hypotheses.

## 26. The four jobs through both lanes (phase 4.5; no new IBM text)

4.5's check: POSTTRAN, TRANREPT, INTCALC and CREASTMT produce the same datasets on logical unload, SYSOUT
and return codes as the phase-1 goldens, the java lane consuming the same `DD_*` fixture with no
per-program adaptation. No IBM text is new here; what governs is R-027 (CREASTMT's STEP040 is outside the
cobc lane, its oracle the java lane), R-061 (the data's signs) and D-005 (DISPLAY's sign form).

Predicted, before the run. The java lane's setup today lowers each program by name, CBSTM03A and CBSTM03B
at CREASTMT's tab widths and the others at POSTTRAN's: a per-program adaptation. Lowering each job's
programs from its own fixture (its steps' PGMs and its modules, at its `tabs` widths) changes no line of
any job's report. POSTTRAN, TRANREPT and INTCALC (with TRANREPT-STUDY and the three other jobs) report what
the cobc golden holds, D-005's stdout lines aside, as they do since J-183. CREASTMT matches the golden
through STEP030; from STEP040 its golden is the java lane's own run, recorded beside the cobc golden
(`oracle/lane/carddemo-java.golden`), held to its input by `Statements` (J-187): what the golden catches is
change, what `Statements` catches is error. The rival: a job whose fixture names a program another job's
fixture compiles at other tab widths, which only CREASTMT's CUSTREC.cpy has.

Result (J-190): as predicted. With each job's programs lowered from its own fixture, every job's report is
line for line what it was; CREASTMT through STEP030 is the cobc golden's, and its STEP040 is recorded as
`oracle/lane/carddemo-java.golden`. The rival did not arise: every fixture names CUSTREC.cpy at the same
widths.

## 27. Proof report v1: the four jobs, the java lane against the cobc lane (phase 4.6; no new IBM text)

What 4.6 asks (docs/ROADMAP.md): the proof report on a real job, the coverage of CardDemo's paragraphs by
its fixtures, and the paragraphs no fixture entered, "the honest number a buyer needs". v0 (1.9) proved the
cobc lane against the study's frozen datasets; v1 proves what Katana makes, the java lane, against the cobc
lane, for each of the four jobs of 4.5, one report a fixture (PROOF-02): the gate (the cobc lane twice),
every dataset the job leaves compared field by field under the copybook its fixture names (raw where it
names none), the programs' findings, and each program's coverage. Coverage comes from a cobc trace; where
the cobc lane cannot run a step (CREASTMT's STEP040, R-027) it comes from the java lane's own trace, written
in cobc's trace format, which is held equal to the cobc trace on every program both lanes run.
`oracle/proof/CARDDEMO.coverage.md` sums the four: each program's paragraphs entered by any fixture, and
those none entered.

Predicted, before the run. POSTTRAN, TRANREPT and INTCALC: every dataset the same, the java lane's trace
the cobc trace's paragraphs exactly, CBTRN02C 24 of 26 as in v0 (9999-ABEND-PROGRAM and
9910-DISPLAY-IO-STATUS never entered: the fixture holds no I/O error). CREASTMT: every dataset through
STEP030 the same; STATEMNT.PS and STATEMNT.HTML have no expected side and the report says so, naming R-027,
R-063 and `Statements`; CBSTM03A's and CBSTM03B's coverage from the java lane, their abend and error
paragraphs not entered. Across CardDemo's five batch programs these fixtures run, more than four paragraphs
in five entered. The rivals: a paragraph cobc's trace names that the IR's graph does not (a section's
implicit paragraph), or the reverse; a dataset written in place (ACCTDATA, TCATBALF) compared under a
copybook whose signed fields the comparator reads otherwise than the lanes do.

Result (J-191): the prediction held but for its job list. POSTTRAN, INTCALC and CREASTMT went as predicted:
every dataset the same, the java lane's trace the cobc trace's paragraphs exactly, CBTRN02C 24 of 26. TRANREPT,
written as its JCL is, stops at its first step in both lanes (J-035), so it runs no program; its report says
so, and TRANREPT-STUDY, the run that reaches CBTRN03C, was added (24 of 26). CREASTMT's statements are listed
as not compared; CBSTM03A 24 of 25, CBSTM03B 14 of 14, from the java lane's trace. CardDemo: 106 of 113
paragraphs entered (94%), the seven left the abend and I/O-error paragraphs. Neither rival arose; TRANREPT's
report file, laid out by CVTRA07Y's seven record descriptions, is compared byte for byte, the report saying
why. On the way: cobc's `-ftrace` adds where a signal stopped the program to its stderr, so the trace check
leaves a signalled step's stderr out.

## 28. The Intent IR's types (phase 5.1; no IBM text: the facts are Katana's own)

What 5.1 asks (docs/ROADMAP.md 3.2 and phase 5): the fact kinds (`Money`, `Enum`, `Flag`, `Counter`,
`Date`, `TaggedUnion`, `Function` for a PERFORM range, `Reducible` for a paragraph set, `Stateless` for a
subprogram, `NeverOverflows` for an item), each with its evidence (the IR nodes and the rule that fired), a
confidence, and a state, inferred, confirmed or refuted, with who and when. The Semantic IR is what the
program does; a fact is what someone (a rule, then a person) says it means, so no IBM page settles one,
and the types hold the shape of a claim, not its truth: 5.2's rules make facts, 5.3's review file
confirms or refutes them, 6's passes act on them (R-029: never on a refuted fact; an unconfirmed one is
said so).

The design, as `sir/types` is built (records whose constructors refuse a violation, numbered invariants
held to a document by a test): a fact is its kind, its subject, its evidence, its confidence and its state.
The subject is what the fact is about, in one of five shapes, each kind taking one: an item (`Money`,
`Flag`, `Counter`, `Date`, `NeverOverflows`), an item and its condition-names (`Enum`, two or more), an
item and the items that REDEFINES it, with the item whose value says which is meant when there is one
(`TaggedUnion`), a range of paragraphs (`Function`, first THRU last as PERFORM names them), a set of
paragraphs (`Reducible`), and a program (`Stateless`). The evidence names its rule and its version and at
least one IR node: an item, a paragraph, a statement (paragraph and place) or an edge of the paragraph
graph. A fact is known by its kind and its subject, so a rule's next version re-inferring it is the same
fact, and a person's confirmation stays with it. A set of facts is one program's, under one profile hash,
and is checked against that program's IR: every node it names is in that program.

Predicted, before the build. Each invariant refuses a violation (one mutation each, as SIR-nn). The
facts a person writes for CardDemo's CBTRN02C by reading it, the trial of the shapes on a real program,
all fit and are accepted against its IR: `Money` for DALYTRAN-AMT, TRAN-CAT-BAL and ACCT-CURR-BAL (the
amounts it ADDs), `Enum` for APPL-RESULT (APPL-AOK, APPL-EOF), `Flag` for END-OF-FILE and
WS-CREATE-TRANCAT-REC, `Counter` for WS-TRANSACTION-COUNT and WS-REJECT-COUNT, `Date` for COBOL-TS and
DB2-FORMAT-TS, `Function` for 1500-A-LOOKUP-XREF and 9910-DISPLAY-IO-STATUS, `Reducible` for the six
open paragraphs; the same facts with one node renamed or moved are refused. CBTRN02C has no
`TaggedUnion` and no `Stateless` (it is a main program). The rivals: one of its meanings needs a shape the
five do not give. The likeliest is its two REDEFINES, TWO-BYTES-ALPHA over a binary and a FILLER over
DB2-FORMAT-TS: views of the same bytes, not a union with a tag, which no kind the roadmap names holds. If
so, a finding for 5.2 and Fable, not a kind added here (the kinds are the roadmap's).

Result (J-193): the prediction held. Every invariant refuses its violation; the thirteen facts written for
CBTRN02C (three `Money`, an `Enum`, two each of `Flag`, `Counter` and `Date`, two `Function`s and the
opens as `Reducible`) are accepted against its IR, and refused with a condition-name renamed, an item past
the data map, an alternative that does not redefine its item, or a range past the last paragraph. The
rival arose as foreseen, and only as a matter of meaning: the shapes accept a `TaggedUnion` of
TWO-BYTES-BINARY and TWO-BYTES-ALPHA, since the one REDEFINES the other, though it is a view of the same
bytes with no tag. The types hold a claim's shape, not its truth; a person refutes such a fact (R-029), and
whether a view is a kind of its own goes to Fable (HANDOFF). On the way: the IR's paragraph 0 is the
procedure's unnamed start, so a program's first named paragraph is 1.

## 29. The first inference rules, measured against a hand-written key (phase 5.2; no IBM text)

What 5.2 asks (docs/ROADMAP.md, phase 5): one file per rule, named, with its evidence and its precision
measured on the corpus against a hand-written key, a rule that lowers the recorded score refused. The first
three, as the roadmap words them: 88-levels with disjoint values are an enum; `V99 COMP-3` written by ADD or
SUBTRACT and never multiplied by itself is money; a PERFORM range entered only by PERFORM, leaving only at
its end, no ALTER target and not recursive, is a function, checked on the paragraph graph.

The key (`test/katana/intent/carddemo.key`) is CardDemo read by hand before any rule exists: its scope and
its judgments are written at its head. Enum: 200 items carry two or more condition-names; 118 are enums, 69
are flags in an enum's clothing (Y/N, 0/1, valid/invalid), 13 are neither (message texts, a day or a month
with named ranges). Money: 109 numeric items of scale 2, all money but one rate; 8 are COMP-3, and none of
those 8 is written by anything but MOVE (CBEXPORT and CBIMPORT's export records). Function: 175 PERFORM
ranges in the batch programs, every one a function by reading; the online programs' ranges are out of scope
until the CICS facade, since their main lines end in EXEC CICS RETURN, which the IR does not read.

The rules, version 1, as the roadmap words them, each its own file in `intent/rules`. Precision is the share
of a rule's facts in its scope that the key holds; recall the share of the key the rule finds. A rule's
confidence is never more than its recorded precision.

Predicted, before the rules. Enum: every multi-88 item but the few whose values overlap (ACUP- and
CCUP-CHANGE-ACTION, WS-EDIT-SELECT, the day and month numbers), so about 195 facts, about 115 true: precision
near 0.6, recall near 0.97; its errors are the flags, which disjointness cannot tell from an enum. Money: no
fact at all on CardDemo, so no precision and recall 0: CardDemo's money is DISPLAY, and its COMP-3 amounts
are only MOVEd. Function: all 175 batch ranges, precision and recall 1. The rivals: a range refused because
the IR's unnamed paragraph 0 or a main line ending in GOBACK reads as falling into it; CBSTM03A's ALTERed
GO TOs and CBSTM03B's GO TOs to their exits read as leaving a range. If money finds nothing, the finding is
the roadmap's evidence requirement, not the rule: COMP-3 is IBM's packed decimal, and CardDemo's amounts are
zoned; whether money's next version drops COMP-3 goes to Fable.

Result (J-195): as predicted for function and money, near it for enum, and the key itself corrected twice
by the first run. Enum: 187 facts, 110 true of 113, precision 0.588, recall 0.973; its 77 errors are 69
flags and 8 message or number fields, its misses the three items whose condition values overlap. Money: one
fact, CBSTM03A's WS-TOTAL-AMT, a COMP-3 statement total its program ADDs to, and true: precision 1.000,
recall 0.009 (1 of 116): CardDemo's money is zoned. Function: 175 of 175 once the rule read a paragraph's
end as a person does; its first run refused six, the rivals foreseen: CBSTM03A's 8599-EXIT ends
`GO TO 0000-START. EXIT.`, whose last statement falls though nothing reaches it, and CSUTLDTC's EXIT PROGRAM,
which the IR names `EXIT` with PROGRAM as its operand, so a reading of the verb alone never matched it (nor
does `Contract.falls`'s; EXIT PROGRAM in a program no CALL reached is passed over, so the contract's
reading is IBM's there by accident). The key: CBSTM03A was absent (the survey that listed the candidates
read CUSTREC.cpy at 8 columns and failed), and three items with one condition-name were counted as two;
both corrected, CBSTM03A's eight money items and WS-M03B-OPER judged by the key's written rules, and
WS-US-PHONE-AREA-CODE-TO-EDIT, which had entered the key unread, judged after the run (a code checked
against lists of valid ones, not an enum). The bench is held to these scores; what comes next for enum
(flags) and money (zoned amounts) goes to Fable.

## 30. The review file: a person's word on each fact, kept across a rebuild (phase 5.3; no IBM text)

What 5.3 asks (docs/ROADMAP.md, phase 5): how a person confirms or refutes facts, as a plain text file
beside the program, the person's words surviving a rebuild (Suite Builder's YOURS-block instinct), and a
refuted fact never re-inferred without a new rule version (R-029).

The design. `<PROGRAM>.review`, one line a fact, fields between ` | `: its state, its kind, its subject,
the rule and version that inferred it (`person v1` for a fact a person wrote), its confidence, and for a
confirmed or refuted fact who and when, then why if the person says. The subject is written by the
program's names, not the IR's numbers: an item by its name (qualified `OF` its parent where the name is
not unique), a condition-name list after a colon, a range `FIRST THRU LAST`, a set of paragraphs by name,
a program by its PROGRAM-ID. Numbers move when a line is added above them; names move only when a person
renames, and then the line no longer names anything. A person edits the state word and adds `who date`.

A rebuild merges the rules' new facts with the file: a fact the file confirms stays confirmed, whatever
version infers it again; a fact it refutes stays refuted while the same rule version infers it, and comes
back as inferred under a newer version, the old refutation written beside it; a fact the person wrote, or
confirmed or refuted, that no rule infers any more stays, the person's word kept; a line whose names the
program no longer holds is kept in the file as orphaned and reported, never dropped and never bound to
something else. The merged facts are checked against the IR (INT-07) as any are.

Predicted, before the build. On CBTRN02C, its rules' facts written to a review file, a person's edits
(money confirmed, an enum refuted as a flag, a fact of the person's own) survive a rebuild of the same
program unchanged, and a rebuild after a WORKING-STORAGE item is added above the others (every later item
renumbered): every line binds to the same names, the review file unchanged but for nothing. After a
paragraph the person confirmed as a function is renamed, its line is orphaned and reported, the rest kept.
A refuted enum re-inferred by enum.disjoint-88s v1 stays refuted; under a v2 it returns as inferred with
the refutation beside it. The rivals: a name the IR does not hold uniquely even qualified (FILLER, an item
of a copybook copied twice), which the writer cannot name and must say so, not guess; and condition-names
that are not unique across items, which a `Choice` names under its item, so they need no qualification.

Result (J-196): as predicted. On CBTRN02C a person's four words (a function and an enum confirmed, a
function refuted, a money fact of their own) survive a rebuild unchanged; with a WORKING-STORAGE item added
above the others the review file is byte for byte the same, every line bound to the same names; with
1500-A-LOOKUP-XREF renamed its confirmed line is kept as orphaned and reported, the renamed paragraph
inferred afresh, the other three words kept. A refuted enum stays refuted under its rule's v1 and returns as
inferred under a v2, the refutation beside it. The first rival arose only where foreseen: an enum on a FILLER
has no name, and the merge says so rather than write a line no person could edit; condition-names need no
qualification. Nothing else surprised.

## 31. The proof ledger: append-only lines over the Java a build makes (phase 5.4; no IBM text)

What 5.4 asks (docs/ROADMAP.md 3.2 and phase 5): the append-only store; the line schema (pass, fact, oracle
run, Java symbol, COBOL node, profile hash, time, supersedes); the provenance map writer, per Java method and
field what it stands on; the report writer. Its check: the ledger refuses an edit to an existing line; every
Java symbol in a build has at least one line; a symbol removed by a pass has a `superseded-by`. R-030, the
ledger append-only, is recorded here.

The design. `ledger/` may not read `lower/` (modules.json), so the lowering says what it wrote: a manifest,
each Java symbol it emitted that stands on a COBOL node, the class on its program, a field `f<id>` and a
record `r<id>` on their item, a stretch method `s<label>` on the paragraph it runs; the machinery (the
constructor, `run`, `step`, `main`) stands on its class's line. A build's literal lines are one a symbol,
pass `literal`, no fact, the oracle run that proved the Java (a proof report and its hash) or `-` where
none has, under the profile hash. A line is `seq | prev | pass | fact | state | run | symbol | node | profile
| time | supersedes`; `prev` is the hash of the line before, so an edit, a deletion, an insertion or a
reorder anywhere breaks the chain from there on and is refused by reading the file alone, without its
earlier copy. A pass's line that replaces a symbol supersedes the line it stands in for; a symbol is live
while its last line is not superseded. Against a build: every symbol of the manifest has a live line, and
every live line's symbol is in the build, so a symbol a pass removed without a superseding line is refused.
A pass acting on a fact writes the fact's id and whether a person confirmed it (R-029); a refuted fact
cannot appear.

Predicted, before the build. For the programs of the four proven jobs, the manifest names every `f`, `r` and
`s` member the Java text declares and nothing else, nested programs as `Outer.Inner`; each build's literal
ledger covers its manifest; changing one character of any line, deleting, inserting or swapping lines is
refused at that line; a pass simulated on CBTRN02C that removes a field is refused until a line supersedes
it, and accepted after; the provenance map names, for every symbol, the COBOL node and the run it stands
on. The rivals: a stretch label that is not a paragraph's own start (a continuation the dispatcher needs
inside a paragraph), whose node is the paragraph it continues; and a symbol the Java declares that no
COBOL node stands on, which belongs to its class's line, not a line of its own.

Result (J-197): as predicted, with one hole in the design found by its own test. CBTRN02C, CBTRN03C, CBACT04C,
CBSTM03A and CBSTM03B lower to 316, 324, 294, 300 and 70 symbols; each manifest is exactly the `f`, `r` and
`s` members the Java text declares, in order, and a nested program's are inside its container's class; each
literal ledger covers its build, reads back equal, and its provenance map names every symbol with its node and
its job's v1 report. A pass simulated on CBTRN02C that removes a field is refused until a line supersedes the
field's, and accepted after, the earlier ledger's lines unchanged in the later. The hole: a chain where each
line carries the hash of the one before leaves the last line unheld, and the edit test found it; the file now
ends with an `end` line carrying the last line's hash. Neither rival arose as a symbol without a node: a
stretch label inside a paragraph is opened while that paragraph is the current one and stands on it, and the
machinery stands on its class.

## 32. The rules' version 2: use as evidence (R-065; phase 5.2's answers, J-199)

R-065: a fact rule's evidence is what the program does with an item, its representation at most a
weight; FLAG runs before ENUM; each version adopted only if neither precision nor recall falls against
the key. Its trials: C-1, money's version 2 raises recall without lowering precision; C-2, a flag rule
before enum lifts enum's precision past 0.9.

The key first. Flags had no lines: version 1's key judged 69 two-condition items yes-or-no pairs, flags
not enums, without writing them as flags. Its scope for flags is now every item with one or two
condition-names, its reading written at its head (a yes-or-no, or the one condition-name naming the yes
of a switch); the 69, and 10 of the 33 one-condition items (the found-in-master and row-error switches,
the one-condition validity groups), are its 79 flags, judged before the rule.

The rules. flag.yes-no v1: two condition-names whose values, SPACE and LOW-VALUE set aside as the
nothing state, are a yes-or-no pair (Y and N, or 0 and 1), or one of which is only SPACE or LOW-VALUE; or
one condition-name, the item also compared with or set to SPACE or LOW-VALUE (R-065's second form).
enum.disjoint-88s v2: version 1 leaving every item flag takes. money.used v2: a numeric item of two
places an ADD, SUBTRACT or COMPUTE stores into with another two-place item among its operands, or one
MOVEd to an edited picture with a currency sign or comma insertion; never one it squares.

Predicted, before the rules. Flag: precision at least 0.9, recall near 0.85; it misses the 0 and 9 pair
(WS-CA-LAST-PAGE-DISPLAYED) and the one-condition switches no statement sets to SPACE or LOW-VALUE.
Enum v2: precision past 0.9 (C-2), recall unchanged at 0.973. Money v2: precision at least 0.95, recall
between 0.15 and 0.5 (C-1): the balances, totals and interest CardDemo adds and computes, and the amounts
CBTRN03C edits for its report, found; the record fields only read, written or moved, which are most of
the key's money, missed; DIS-INT-RATE, only multiplied, not taken. The rival for money: a two-place
item added to that is no amount (a percentage kept as a running sum), which precision would show.

Result (J-202): as predicted, every figure inside its range. flag.yes-no v1: 69 facts, 68 true of 79,
precision 0.986, recall 0.861; its one error a message field with a blank and one text, its misses the
0 and 9 pair and the one-condition switches no statement sets to SPACE or LOW-VALUE (they are set to '0'
and '1'). enum.disjoint-88s v2: 118 facts, 110 true, precision 0.932 (0.588 at v1), recall 0.973 kept:
C-2 verified. money.used v2: 29 facts, all true, precision 1.000, recall 0.250 (0.009 at v1): C-1
verified; DIS-INT-RATE not taken; the misses are the record fields only read, written or moved. Neither
version lowers a figure, so both are adopted (R-065); confidences 0.9, under each precision.

## 33. The first review file's second reading: tests and documents for what is under certainty (R-066; J-204)

R-066: a fact any reading puts under certainty, or the readings disagree on, gets a test or a document
before the person sees it, and the review file says which. A Fable advisor read every fact of
test/katana/intent/carddemo-batch.review and proposed a probability and a reason for each (a proposal,
R-031: test/katana/intent/carddemo-batch.proposals), naming a test or a document for each under 100%; three
document researchers searched CardDemo's corpus for what its authors wrote (ibm.com was refused by the
network, so no IBM page is quoted). The catalogue below is the advisor's, as built or named; each fact's
`tests:` line names its entries.

The proposals: Function 95 to 99% (the 12 abend paragraphs 95%: CALL CEE3ABD ends the job, so they never
return on the real system; CSUTLDTC's range 95%: a function only because the program is CALLed); Money 96
to 99%; Enum split: the M03B operation codes 95%, the CODATECN format codes 85%, FEEDBACK-TOKEN-VALUE 55%,
APPL-RESULT 40% (eight programs: the program stores 0, 8, 12 and 16, two of them named, so as the kind is
defined the fact is false), the six file-status items 30%, HTML-FIXED-LN 5% (a text buffer). R-066 C-1, the
three readings agree on most batch facts: on this reading they agree for Function and Money and not for
Enum, which the person's answers will settle.

The catalogue:

- **T-F1 FunctionRangeStructureIntegrationTest** (built; with `FunctionRangeStructureTest` on small
  programs): each `function` line's range on the paragraph graph and the contract's `falls` lines, which
  sir/ builds, never intent/rules' code: one entry (a PERFORM only of the whole range; no GO TO or ALTER in;
  not on the main line, which the main line does not fall into), one exit (no GO TO out), no overlap, no
  declarative section. Its killer mutations: CBSTM03B's 1000-TRNXFILE-PROC alone (its GO TO leaves it),
  CBSTM03A's 8100-TRNXFILE-OPEN and 1000-MAINLINE (an ALTER, the main line). Its first run disagreed with the
  rule on CBSTM03A's last five paragraphs, and the contract was wrong: `Contract.falls` read 8599-EXIT's
  dead `EXIT.` after `GO TO 0000-START.` as a fall into 9100-TRNXFILE-CLOSE. Fixed (J-204): no top-level
  statement after an unconditional GO TO, STOP RUN or GOBACK is reached.
- **T-F2 FunctionRangeXrefIntegrationTest** (built, J-205; with `FunctionRangeXrefTest` on a listing written by hand): the same three conditions read on
  cobc's cross-reference listing, a second parser of the source, with a count-equality guard so it cannot
  pass on nothing.
- **T-F3 PerformTracesTest** (built, J-205, as `PerformTraces`, held by ProofV1IntegrationTest over its runs;
  `rt/PerformTrace` traces PERFORM, return and fall to a file of its own): on the fixtures' runs, every entry to a range by
  PERFORM, every exit a return. Evidence on the fixtures' data only.
- **D-F1** (a document to find): IBM's Language Environment reference for CEE3ABD, that it ends the
  enclave and does not return; for the 12 abend paragraphs.
- **T-E1 EnumValuesStoredTest** (built, J-205): the values the program stores into an enum item
  are among its condition-names' values (APPL-RESULT's 8 and 12 are not; the evidence line now says so).
- **T-E2 EnumIsReadTest** (built, J-205): some condition-name of the item is tested, in the program
  or in the program it is handed to by CALL (HTML-FIXED-LN's never are).
- **T-E3 FileStatusAbsentIntegrationTest** (built, J-205, on STATUS35 in both lanes): a run with an input absent, where a file-status item takes a value no
  condition-name names ('35').
- **D-E1** (a document to find): the file status table (IBM's Language Reference, "Status key").
- **D-E2** (a document to find): IBM's CEEDAYS feedback codes, for FEEDBACK-TOKEN-VALUE.
- **T-M1 MoneyFlowTest** (built, J-205): every item a money item is added with, moved to or from,
  or compared with is a two-place number or an edited field; a rate only by multiplication.
- **T-M2 MoneyEditedElsewhereTest** (built, J-205): a copybook money item is printed or shown, by
  some program of CardDemo, through a currency- or comma-edited field.
- **D-M1** (a document to make): the values each money item holds in the fixtures' data, decoded under its
  copybook.
- **D-M2** (a document to find): the authors' own words beside the item in the programs (CBACT04C's
  'Int. for a/c ').

The contract fix's golden change, every line attributed (the J-119 condition): sir/ir.golden's contract facts
for CardDemo fall by 5, each a `falls` line of CBSTM03A whose paragraph ends `GO TO ... EXIT.` with the EXIT
never reached (8100-TRNXFILE-OPEN, 8200-XREFFILE-OPEN, 8300-CUSTFILE-OPEN, 8400-ACCTFILE-OPEN, 8599-EXIT);
NIST's rise by 5, each a paragraph ending in GO TO ... DEPENDING ON, which falls through when its index is
out of range (DB201A twice, NC102A twice, IC115A), and which the old reading took for a GO TO that always
leaves. No golden program's contract changed.

## 34. The remaining review tests, built (R-066; J-205)

Section 33's catalogue named seven tests not yet built: T-F2, T-F3, T-E1, T-E2, T-E3, T-M1 and T-M2. Each is
built as a check with its killer mutation on programs small enough to read, and, where it reads the review
file's facts, a verdict per fact that the fact's `tests:` line shows and the doc test holds to what the check
computes now. T-F3 needs the java lane to say, beside its paragraph trace, each PERFORM, each return from
one and each fall from a paragraph into the next: `rt/Dispatcher` writes them, with its program's name, to
`KATANA_PERFORM_TRACE_FILE`, a file of its own, since the paragraph trace is held equal to cobc's.

Predicted, before the tests (the advisor's expectations, section 33). T-F2, on cobc's cross-reference
listing: all 175 function facts hold. T-F3, on the four proven jobs' runs: no range is fallen into or out
of; each range a fixture enters is entered by PERFORM and returns as often; the abend and I/O-status
paragraphs not entered. T-E1: APPL-RESULT refuted in all eight programs (8 and 12 stored, unnamed);
CODATECN's two and the M03B codes hold; the six file-status items and FEEDBACK-TOKEN-VALUE cannot tell from
the program, being set outside it. T-E2: HTML-FIXED-LN refuted (never tested); CODATECN cannot tell (handed
to COBDATFT, assembler, outside CardDemo's COBOL); the rest hold, LK-M03B-OPER and WS-M03B-OPER through
CBSTM03B's tests. T-E3: a file-status item takes '35' on OPEN of an absent file, in both lanes, a value no
condition-name of CBEXPORT's or CBIMPORT's names. T-M1: all 13 money facts hold, WS-MONTHLY-INT's rate by
multiplication only. T-M2: the copybook amounts ACCT-CURR-BAL and TRAN-AMT are printed through a
comma-edited field by some program of CardDemo; the program's own working totals are not copybook items.
The rival: a test that cannot tell on a fact the advisor thought it would settle, which the verdict says.

Found (J-205). Every verdict is on the review file, under the fact's tests line, held to what the test
computes now by BatchReviewIntegrationTest. T-F1, T-F2: all 175 function facts hold, on sir/'s graph and on
cobc's listing alike; cobc's PERFORM and GO TO references number the IR's edges in all fourteen programs
(the guard first caught the reader: cobc's table cuts a name at 28 characters, CBEXPORT's
4200-CREATE-XREF-EXPORT-RECORD among them, and puts a long list's count on a line of its own). T-F3: the
five proven jobs' java-lane runs (POSTTRAN, TRANREPT, TRANREPT-STUDY, INTCALC, CREASTMT) enter 84 of the
91 ranges it is named for, each entry by a PERFORM of the whole range, none fallen into or out of; 7 cannot
tell (not entered); the 84 other function facts it is not named for are counted in
carddemo-batch.performs too. T-E1: APPL-RESULT refuted in all eight (8 and 12); the M03B codes hold in
both programs once the CALL is followed both ways (CBSTM03A's SETs are what CBSTM03B's LINKAGE receives);
HTML-FIXED-LN holds; the six file statuses and FEEDBACK-TOKEN-VALUE cannot tell, as predicted; CODATECN's
two cannot tell, against the prediction: the program stores only named values, but it hands the record to
COBDATFT, assembler, which may store what it likes (the rival, as stated). T-E2: HTML-FIXED-LN refuted;
CODATECN's two cannot tell; WS-M03B-OPER holds through CBSTM03B's tests; the rest hold. T-E3: '35' on
OPEN INPUT of an absent file in both lanes, so the six file-status facts are refuted as the kind is
defined, the program reading a value none of its condition-names names. T-M1: all 13 hold. T-M2: the six
it is named for hold, ACCT-CURR-BAL through COACTVWC's ACURBALO (+ZZZ,ZZZ,ZZZ.99), TRAN-AMT and the three
working totals through CBTRN03C's report fields; the totals are the program's own, so the program itself
is where they are printed.

## 35. 6.1, pattern structuring: a Function range becomes a method (R-029, R-032; J-210)

**What 6.1 asks** (ROADMAP 6.1): a GO TO to the next paragraph becomes a fall; a PERFORM range satisfying
`Function` becomes a method; a PERFORM VARYING with a provably monotone counter becomes a `for`. Each pass
is a file in `passes/`, with the facts it needs, its obligations, the transformation, its proof and its
ledger lines (ROADMAP phase 6). This step builds the first two; the third is the next step's, since its
obligation (the counter written nowhere in the body, its BY a positive constant, its UNTIL reached) is
an interval proof of its own, not a reading of the graph.

**Where it acts.** The literal lowering already makes each paragraph a method `s<label>` returning the next
label, stepped by `run`'s switch (R-021). An out-of-line PERFORM ends its stretch, `return d.perform(first,
last, back)`, and the statements after it are a new stretch `s<back>`. So a range does not need new code to
become a method; it needs a caller that calls it:

```
private void f<first>() {                       // the range FIRST THRU LAST, on Function fact F
    int pc = <first>;
    while (pc != <last + 1>) { if (pc == <end>) throw new Halt(); pc = step(pc); }
}
```

and a PERFORM of it, once, TIMES or UNTIL, lowered as structured Java in the caller's own stretch
(`f<first>();`, `while (!(cond)) f<first>();`), with no `d.perform`, no split and no `s<back>`. The range's
paragraphs keep their `s<label>` methods and their trace lines, so the paragraph-entry trace is the same by
construction; the last paragraph's `d.end(last, last + 1)` finds no PERFORM ending there and gives
`last + 1`, which ends the loop. A STOP RUN inside throws `StopRun`, as it does now; a GOBACK or EXIT PROGRAM
returns the run's end label, which the loop turns into `Halt`, the exception the declaratives already use,
caught in `run`. A VARYING PERFORM of a method range keeps the dispatcher's form, which still works, since
the range's paragraphs are all still in the switch.

**The pass decides; lower/ writes** (MOD-04: passes/ writes Java only through lower/'s emitter). `passes/`
reads the program's Semantic IR and its Intent, and gives lower/ a plan: the ranges that become methods,
each with the fact it stands on, and the GO TOs that become falls; lower/ takes the plan as data and decides
nothing (`Lower.java(program, className, plan)`, the literal lowering being the empty plan).

**Obligations**, each read on sir/'s graph and contract, never on intent/rules' code (R-032: a range that
fails one is not structured, and the pass says why, never with a warning):

1. A `Function` fact names the range, and no person refuted it (R-029: `Intent.actionable`); a fact no person
   confirmed licenses the method and its ledger line says `unconfirmed`.
2. Only a PERFORM of the whole range enters it: no PERFORM of a part of it, no GO TO into it, no ALTER
   naming it, the main line never falling into it.
3. It leaves only at its end: no GO TO out of it.
4. No other range overlaps it, and it never PERFORMs itself, directly or through another (recursion would be
   Java recursion with no end, where the dispatcher's PERFORM is undefined, LR 6.4 p. 414).
5. The program is not segmented (no independent segment with an ALTER, research §16), and has no USE FOR
   DEBUGGING (Debugging wraps every PERFORM), and the range is in no declarative section.

For a GO TO to become a fall: it is the paragraph's last top-level statement, unconditional, to the
paragraph after it, not alterable, and no PERFORM range ends at that paragraph (a GO TO passes a range's
end where a fall would take it).

**Proof** (ROADMAP 6's check for every pass): the CardDemo jobs the java lane proves (POSTTRAN, TRANREPT,
TRANREPT-STUDY, INTCALC, CREASTMT) run with the structured Java leave every dataset byte-identical to
cobc's golden, and each step's paragraph trace equal to the literal Java's; NIST's Java lane, structured,
reports what it reports literal, module by module; the GnuCOBOL autotests the lowering runs pass structured
as they pass literal; and the ledger: a line per method `f<first>` (pass `structuring v1`, its fact, its
state), and a removal line superseding each stretch `s<back>` a PERFORM no longer splits off, so LED-04
holds against the structured build.

**Predicted, before the build.** On CardDemo's batch programs, all 175 function facts discharge every
obligation and become methods (T-F1 and T-F2 hold on all of them; no batch program is segmented or debugs);
every dataset and trace the same. The PERFORMs that call them become plain calls, so the stretches and the
switch's cases fall by about one per PERFORM statement of a method range; the GO TOs to the next paragraph
are few (CBSTM03B's `GO TO nnnn-EXIT` jumps to the end of its own range's last paragraph, which a PERFORM
ends, so obligation 6 keeps it). On NIST, every report the same. The rival: an obligation the graph
discharges that the run disproves, which shows as a dataset or trace that differs, or a NIST report that
changes, and stops the pass where it shows.

## 36. 6.1's third pattern: a PERFORM VARYING with a monotone counter becomes a `for` (R-032; J-211)

**What it changes.** A PERFORM VARYING is lowered today always as stretches (`Performs.varying`): the FROM
moves, a test stretch per phrase, a step stretch, each a `s<label>` the switch steps. Structured, a loop of
one phrase becomes a Java `for` over the COBOL field itself, in the caller's own stretch:

```
<FROM moved into the counter>;
for (; !(<UNTIL>); <the counter stepped BY>) { <the inline body, or perform<first>();> }      // TEST BEFORE
for (;; <the counter stepped BY>) { <body>; if (<UNTIL>) break; }                            // TEST AFTER
```

The same moves, tests and steps in the same order as the stretches (LR 6.4 pp. 419-421, as research §16's
reading and `Performs` already have them), so it holds whatever the body does; the obligations are what make
it honest to call it a `for`, the counter the loop's own.

**Obligations** (R-032; the PERFORM is left as it was where one fails, and the pass says why):

1. One VARYING phrase. With AFTER, COBOL sets every inner identifier FROM before the outer test, and after
   stepping the outer one sets the inner FROM again before testing the outer condition, so at the end each
   inner identifier holds its FROM value (`Performs.varying`'s comment, NC201A); nested `for` loops leave it
   where its own test stopped it. Not the same; refused.
2. Its body is structured Java: an inline body that holds no out-of-line PERFORM but of a range the plan makes
   a method, no other VARYING but one the plan makes a `for` (inner loops first), no SEARCH and no SORT or MERGE
   procedure; or an out-of-line PERFORM of a range the plan makes a method.
3. The counter is monotone: BY is a numeric literal other than zero (or absent, 1), and nothing in the body,
   nor in any paragraph a PERFORM in it reaches, stores into the counter or a group holding it, or hands it
   to a CALL.

No intent fact licenses it; its proof is the run's. The ledger has a removal line for each stretch it removes.

**Predicted, before the build.** CardDemo's batch programs hold two PERFORM VARYINGs, both CBSTM03A's
4000-TRNXFILE-GET, the second inside the first, its body PERFORMing 6000-WRITE-TRANS (a method since J-210):
both become `for` loops, and CREASTMT reports and traces as literal. NIST: many become `for` loops, every run
reporting what it reports literal; those with AFTER stay stretches. The rival: a loop the obligations admit
whose run differs, which the comparison of each run with its literal self shows.

## 37. 6.2, method extraction under the dispatcher: a range's paragraphs as the method's own code (R-032 to R-034; J-212)

**What 6.2 asks** (ROADMAP 6.2): a reducible paragraph set becomes a method with local control flow; the
dispatcher shrinks; the residual `switch` stays for what is not reducible, with a finding naming it
(R-034: the residual dispatcher is acceptable output; R-033: Erosa–Hendren's total structuring refused).

**This step's set.** After 6.1 a `Function` range is a method, `perform<first>()`, but its body still steps
the global switch over the range's stretches (`int pc = d.perform(first, last, -3); while ...`). Here the
range's paragraphs move into the method as its own code, in order, each after its trace line and its
provenance comments, and their stretches, `s<label>`, leave the switch. A paragraph's fall into the next is
the next statement. A GO TO forward to a paragraph inside the range is a `break` out of a labeled block that
ends just before that paragraph's code (the blocks nest, the farthest target outermost: forward jumps in an
acyclic sequence are exactly what nested labeled blocks express). GOBACK and EXIT PROGRAM, the run's end, are
`Halt`, as in 6.1's methods. This is the acyclic, forward-only part of the reducible graphs; a backward GO TO
(a loop) stays in the switch this step, a finding naming it (a labeled `while` is the next step's).

**Obligations** (R-032; a range that fails one stays 6.1's method over the switch, and the pass says why):

1. The range is a method of 6.1's plan.
2. Its paragraphs need no stretch of their own: no PERFORM of a range no method, no VARYING no `for`, no
   SEARCH, no SORT or MERGE procedure, no NEXT SENTENCE, no GO TO DEPENDING, no EXIT PARAGRAPH or SECTION.
3. Every GO TO in it goes forward to a paragraph inside it, after the one it is in.
4. No PERFORM but the range's own ends inside it, and nothing PERFORMs it through the dispatcher (a VARYING
   no `for`, a SORT procedure): its first stretch is no longer in the switch to be entered.
5. Nothing falls into its first paragraph: the paragraph before it does not fall, or a PERFORM ends there.

**The residual dispatcher** is reported per program: the switch's cases before and after, and each range
left with why; the main line stays in the switch this step.

**Proof**: as 6.1's: every CardDemo job and every NIST module, structured, reporting and tracing what it
does literal; the ledger, a removal line for each stretch the switch loses.

**Predicted, before the build.** CardDemo's batch: nearly all of 6.1's 175 methods become the method's own
code; those left are the ranges with a GO TO DEPENDING or a backward GO TO (none expected in the batch
ranges, whose GO TOs go forward to an `-EXIT` paragraph) or a NEXT SENTENCE; the switch loses most of its
cases, the main line's and those ranges' remaining. NIST: many ranges inline; every run as literal. The
rival: a range the obligations admit that the run disproves (an entry the graph does not show), caught by
the comparison as a dispatcher `pc` with no case, or a report that differs.

## 38. 6.2b, the main line as `run`'s own code and the loops as labeled `while`s (R-032 to R-034; J-213)

**What is left after 6.2.** The switch holds the main line (CardDemo's batch: 30 cases, its main lines) and
the ranges with a GO TO back. A survey of the backward GO TOs before the build (J-212's follow-up, a Fable
advisor's reading of the design): NIST, 541 on main lines (533 of them a paragraph's GO TO to itself), 89
elsewhere (15 to itself), 2 inside method ranges (both to themselves); CardDemo, 8 on main lines and 1 a
paragraph's GO TO to itself. Backward jumps are mostly the main line's.

**This step's set.** The main line, the paragraphs control reaches from the start without a PERFORM, becomes
`run`'s own code, in order, as a method range's does in 6.2. A GO TO back to a paragraph T from a paragraph S
at or after it makes T a loop's header: `WT: while (true) { ...T..E...; break WT; }`, E the last paragraph that
jumps back to T, the GO TO a `continue WT`. Forward GO TOs stay `break`s out of labeled blocks, opened per
level (inside a loop, its own). A PERFORM of a range no method, which 6.2 refused, is a call,
`dispatch(first, last)`: the dispatcher runs it to a mark of its own, `-3 - depth`, so a PERFORM nested in
the dispatcher's run returns to its own frame (an `Unwind` carries a foreign mark up to the frame that owns
it). When no paragraph is left in the switch, `step` is not written.

**One reading of EXIT PROGRAM.** 6.1's main line skipped a `returns-if-called` fall in a program with
USING, 6.2's entries counted it: two readings. A program with USING may run as the main program (its PARM
its USING), and then EXIT PROGRAM falls (J-199). Both now count it.

**Obligations** (R-032; the main line that fails one stays in the switch, and the pass says why, a finding
`main line` per program, R-034):

1. The main line's code needs no stretch: as §37's 2 (a PERFORM of a range no method is now `dispatch`).
2. Its GO TOs name paragraphs on it; none is DEPENDING or altered; no ALTER reaches it.
3. Nothing enters it from elsewhere: no PERFORM or SORT procedure runs through it, no GO TO or fall from a
   paragraph off it.
4. No method's range is in it.
5. Its loops are reducible as text order says them: a loop is entered only at its header, and two loops nest
   or are apart. An irreducible one (two entries) stays residual: a choice of entry would change what runs
   (the question Ghost asked; R-033 refuses node splitting, and the dispatcher is the faithful form).

A method range takes 5 too: a range with a GO TO back is now its own code when its loops are reducible.

**Proof**: as §37's: every CardDemo job and every NIST module structured, reporting and tracing what it does
literal; the ledger's lines; the autotests structured.

**Predicted, before the build.** CardDemo's batch: 13 of the 14 programs lose their switch entirely (no
`step`); CBSTM03A keeps its main line, ALTERed (and its methods inline as before). NIST: most main lines
become `run`'s code, its 541 backward GO TOs mostly `continue`s; those left are main lines with ALTER, GO TO
DEPENDING, SORT procedures, segmentation, or a PERFORM running through the main line (the old style of
`PERFORM A THRU Z` over the line), and a handful of irreducible loops, perhaps none. Every run as literal.
The rival: a main line the obligations admit that the run disproves, an entry the graph does not show
(J-212's SORT procedure was one), seen as a report or trace that differs or a `pc` no case takes.

## 39. 6.3, scalar promotion: an item no bytes alias becomes a typed Java variable (R-032, R-068; J-215)

**What 6.3 asks** (ROADMAP 6.3): an item with no REDEFINES, RENAMES, reference modification, ODO,
group MOVE or raw I/O touching it, and `NeverOverflows` proven, becomes a typed field (`long` with
scale, or `BigDecimal` with an explicit scale).

**The survey, before the design.** Numeric elementary items, and what keeps each from being a value
the program alone holds: CardDemo's 14 batch programs have 292, of which 43 pass every aliasing
obligation below (62 are never referenced; the rest are blocked by a group around them that is READ,
WRITTEN, DISPLAYed, MOVEd, INITIALIZEd or passed by CALL, by FILE or LINKAGE, REDEFINES, reference
modification, or a CALL or STRING of the item itself). NIST's 437 programs have 10,470, of which 4,938
pass. The 43 are used by ADD 33, MOVE 23, DISPLAY 17, IF 15, SUBTRACT 5, PERFORM's condition 3, COMPUTE
2; NIST's 4,938 by MOVE, ADD, IF, SUBTRACT, COMPUTE, DIVIDE, MULTIPLY, a few SEARCH, EVALUATE and
PERFORM. 33 of CardDemo's are DISPLAY usage, 12 BINARY, 3 PACKED; 8 have decimal places.

**What makes it exact.** Every store into a numeric item is already a function of a value: an
arithmetic result is `Decimal.store(result, digits, scale, signed, rounded)` (R-020), a MOVE of a
number `Move.number`, the same function unrounded; the bytes only hold what it returns, and `get()`
returns it again, for every usage under `ibm.trunc STD` (binary items truncated to their PICTURE). A
numeric item starts at zero (R-057). So an item whose bytes nothing else reads or writes can be a
Java variable holding that value, every store computing it by the same function: what the program
does is unchanged whatever is proven about it.

**Ghost's choice (2026-10-19, R-068): exact always.** A promoted item's every store keeps COBOL's
truncation and size error, as the byte form does; `NeverOverflows`, proven by interval analysis, only
simplifies a store to plain arithmetic, and the corpus's evidence alone never does (an item an input
could overflow would stop truncating: different behaviour). This departs from the roadmap's wording,
where `NeverOverflows` gated the promotion. The type: an integer item of up to 18 digits a `long`, an
item with decimal places a `BigDecimal` at its PICTURE's scale; over 18 digits, not promoted.

**This step's set.** The variable is named for the item (`wsTranCount`), its PICTURE and usage in a
comment, the item's `Field` gone from the class, so a use the lowering has not converted fails to
compile rather than read stale bytes. A read in an arithmetic expression or a numeric comparison is
the value; a read anywhere else (DISPLAY, a MOVE to text or to an item not promoted, an alphanumeric
comparison) is a scratch field the value is encoded into at that point (`Scalars.field`), so the
runtime's own rendering decides the bytes. A store from arithmetic is `Decimal.store` with its size
error into the statement's flag, the variable unchanged where IBM leaves the receiver unchanged; a
MOVE of a number is `Move`'s function; a MOVE of anything else, and the initial value, go through a
scratch field and are read back.

**Obligations** (R-032; an item that fails one keeps its bytes, and the pass says why):

1. Numeric, elementary, DISPLAY, BINARY or PACKED usage (not native binary, not floating), at most 18
   digits; WORKING-STORAGE or LOCAL-STORAGE; not EXTERNAL or GLOBAL.
2. No REDEFINES of it or of a group holding it, or by it; no OCCURS on it or a group holding it; no
   RENAMES over it; no reference modification of it or a group holding it.
3. No group holding it is referenced as a whole, by any statement (its bytes would be read or written
   around the variable).
4. It is referenced only by MOVE, ADD, SUBTRACT, MULTIPLY, DIVIDE, COMPUTE, DISPLAY, IF, EVALUATE and
   PERFORM's conditions; it receives only from MOVE and the arithmetic verbs. Not CALL, ACCEPT, READ,
   WRITE, STRING, UNSTRING, INSPECT, SET, SEARCH, INITIALIZE (each reads or writes its bytes; a later
   step may take them through a scratch field).
5. Referenced at all (an item never referenced is left; it changes nothing).

**Not this step.** The interval rule for `NeverOverflows`: an arithmetic store simplifies only when its
value provably fits, and the items here are mostly counters a loop adds to, which no interval bounds
without the loop's trip count (reasoned, not measured: an ADD to an item whose interval is already
its whole PICTURE can overflow it, so no ADD of CardDemo's 33 is provable without the loop's bound). The rule waits
until the loop bounds are read (6.6's dataflow). Tables (OCCURS) as arrays, and the verbs of 4.

**Proof**: as §37's: every CardDemo job and every NIST module, structured and promoted, reporting and
tracing what it does literal; the ledger a line per promoted item (the variable new, the `Field` gone).

**Predicted, before the build.** CardDemo's batch: about 40 of the 43 promoted (a few fall to
obligation 4); every job's report and trace as literal. NIST: most of the 4,938, perhaps 4,000 (those
in SEARCH, SET or INITIALIZE, or receiving from another verb, fall); every run as literal. The rival:
a read the scratch field renders otherwise than the item's bytes did (a sign, a BINARY item's value
over its PICTURE, a codepage), or a size error a variable takes otherwise than its bytes (DIVIDE's
REMAINDER, several receivers), seen as a report or trace that differs.

## 40. 6.3's remaining verbs: a statement that writes a promoted item's bytes runs on a field of its own (R-068; J-216)

**Ghost's decision (2026-10-19).** Of 6.3's open parts, the `NeverOverflows` interval rule moves to 6.6 (it
needs loops' bounds, 6.6's dataflow), tables and items in records used whole to 6.7 (record classes); the verbs
§39's obligation 4 kept out are done now.

**The mechanism.** For one statement, a promoted item it names is a `Field` again: a field of the item's own
attributes, `p<id>`, holding its value, which every lowering path uses as it uses any field; after the
statement, the item takes the value the field then holds. That is exact when the statement leaves bytes that
are a valid encoding of a number (what `get()` reads back, re-encoded, is the same bytes): a MOVE, a numeric
store, INITIALIZE's zero or REPLACING value, all leave such bytes.

**Each verb.**

- INITIALIZE of the item itself (a group holding it is still a group used whole): zero, or REPLACING's
  value, MOVEd; through the field.
- SET: TO a value (a MOVE), UP or DOWN BY (a store, already the item's own), an 88 TO TRUE (its first
  value MOVEd); through the field.
- STRING: its senders are reads (a field the value is encoded into, as any read); its POINTER a numeric
  store; through the field.
- UNSTRING: its receivers (INTO, DELIMITER IN, COUNT IN), POINTER and TALLYING, each a MOVE or a numeric
  store; through the field.
- ACCEPT FROM DATE, DAY, DAY-OF-WEEK or TIME: digits MOVEd (`Accept.from`); through the field.
- CALL ... BY CONTENT: a copy of the field the value is encoded into; the called program cannot write it.

**Kept out, and why.** ACCEPT from the input device copies the record's bytes into the item as they are, and
CALL ... BY REFERENCE gives the called program the item's bytes to write as it likes: either can leave bytes
that are no valid number, which the literal program would DISPLAY as they are and a promoted item would show
decoded. A statement among the five above with statements of its own (ON OVERFLOW, ON EXCEPTION) is kept out
too: those statements may PERFORM a paragraph that reads the item before the field's value is taken back.

**Proof**: as §39's: every CardDemo job and NIST module structured and promoted, as literal; the ledger.

**Predicted, before the build.** CardDemo's batch: from 40 promoted to a few more (STRING's senders such as
WS-TRANID-SUFFIX's, if nothing else holds them; its CALL arguments are BY REFERENCE and stay). NIST: from 3,844
to about 3,950 (SET's 96, some of CALL's 45 BY CONTENT, a few STRING, UNSTRING and INITIALIZE), every run as
literal. The rival: a verb that leaves bytes no number encodes in a way this reading missed (an UNSTRING
receiver given a delimiter's characters, a SET of an 88 whose value is text), seen as a report that differs.

## 41. 6.4, exact-arithmetic substitution: an expression IBM carries without a cut is plain arithmetic (R-020, R-032; J-217)

**What 6.4 asks** (ROADMAP 6.4): step-wise IBM arithmetic becomes exact arithmetic only where no division or
non-integer exponent can trigger dmax truncation.

**When IBM's step-wise result is the exact one.** Appendix A (PG 6.4 p. 795, `rt/ArithProfile`, R-049) carries
`op1 + op2` and `op1 - op2` with one integer place more than the greater and the greater decimal places, `op1 *
op2` with the places summed; `carried` cuts only when integer and decimal places together pass the cap (30 under
IBM_COMPAT, the lane's). An operand whose value fits its places (its PICTURE's, a literal's digits) then gives an
exact sum, difference or product that fits the places carried, so `Decimal.cut` loses nothing at any step; ROUNDED
adds one decimal place to the last operation, which the exact value also holds. The store (`Decimal.store`)
rounds or truncates the exact value as it would the carried one: HALF_UP looks at the first digit dropped, which
the two share. A division cuts to dmax (p. 795) and a negative or non-integer exponent divides or goes to
floating point (pp. 796-797, 800): those keep the step-wise form.

**Which operands.** A promoted item (6.3) or a literal, ZERO among them, always holds a value within its places.
A data item's bytes need not: a packed or zoned field holding no valid number (after a READ into its record) can
decode to more digits than its PICTURE, which IBM's step-wise cut wraps and exact arithmetic would not. So this
step takes only promoted items, numeric literals and ZERO; an expression reading any other field keeps the
step-wise form (the survey: CardDemo's batch 10 statements, NIST 2,666). A later step may take such a field with
a check that its value fits.

**Survey, before the design.** Arithmetic statements: CardDemo's batch 79, of which 46 use only promoted items
and literals, 22 ZERO or a function, 10 another field, 1 a division; NIST 8,966: 5,629 only promoted items and
literals, 345 a function or ZERO, 2,666 another field, 326 a division or exponent.

**This step's set** (the pass decides, by statement, `passes/Exact`; the lowering writes): ADD, SUBTRACT,
MULTIPLY and COMPUTE whose expression is `+`, `-`, `*`, unary minus and plus over promoted items, numeric
literals and ZERO, every operation's places within the cap; ADD TO, SUBTRACT FROM and MULTIPLY BY with one
receiver (several share a temporary taken before any receiver changes, R-059, kept step-wise); no CORRESPONDING.
The value is written as Java: `long` arithmetic when every operand is an integer and every operation's integer
places are at most 18 (`wsRecordCount + 1`), else `BigDecimal`'s (`wsTotalInt.add(wsMonthlyInt)`), handed to
the store as `Decimal.exact(...)`; the store, its truncation, rounding and size error, is unchanged (R-068).

**Proof**: as §39's: every CardDemo job and NIST module, structured and promoted, as literal.

**Predicted, before the build.** CardDemo's batch: about 50 of its 79 arithmetic statements exact (the 46 and
those with ZERO); every job as literal. NIST: about 5,500 of 8,966 (the 5,629 less ADDs and SUBTRACTs of several
receivers), every run as literal. The rival: an operand whose value passes its places after all, or a ROUNDED
result the exact value rounds otherwise, seen as a report that differs.

## 42. 6.5, enum, flag and money types from the intent facts, behaviour kept whatever a fact says (R-029, R-070; J-218)

**What 6.5 asks** (ROADMAP 6.5): enum and flag types from `Enum` and `Flag` facts; money as a dedicated type from
`Money`.

**The survey, before the design.** CardDemo's batch has 32 such facts, every one `unconfirmed` (R-029: acted on,
flagged in the ledger): 20 Enum, 13 Money, no Flag. Of the Enum facts, 8 are APPL-RESULT's (a promoted `long`,
its 88s APPL-AOK 0 and APPL-EOF 16), and the code MOVEs 8 and 12 into it too: a closed Java `enum` could not hold
them. The other 12 are PIC X items or a group (file statuses, an operation code, HTML-FIXED-LN's 34 condition
names), in bytes (6.3 promotes numeric items only), several written by the I/O as FILE STATUS. Of the Money
facts, 7 are promoted `BigDecimal` items (WS-TOTAL-INT, WS-GRAND-TOTAL), 6 record fields (ACCT-CURR-BAL,
TRAN-AMT), in bytes until 6.7.

**Ghost's choice (2026-10-22, R-070).** An Enum or Flag fact gives its item names, not a closed type: each
condition name a predicate method (`applAok()`), the condition tests that name it without a subscript calling it;
a promoted numeric item's single-valued conditions also constants (`APPL_AOK = 0`), which a MOVE or SET ... TO
TRUE of that value assigns by name. The item keeps its own type and every value it can hold, so a wrong fact gives
a misleading name and nothing else. A Money fact on a promoted item makes it an `rt` `Money` (an amount at its
PICTURE's scale) in place of `BigDecimal`, every store still `Decimal.store`'s (R-068); a Money fact on a record
field waits for 6.7.

**Proof**: as §41's: every CardDemo job and NIST module as literal; the ledger a line for each new name, carrying
the fact and its state.

**Predicted, before the build.** CardDemo's batch: the 20 Enum items each with a predicate per condition name
(APPL-RESULT's two also constants, in 8 programs), the 7 promoted Money items `Money`, the 6 in records refused;
every job as literal. NIST: few facts (its names carry little meaning, the rules read names and use), every run
as literal. The rival: a condition test the predicate changes (a subscripted reference, a condition on a group
whose test reads otherwise), seen as a report that differs.

## 43. 6.4b, a record field in exact arithmetic, under a guard (R-069; J-219)

**What R-069 asks** (Ghost and Fable, 2026-10-21; HANDOFF "For Opus: 6.4b"): the statements 6.4 left out for
reading an unpromoted item (§41) are admitted under a run-time guard, each such operand's bytes a valid number for
its PICTURE and usage, the exact form when all pass and the step-by-step form otherwise; the ledger line names the
guard; several receivers stay step-by-step.

**The guard.** `Classes.numeric`, the NUMERIC test (`rt/`, under the profile's NUMPROC NOPFD and INVDATA NO, R-049),
and the value within the PICTURE's digits. The second is needed besides the first: the NUMERIC test passes every
binary item, and a binary field a READ or a group MOVE filled can hold more digits than its PICTURE; it passes a
packed item of an even number of digits whose spare high-order nibble is not zero. Either decodes past the places
IBM carries, where the step-by-step cut wraps and exact arithmetic would not. Within its digits, a field is an
operand exact arithmetic holds, as §41 shows for a promoted item. `Classes.exact(Field)` is the guard.

**The form.** A guarded statement is written twice, as one statement: `if (valid(f12, f13)) { <exact> } else {
<step-by-step> }`, `valid` one method per program that has a guarded statement, which checks each operand with
`Classes.exact`. The ledger records symbols, not statements (LED-04), so the line that names the guard is
`valid`'s, pass `exact-v1`, one per program; a statement guarded calls it with its operands. Operands admitted: a
numeric data item of DISPLAY, BINARY or PACKED usage, subscripted or not, never reference-modified (a native
binary item's value may pass its PICTURE by design, and stays step-by-step); in the exact form its value is
`f12.get()`, a `long` by `Scalars.longOf` when the expression is all integers within 18 places, else
`toBigDecimal()`. A promoted item, a literal or ZERO needs no guard (§41).

**Proof**: as §41's: every CardDemo job and NIST module as literal (R-069 C-1). `ExactArithmeticTest` gains
operands holding invalid bytes (a bad zone, a bad sign, a non-digit, a packed spare nibble, a binary value past its
PICTURE): the guard refuses each and the guarded form stores what the step-by-step form stores, size error with it;
and the guard removed, the exact form stores otherwise on at least one of them (R-069 C-2, the mutation).

**Predicted, before the build.** CardDemo's batch: the 10 statements §41 kept for a record field admitted, 74 of
79 exact (the rest a division, LENGTH OF, several receivers); every job as literal, its data valid, so the guard
passes and the exact form runs. NIST: most of the 2,492 admitted, perhaps 2,300 (those with a native binary or
reference-modified operand, or another hold, stay), every run as literal; where a NIST test feeds invalid data the
guard sends it step-by-step. The rival: a field whose bytes pass the guard and still decode otherwise than the
step-by-step cut reads them (a sign the NUMERIC test admits that `get()` reads differently), seen as a report that
differs.

## 44. 6.6, the dataflow, and stateless subprograms (J-221)

**Ghost's choice (2026-10-22).** The survey found few candidates: of the programs CALLed as subprograms,
CardDemo's batch has CSUTLDTC alone without a file (CBSTM03B keeps four open between calls), NIST about 12 of 24
plus 6 nested programs, several with no WORKING-STORAGE item at all. So: the dataflow first, as 6.6's core, applied
to those subprograms; what the `NeverOverflows` interval rule with loops' bounds would unlock measured before it is
built, for Ghost to decide with the numbers.

**What a stateless subprogram is.** WORKING-STORAGE keeps its contents between CALLs (LR 6.4: a program's state is
kept until a CANCEL, unless IS INITIAL). If every byte of WORKING-STORAGE that any statement writes is written
before it is read on every path of every call, and every other byte holds its initial value forever, no call can
observe what an earlier call left. The program is then lowered as if INITIAL: `initial()` true, so the run unit
drops the instance after each call and the next starts fresh (`RunUnit.call`). Exact by the proof, and the Java
says the program carries no state between calls.

**The dataflow: written before read, by byte.** A must-analysis over the bytes of each WORKING-STORAGE record:
the state at a point is the set of bytes written on every path from the program's entry to it. Bytes, not items,
so a group, its subordinates, FILLER, a REDEFINES and a RENAMES are one model. Paragraph entry states meet
(intersect) over every edge that may reach them: the fall from the paragraph before, every GO TO (DEPENDING: every
target), and every PERFORM whose range starts there; a PERFORM statement is followed by its state united with the
state at its range's last paragraph's end (the range's own writes on every path to its end), and if the PERFORM may
run its range no times (TIMES, UNTIL before, VARYING) by its state alone. An IF, EVALUATE or a phrase (ON SIZE
ERROR, AT END, INVALID KEY, ON OVERFLOW, ON EXCEPTION) meets its branches with the way past them. Extra edges only
shrink the sets, so every place the model is not exact errs towards "not written".

What a statement writes for certain: a MOVE's, INITIALIZE's, SET ... TO's and ACCEPT FROM DATE's receivers, and an
arithmetic receiver without SIZE ERROR's phrase; each whole, unsubscripted and not reference-modified. Nothing else
counts as a write (a READ INTO, ACCEPT from the input device, STRING, UNSTRING, CALL BY REFERENCE, a subscripted
or reference-modified receiver), though each may write. What a statement reads: every operand not only received,
an arithmetic TO receiver, a CALL's argument by any mode (the called program reads it), a subscript, and a
subscripted element as its whole table.

**Obligations** (a subprogram that fails one keeps its state, and the pass says why): CALLed (its PROCEDURE
DIVISION has USING, or a CALL in the corpus names it); no file, no EXTERNAL or GLOBAL item, no POINTER, no
declaratives, no ALTER, no segmentation, no nested program, no NEXT SENTENCE or EXIT PARAGRAPH or SECTION; and no
read of a WORKING-STORAGE byte that some statement writes, where the byte is not written on every path to the read.

**Proof**: as §43's: every CardDemo job and NIST module as literal; IC's modules, which CALL the subprograms many
times, are the trial. `DataflowTest` holds the analysis to small programs, each rule kept and each broken.

**Predicted, before the build.** CardDemo: CSUTLDTC, if its date work writes before it reads (unknown before the
run). NIST: most of the about 12 subprograms without files, several of them trivially (no WORKING-STORAGE item);
those that count their calls in WORKING-STORAGE (a test of state kept between CALLs, which IC tests) refused,
correctly. Every run as literal. The rival: a read the model misses (a write it counts that a path skips), seen as
an IC test whose second call reports otherwise.

## 45. 6.6b, the PICTURE rule: a store whose value always fits, written as plain arithmetic (R-068; J-222)

**Ghost's choice (2026-10-22).** J-221 measured what proves a store into a promoted item never passes the receiver.
Loop bounds would prove none of CardDemo's 67 stores and at most 196 of NIST's 6,194; the operands' PICTUREs alone
would prove 18 and 63. Ghost: "yes, build 6.6b with the PICTURE rule". Loop bounds are not built.

**The rule.** An exact statement (6.4, research §41) computes its expression as IBM does, without a cut, then stores
it with `Decimal.store`'s truncation, rounding and size error. R-068 lets a proof simplify that store: when the
value can never pass the receiver, the store truncates nothing, rounds nothing and raises no size error, so
`receiver = expression` is the same assignment. The interval is the operands' PICTUREs: a promoted item holds a
value within its PICTURE (every store into it is the byte form's), a record field under 6.4b's guard is valid
within its PICTURE (R-069), and a literal is itself. `a + b` and `a - b` carry max(integer places) + 1 integer
places and max(decimal places); `a * b` the sums.

**Obligations**, each statement in `exact` (a statement that fails one keeps the store, and the pass says why):
1. One receiver: a promoted item, whole and unsubscripted.
2. The expression's integer places at most the receiver's integer digits (its PICTURE's, scale off).
3. Its decimal places at most the receiver's scale, so neither truncation nor ROUNDED changes a digit.
4. An unsigned receiver stores the absolute value: so for one, every operand unsigned (an unsigned item, a literal
   with no minus sign, ZERO) and no subtraction or negation anywhere in the expression.

An ADD or SUBTRACT whose receiver is among the operands (an accumulator) can never pass obligation 2: its sum has
one place more than the receiver. That is J-221's measurement, and why loop bounds would be needed for them.

**What it writes.** `wsCount = (a + b);` for a `long` receiver over `long` operands, the `BigDecimal` expression
`setScale`d to the receiver's scale for a scaled one (exact, by obligation 3), `new Money(...)` of it for a Money
item; under 6.4b's guard, inside its `if (valid(...))`. ON SIZE ERROR's phrase, if any, sees `sz` false, as the
store would have left it.

**Proof**: as §44's: every CardDemo job and NIST module as literal (the structured, promoted and exact lowering).
A unit test holds each obligation kept and broken on small programs.

**Predicted, before the build.** CardDemo's batch: 12 to 18 of the 18 the measurement found (obligation 4 and one
receiver drop a few). NIST: about 50 (the measurement's 63, less those with an unsigned receiver and a subtraction,
plus a few among the 130 with ON SIZE ERROR's phrase, which the measurement left out). Every run as literal. The
rival: an operand whose value passes its PICTURE (a promoted item a store left otherwise, a BINARY field), seen as a
report that differs.

## 46. 6.7, record classes as typed views over a record's bytes, with fixed tables (J-223)

**Ghost's choice (2026-10-22).** The survey: CardDemo's batch has 239 01-level records. 194 have no aliasing
(no REDEFINES in or of them, no RENAMES, no reference modification of a member, no OCCURS DEPENDING ON, no
pointer, float, national or native-binary usage, not EXTERNAL or GLOBAL): 48 file records, 139 in
WORKING-STORAGE, 7 in LINKAGE. Their group uses are mostly MOVE (57), READ INTO (18), WRITE and REWRITE FROM, IF
and DISPLAY, and they hold 172 numeric items still in bytes. NIST: 13,881 of 15,049 records have no aliasing.
Ghost chose a typed view, and fixed OCCURS in the same step.

**What a record class is.** A Java class per record, over the record's bytes, which stay the truth. Each member
of the record is a named `Field` of the class, its name the item's (TRAN-AMT `tranAmt`), and the program reaches
it as `tranRecord.tranAmt`, not `f12`. A numeric member also has a typed getter, `long tranAmt()` or
`BigDecimal`, returning what IBM's read of the bytes gives (`Field.get()`, so NUMPROC and invalid data stay
IBM's). The exact form reads through the getter. Every store goes through the `Field`, as before, so every store
keeps COBOL's truncation and size error. Group MOVE, READ INTO and WRITE FROM stay byte copies of the record, so
the byte codec stays at the boundary. The view changes no byte and no operation: exact by construction.

No setters. Every store already goes through a `Field` operation (`Arithmetic.store`, the MOVE rules), and a
setter no lowering calls would be dead code.

**Tables.** A member under a fixed OCCURS gets an accessor, `Field entryAmt(long i)` (one argument per
dimension), which is `Table.at` over the first element. A subscript out of range reads the neighbouring bytes,
as IBM's does with SSRANGE off.

**Obligations** (a record that fails one keeps its `r`/`f` fields, and the pass says why):
1. A group 01-level in WORKING-STORAGE or the FILE SECTION. LINKAGE is left out: its bytes are the caller's,
   bound at each CALL.
2. No aliasing, as above.
3. Not in a program the structuring pass leaves literal (declaratives, independent segments with ALTER).

A promoted member (6.3) stays the program's variable and is not in the class.

**The ledger.** The class instance is a symbol of a new node form, `record <id>`; each member's new name
(`tranRecord.tranAmt`) supersedes its `f<id>`, under `records-v1`.

**Proof**: as §45's: every CardDemo job and NIST module as literal; the ledger holds against the build (LED-04).
A unit test holds the class, the getters and a table's accessor, and each obligation broken.

**Predicted, before the build.** CardDemo's batch: about 130 records as classes (the 187 clean ones in
WORKING-STORAGE and files, less the elementary 01s). NIST: some thousands (most NIST 01s are elementary
counters and messages). Every run as literal. The rival: a name the class gives that two members share (the
same name under two groups of one record, qualified in COBOL), or a constructor past the JVM's method size on a
large record, seen as a compile failure.

## 47. 6.7b, LINKAGE records as record classes (J-224)

**Ghost's word (2026-10-22).** "The linkage records, if those cannot be done now, lets move them later in the
roadmap where they can be done." They can be done now: 6.7's classes are views, and a view needs only its
record's storage and where in it the record starts.

**How a LINKAGE record is bound.** A LINKAGE record has no storage of its own: each CALL binds it to the caller's
argument, "the same storage area" (LR 6.4 p. 320), or SET ADDRESS OF binds it to an address (p. 443). The literal
lowering rebuilds each item's `Field` in `bind<id>(Field a)`, at `a.offset()` plus the item's offset. A record class
does the same in one step: its constructor takes the storage and the record's start, `new TranRecord(a.record(),
a.offset())`, and each member is built at that start plus its offset. A WORKING-STORAGE or file record's class is
built at start 0, as before.

**Obligations.** 6.7's, with LINKAGE admitted: a group 01-level, nothing aliasing it (no 01 redefining it, which
`bind` would bind with it), not EXTERNAL or GLOBAL.

**Proof**: as §46's. NIST's IC modules CALL subprograms whose LINKAGE records are bound on every call, and CardDemo's
CSUTLDTC and CBSTM03B take their arguments the same way.

**Predicted, before the build.** CardDemo's batch: 6 of its 7 LINKAGE group records become classes (J-223 refused 6
for LINKAGE alone). NIST: about 20 of the 21 refused for LINKAGE alone. Every run as literal. The rival: a call that
binds the record before its class exists, or a member that keeps the start of an earlier call, seen as an IC module
that reports otherwise.

## 48. 8A.1's trial: a foreign Java port run through the lanes, and refused when it ignores the DD contract (J-229)

**Ghost's word (2026-10-22).** "run the 8A.1 trial now", ahead of the chain (TODO.md puts 8A after 7.1; R-072).

**What can run.** R-072 named the oc4j CardDemo reference as the first foreign port. It cannot run here: its classes
import opensourcecobol4j's runtime (`jp.osscons.opensourcecobol.libcobj`). That runtime is in neither the tree nor the
image, and the network is closed. Adding it is a dependency change, which waits on Ghost's approval (the supply-chain
rules). The trial takes the other port R-072 names, hand-written ports. It writes three of a small program whose
COBOL has the two traps a money port most often misses.

**The program.** `ACCRUE`, written for the trial, has a 20-byte record, `ACCTREC`: an id, a balance and an interest,
both `PIC 9(5)V99`. For each record it does `COMPUTE ACCT-INT = ACCT-BAL * 0.0375` and then `ADD ACCT-INT TO ACCT-BAL`,
with no ROUNDED and no SIZE ERROR phrase. It writes the record and says how many it wrote.
- The COMPUTE truncates the result to two places (LR 6.4 p. 263: without ROUNDED, excess low-order digits are
  dropped).
- The ADD drops a high-order digit when the sum passes 99999.99 (LR 6.4 p. 285: without SIZE ERROR the result is
  truncated, under TRUNC(STD)).

Five records: one whose interest has a third decimal of 5, one exact, one with a third decimal over 5, one balance
that overflows, and one cent.

**The three ports**, each a process honouring LANE-06's adapter shape, run by the java lane as the step's program:
1. *Faithful:* `BigDecimal`, the interest set to scale 2 with `RoundingMode.DOWN`, the sum taken modulo 100000.
2. *Naive:* the same with `HALF_UP`, the most common port of a COBOL COMPUTE.
3. *Deaf:* it writes its output to a fixed path in its working directory and never reads `DD_OUTFILE`.

**What 8A.1 builds.**
- `oracle/prove/Prove`: the cobc lane twice through the gate, the foreign command once in the java lane, then each
  output dataset compared by its copybook (`Compare`) and the step's stdout compared.
- A refusal, with a finding, for a port that did not honour the contract: an output dataset the cobc lane wrote that
  the port's step left empty or never wrote (LANE-01, LANE-04). A refused port's datasets are not compared.
- `Prove.main`, reached as `gradle prove`, is the command until 8.1's CLI names it `katana prove`.

**Predicted, before the run.**
- The faithful port is the SAME on every dataset and on stdout.
- The naive port differs on records 1 and 3 in `ACCT-INT` and `ACCT-BAL`, by one cent each, and on nothing else.
- The deaf port is refused, naming `OUTFILE`.
- What had to be adapted: only LANE-06's `main` (the DDs from `DD_` variables, the exit code as the return code).

The rival: the comparator's field report does not name the field, or a port's stdout is held against cobc's in a
way that hides the difference.

## 49. 8A.2, the divergence catalogue as data: the first three kinds, each a generator and a mutation (J-230)

**Ghost's word (2026-10-22).** "continue into 8A.2", after 8A.1's trial (J-229).

**What a kind is.** A divergence kind (`DV-`) is one way a Java port departs from the COBOL it was written from. It
is a catalogue row, `findings/Divergence.Kind`, with:
- its title;
- the COBOL rule and the IBM text it rests on;
- the wrong Java shape a port takes;
- the exact Java, `rt/`'s call that keeps the rule (what the report will offer as the fix, R-072).

`docs/DIVERGENCES.md` is the table, held to the enum by a doc test, as FINDINGS.md is. The seed is
`docs/research/semantic_traps.md` §1, numeric semantics.

**What a generator is.** `oracle/prove/Generators` writes, for a kind and a seed, the parts of a case:
- a COBOL program, one statement of the kind's shape in a read-compute-write loop;
- its copybook;
- twenty input records drawn so that about half cross the kind's boundary;
- a fixture.

Two ports run each case through `Prove` (8A.1): the exact one, which keeps the rule, and the wrong one, the same
port with that one rule broken, which is the kind's mutation. A kind is held when its exact port is the SAME and its
wrong port DIFFERS. The catch rate is the share of records whose fields differ.

**The first three** (TODO.md 8A.2):

| Kind | Statement | Rule and authority | Exact Java | Wrong Java shape |
| --- | --- | --- | --- | --- |
| DV-01, high-order truncation | `ADD A TO B`, B `PIC 9(3)V99`, no SIZE ERROR | a result too large for the receiver loses its high-order digits (LR 6.4 p. 285; PG Appendix A; TRUNC(STD)) | the sum modulo 10^3, `Arithmetic.store` without the phrase | a fixed-width writer cutting the formatted value at its right end (`substring(0, width)`) |
| DV-02, scale truncation | `COMPUTE R = A * K`, R `PIC 9(5)V99`, no ROUNDED | excess low-order digits are dropped, not rounded (LR 6.4 p. 263) | `setScale(2, DOWN)` | `HALF_UP`, the common reading |
| DV-03, intermediate precision (dmax) | `COMPUTE R = A / B * C`, R `PIC 9(5)V99`, A, B and C integers | a quotient is carried to dmax decimal places, the most decimals of any operand or receiver, before the next operation (PG Appendix A; §41; the reproducer `1 / 3 * 300` = 99.00) | the quotient set to scale 2 DOWN, then the product | the whole quotient in `MathContext.DECIMAL128`, cut only at the store |

**Predicted, before the run.**
- Each kind's exact port is the SAME on all twenty records.
- Each kind's wrong port differs on between 30% and 70% of them:
  - DV-01 on the records whose sum passes 999.99, about half by the draw;
  - DV-02 on those whose dropped part is at least half a cent, about half;
  - DV-03 on those whose quotient does not end within two places and whose multiplier makes the cut reach a cent,
    about half.
- Prove's report names the receiver field and no other.

The rival is DV-03: cobc's `-std=ibm` arithmetic departs from Appendix A in places (D-019, D-023). If its division
keeps more places than dmax, the exact port differs and the wrong one does not, and the kind is held against IBM's
text with a discovery, not against cobc.

## 50. 8A.3, boundary inputs from the IR, with the events each drives counted where it happens (J-233)

**Ghost's word (2026-10-22).** "do the four small items first, then 8A.3". 8A.3's check (TODO.md): on CardDemo, the
generated fixture enters every paragraph the sample data does, and drives at least one of each of these, counted at
the sites, that the sample data never does:
- a SIZE ERROR;
- a high-order truncation;
- a negative store into an unsigned item;
- a dmax division.

**The counters.** `rt/Events`, on only when `-Dkatana.events=<file>` names a file, as the paragraph trace is only
with `KATANA_TRACE_FILE`. It writes one line per event: the kind, the program, and the paragraph control last
entered (`Trace.paragraph`, which the lowering writes at every paragraph). The kinds:
- `SIZE_ERROR`: the condition met by a store (`Arithmetic.store`, `Scalars.store`), with the phrase or without it;
- `TRUNCATED`: a size error stored without ON SIZE ERROR, so high-order digits are lost;
- `SIGN_DROPPED`: a negative value stored into an unsigned receiver;
- `DMAX_CUT`: a quotient whose digits ran past the places Appendix A carries (`Decimal.divide`).

The site is the paragraph; a statement-level site waits on a site id from the lowering. Nothing else in `rt/`
changes, and without the property nothing is written.

**The inputs, from the IR.** For a job:
- each program it runs (a step's PGM, a module) is parsed and built;
- every numeric item an arithmetic statement names (ADD, SUBTRACT, MULTIPLY, DIVIDE, COMPUTE: operands and
  receivers) is taken by name;
- in each input dataset whose copybook holds a field of that name, the field is set record by record, in turn:
  - to its PICTURE's largest magnitude;
  - to that magnitude negative (its overpunch, FIXTURE-10, for a signed field);
  - to a value whose decimals are all nines;
  - left as the sample has it, one record in four, so the paths the sample takes are still taken.

Keys and every other field are untouched, so the records still meet across files. The generated datasets and a
fixture naming their hashes are written under `build/`, never into the tree.

**The runs.** Each CardDemo job the Java lane runs, with the sample fixture and with the boundary one, lowered
literal and traced with the events on. Paragraphs entered and events counted, by site, are compared. On the boundary
data, the java lane is compared with the cobc lane, and structured with literal: the proof the hostile review
(2026-10-22, §1) found missing, on inputs that reach the obligations.

**Predicted, before the run.**
- Sample data: no SIZE_ERROR, TRUNCATED or SIGN_DROPPED anywhere. DMAX_CUT only in CBACT04C's
  1300-COMPUTE-INTEREST (a balance times a rate, divided by 1200).
- Boundary data:
  - TRUNCATED and SIZE_ERROR at CBTRN02C's balance and cycle ADDs, and at CBACT04C's ADD of the interest. They are
    equal in number, since no batch program has ON SIZE ERROR.
  - DMAX_CUT where the sample has it, and more.
  - No SIGN_DROPPED: CardDemo's receivers of signed amounts are signed. If so, that part of the check cannot be
    driven by data, and the step says so, not silently.
  - Every paragraph the sample enters is entered.
  - The java lane is the same as the cobc lane, and structured the same as literal.

The rivals:
- a boundary value that sends a record down a reject path, losing a paragraph the sample enters;
- a lane difference on boundary data, which would be a defect the sample never reached and a finding, not a failure
  of the step.

## 51. 8A.4, the alignment file: a foreign port's Java mapped onto the COBOL by the program's own names (J-234)

**Ghost's word (2026-10-22).** "yes, continue into 8A.4".

**What it is.** `<PROGRAM>.align` maps each COBOL node to the place in a foreign port's Java that does it, so the
report can trace a difference from a field to a statement to the Java that ran it (R-072). A node is named by the
program's own names, never by the IR's numbers (R-064):
- `paragraph NAME`, with `IN SECTION` where two paragraphs share a name;
- `item NAME`, with `OF PARENT` as far as makes it unique;
- `statement VERB #k IN PARAGRAPH`, the k-th statement of that verb in the paragraph.

Each line reads `node | Java place | state | evidence`:
- the place is `File.java:line` or `File.java:first-last`, or an identifier with its line;
- the state is `unconfirmed`, `confirmed` or `refuted`;
- the evidence says how the line was inferred.

Inference marks every line `unconfirmed`; only a person changes a state (R-029). Reading an alignment refuses a line
whose node the IR does not hold, so a renamed paragraph cannot carry a person's word to the wrong place.

**How it is inferred** (`oracle/prove/Align`), evidence by evidence, the strongest first:
1. `source`: a comment naming the COBOL source file and a statement's line (`CBACT04C.cbl:464:`), which generated
   ports carry.
2. `name`: a paragraph's or an item's name in a comment or an identifier, in any of the spellings a port uses
   (`ACCT-CURR-BAL`, `ACCT_CURR_BAL`, `acctCurrBal`, a prefix or a qualifier around it).
3. `literal`: a statement's literal (an alphanumeric literal's text, a numeric literal's digits) found at exactly
   one place in the Java's literals.

A paragraph's place runs from its line to the next aligned paragraph's.

**Predicted, before the run.**
- On opensourcecobol4j's Java for the CardDemo batch programs it compiled (`lower/prototypes/oc4j-carddemo-reference/`,
  read as text, never run or shipped):
  - paragraphs at least 95%, by name;
  - statements at least 90%, by source line;
  - items at least 80%, by name.
- On 8A.1's hand-written port of ACCRUE (`AccruePorts`), whose Java names nothing as the COBOL does:
  - no paragraph or item;
  - statements only by literal, the COMPUTE's 0.0375 and the DISPLAY's 'ACCRUED ', 2 of its statements.

Which is the point: for a hand port, the alignment is mostly a person's to write, and the file says so line by line.
The rival: an identifier match that is wrong (a name the port reuses for another field), which only a person's
review catches. Hence `unconfirmed`.

## 52. 8A.5, attribution: which divergence kind reproduces a field's difference, by probe (J-236)

**Ghost's word (2026-10-22).** "do the tithe first, then 8A.5".

**What attribution claims.** When a foreign port's output differs from the cobc lane's, the report says which kind
of divergence could have caused it. The claim is never "your Java forgot to truncate" (R-072). It is: "running the
COBOL with exactly this rule broken gives your output on these cells". A kind's probe is Katana's literal Java for
the same program, run under the same fixture with that one rule broken in `rt/`:
- DV-01: a value too large for its receiver keeps its leading digits, the formatted value cut at its right end;
- DV-02: a store without ROUNDED rounds HALF_UP;
- DV-03: a quotient is carried in full (`MathContext.DECIMAL128`), cut only at the store.

The switch is `-Dkatana.mutate=DV-0n`, read once, off by default. It is for the report's probes and the mutation
harness only, which LANE-23 says.

**The rank.** Every cell (record, field) where the port differs from the cobc lane is a cell to explain. A probe
reproduces a cell when its output there is the port's, byte for byte. Kinds are ranked by the cells they reproduce;
a kind that reproduces none is not listed; a difference no probe reproduces is `unattributed`.
`oracle/prove/Attribute` does the counting. Its runs come from its caller, since oracle/ never imports lower/
(MOD-05).

**The check** (TODO.md 8A.5):
1. The mutation harness: for each kind, Katana's literal Java of that kind's generated case (8A.2), run with the
   kind's rule broken, is the foreign port. The attribution must name that kind first.
2. Each mutation is red before its kind is built: with that kind's probe withheld, the same port's difference is
   unattributed or named otherwise.
3. A real foreign port: 8A.1's hand-written HALF_UP port of ACCRUE.

**Predicted, before the run.**
- Each kind's mutant is attributed to its own kind first, which reproduces every differing cell (3 of 3). The other
  two probes reproduce none of its cells: DV-01's and DV-03's programs store no result whose rounding matters, and
  DV-02's divides nothing.
- With its own probe withheld, each mutant is unattributed (3 of 3).
- ACCRUE's naive port: DV-02 first, reproducing all four differing cells (two records, `ACCT-INT` and `ACCT-BAL`).

The rival: kinds that overlap, where one rule's break reproduces another's cells. The ranking would then show two
kinds for one cause, and the report must show both rather than pick one.

## 53. 8A.6, the divergence report: each divergence proven or possible, its statement, cause, trace and fix (J-237)

**Ghost's word (2026-10-22).** "yes, continue into 8A.6".

**What the report says**, for a foreign port and its COBOL under one fixture (R-072): a header and the entries.

The header quotes docs/PROOF.md's "What it never covers" verbatim, as PROOF-01 has every proof do, since the oracle
is cobc -std=ibm and not IBM (R-013). It names the fixture by path and hash and the port by its command.

Then one entry per divergence, each with:
- **status:** `proven`, where a run differed and the report cites the fixture by path and hash and the input record
  that produced it by hash; or `possible`, a statement of a kind's shape that this fixture never took across the
  kind's boundary (no event of that kind at its paragraph in Katana's own run, LANE-22). A possible entry is a place
  to look, which 8A.3's boundary inputs can turn into a proof.
- **where:** the field, the dataset and its records.
- **the statement:** the COBOL statement that stores the field, named as the alignment names it, with its origin.
  When a statement reads another differing field, the entry says it follows from that field's divergence, rather
  than claiming a second cause.
- **the aligned Java:** the place, its state and evidence from the `.align` (8A.4), or `unaligned`.
- **the cause:** the kind attribution ranks first, with the cells it reproduces (8A.5), its rule, the IBM text, and
  the wrong shape. Or `unattributed`.
- **the trace:** the paragraphs Katana's own run entered on that record's pass, up to the statement's paragraph.
- **the fix:** Katana's literal Java for the statement, sliced from the lowering by its provenance comment, with the
  kind's exact-Java description. It is a reference, never a patch to the port (R-072).

Last, the program's NE- and HY- findings, apart, as "no port can be exact here".

**Refusals.** A `proven` entry whose fixture is missing, or whose hash is not the fixture's, is refused. The report
writes nothing that varies between runs: no time, no absolute path, entries in the program's order. Two runs write
identical reports.

**Predicted, before the run** (8A.1's hand-written HALF_UP port of ACCRUE):
- one proven root divergence: `ACCT-INT` on records 1 and 3, stored by `COMPUTE #1 IN PROCEDURE DIVISION`
  (ACCRUE.cbl:28), aligned to `AccruePorts.java` by its literal, cause DV-02 reproducing all its cells, and the fix
  Katana's `Arithmetic.store` of the step-by-step product;
- `ACCT-BAL` on the same records, following from `ACCT-INT`'s divergence, through the ADD that reads it;
- no possible entry: the ADD's overflow is reached by record 4, so DV-01's boundary is exercised;
- no NE- or HY- finding;
- two runs identical.

The rival: the trace by record. A program whose paragraphs are entered other than once a record has no pass per
record; such a trace is given for the run and says so.

## 54. 3.1, the runnable autotests through the lowering: a first sweep of all 378, outcomes counted by stage (J-246)

**Ghost's word (2026-10-23).** Attempt TODO 3.1, the next roadmap item, as far as can be done soundly.

**What the step builds.** `AutotestsLoweredIntegrationTest`: every runnable case in
`corpus/gnucobol-autotests/cases/` (the 378 that `AutotestRtIntegrationTest.expected` finds a check for), its
`prog.cob` through parse, the Semantic IR, `lower/`, javac against `rt/` alone, and a run with a time limit. Each
case ends at the first stage it cannot pass, and the golden `test/katana/rt/autotest/lowered.golden` records that
stage per case and the counts: `NOPARSE` (the front end reports the program unclean), `NOLOWER` (the IR or the
lowering throws or reports a finding that stops it), `NOJAVAC`, `NORUN` (an exception or the time limit), then
`PASS` or `FAIL` against the testsuite's expected stdout, which is cobc's default dialect and not IBM's (D-010; R-045:
the expected side is only compared, never shown). The test holds a floor: the count of PASS never falls below the
golden's. The 24 hand-lowered cases are a subset and must not fall behind: each that the hand-lowered check passes
and that parses must also pass here, or be a listed finding.

This is not the whole of 3.1's check ("run as unit tests of `rt/`, then through the lowering, each testing one
rule"); it is the through-the-lowering half at scale, as a census, so that each later step is a named stage's count
falling. It does not classify a FAIL as a finding against the testsuite's dialect (D-010); that is the step after,
when the counts show which FAILs are one cause.

**Predicted, before the run** (from 24 of 24 hand-lowered and 21 of 24 parsing, J-112, J-114):
- `NOPARSE` is the largest bucket outside PASS: between 100 and 200 of the 378 (GnuCOBOL's own items, free format,
  intrinsic functions and file verbs the grammar may not take);
- `NOLOWER` and `NOJAVAC` together under 40, since the lowering already holds 83 of NIST NC's 95 runs;
- `NORUN` under 30, mostly the time limit and a missing input;
- `PASS` between 90 and 160, `FAIL` between 20 and 60, most FAILs the testsuite's dialect (D-010) and the rest
  ignorance of the lowering;
- the 21 hand-lowered that parse all reach `PASS` or a listed finding.

The rival: reading the outcome from javac's and the lowering's messages alone. A case that lowers with a finding the
lowering prints (P-03) would then count as lowered; the stage here is the first the case cannot pass, and a finding
that makes the Java a stub is reported with the case, not hidden in PASS.

## 55. 3.1, a reason for each autotest that does not parse, and the causes by count (J-248)

**Ghost's word (2026-10-23).** Part B of the follow-up to J-246: record a reason for each case that does not parse,
report the causes by count; then read the 9 cases that do not compile.

**What the step builds.** The golden `lowered.golden` gets, on each `NOPARSE` line, the first error the front end
reports for the program (its message, without the path, line and column, which vary with the checkout), and the
test prints the causes by count. No behaviour of the front end, the lowering or `rt/` changes; the PASS floor is
unchanged. The 9 `NOJAVAC` cases are read, not yet changed.

**Predicted, before the run** (from what the 162 are: GnuCOBOL's own tests, not IBM's):
- one or two syntax causes cover half of the 162: GnuCOBOL-only data items (`BINARY-CHAR`, `BINARY-LONG`,
  `FLOAT-SHORT`, 78-level constants) and statements or phrases of the GnuCOBOL dialect the forked IBM grammar lacks;
- free-format source (`>>SOURCE FORMAT IS FREE`) a visible cause of at least 10 cases;
- cases of a preprocessing error (COPY, REPLACE) at most 15;
- more than 20 distinct messages in all (a long tail), the largest under 40 cases;
- the 9 `NOJAVAC` cases share one cause (a "cannot find symbol" from one missing helper or a name the emitter writes
  and `rt/` does not have): all 9 say "cannot find symbol".

The rival: classifying by the construct in the source rather than the parser's first message. The first message is
what the front end says, which is the evidence; a construct count is the reader's judgement and comes after.

## 56. 3.1, the nine autotests whose lowered Java does not compile: three causes, one fixed (J-248)

**Ghost's word (2026-10-23).** Read the 9 cases that do not compile; fix the cause if it is one cause with a sound fix.

**Found by reading javac's first error on each** (§55 predicted all nine one cause: wrong, three):
- Five cases (run_extensions-2260; run_fundamental-3277, 3326, 3375, 3871) hold nested programs. A nested program's class
  names its container by the container's program-id (`private final PROG up;`), while the top-level class is written
  under the name `Lower.java` is given (`Prog`), so `PROG` is not a class. The two names agree whenever the caller
  passes `Lower.className(program)`, which is why no test with nested programs found it.
- Three cases (run_extensions-2381, 4398, run_misc-13585) call `bind1`/`bind2`/`bind3` that the class does not
  declare (a `PROCEDURE DIVISION USING` of an entry the lowering did not write). A different cause; not touched here.
- One case (run_misc-13861) writes `Unsupported.fail("…floating-point…").value()`, a call on a method that returns
  nothing. A different cause; not touched here.

**What the step builds.** `Lower` passes the top-level class's own name down to a nested program, which names its
container by it (only the top-level container is renamed; a nested container is still `Lower.className(n)`, the
name its class is written under).

**Predicted, before the run:**
- the five nested cases compile (NOJAVAC 9 to 4); each then ends at NORUN, FAIL or PASS, I do not predict which;
- the PASS count does not fall (49 or more) and no golden but `lowered.golden` changes: `gradle test` and the rt,
  lower and passes integration tests (CallVerbsTest, ManifestTest, the NIST floors) are unchanged, because they pass
  `Lower.className(program)` or a name equal to it;
- the other four NOJAVAC stay, each with the cause above.

The rival: fixing it in the test (load the class under `Lower.className(program)`). That hides the defect from the
next caller who passes another name (JavaRunner and ManifestTest already do).

## 57. 3.1, the census gives the parser the case's own directory as its copybook path (J-249)

**Ghost's word (2026-10-23).** Take 3.1's "Next" in order; the first is the copybook-not-found cause (13 of the 162
that do not parse).

**Found by reading the 13 cases.** `LoweredAutotestsIntegrationTest.build` and the census's `firstError` both build
`new Copybooks(List.of(), ...)`, no folder at all, so no `COPY` can be found. Six of the 13 keep the file they copy in
the case's own directory (`syn_copy-382`, `438`, `528`, `630`, `syn_misc-3318`: `*.inc`; `run_misc-10634`: `cpyabrt`).
The other seven (`run_file-10289`, `10468`, `10661`, `11040`, `11361`, `11692`, `11881`) copy `xfhfcd.cpy` or
`xfhfcd3.cpy`, which GnuCOBOL installs beside `libcob` and no case directory or the extracted tests holds; the case's
directory cannot find them, and a copybook is not ours to write (R-037, R-045).

**What the step builds.** Both calls pass `List.of(CASES.resolve(id))`. Nothing in `front/`, `lower/` or `rt/` changes.

**Predicted, before the run:**
- the "copybook not found" cause falls from 13 to 7 (the `xfhfcd` ones);
- of the six that now read their copybook, none is a PASS-by-luck: each moves to a later stage or to a different
  first error (a `.inc` that holds a fragment, `syn_*` cases are syntax tests that may expect an error);
- NOPARSE 162 to between 150 and 156; PASS stays 49 or rises by at most 2; the 21 hand-lowered cases are unchanged;
- no golden but `lowered.golden` changes.

The rival: pointing the parser at the whole `cases/` tree. A case would then find another case's file of the same
name, and `copy.inc` is in four cases with different text.

## 58. 3.1, the `>>` directives: conditional compilation and `>>D`, the rest left as errors (J-251)

**Ghost's word (2026-10-23).** Take 3.1's "Next" in order; with the copybook cause done (J-249), the first is the
`>>` directives (13 of the 153 that do not parse, all with "mismatched input '>'").

**Found by reading the 13 cases.** `Lines` reads a line whose text starts `>>` as code, so the grammar meets `>`.
They are five different things: `>>IF`/`>>ELIF`/`>>ELSE`/`>>END-IF` over `NAME [IS] [NOT] DEFINED` or
`NAME = 'literal'` (`run_extensions-4913`, `4938`, `4963`, `5338`, `run_fundamental-105`); `>>D` before a line, the
free-form debugging line (`run_fundamental-7528`, `7551`: the `>>D` starts in column 7, so the `D` is in column 9);
`>>SET CONSTANT` and `>>DEFINE ... AS PARAMETER` (`run_misc-13305`, `13371`, `13505`: a constant the program's
text uses, a value from the command line or the environment); `>>SOURCE FORMAT` (`run_extensions-5046`, `5085`: the
rest of the file in another format); `>>TURN ... CHECKING` (`run_misc-881`: runtime checks on and off). Each case's
`case.txt` has a second check that compiles with a `-D`; the census compiles with none, so every name is undefined.

**What the step builds.** `front/Directives.java`, run by `Lines.normalize` before the lines are read: it blanks
the directive lines and the lines a false `>>IF` excludes (same length, so every line and column stays), and turns
`>>D` into a `D` indicator, which the existing debugging-line rule (R-019) then decides. `>>DEFINE name AS literal`
is read. Anything else after `>>` (`SET CONSTANT`, `DEFINE ... AS PARAMETER`, `SOURCE`, `TURN`, a condition it does
not read, an unbalanced `>>END-IF`) leaves the whole source as it was, so the parser still stops on it: an unread
directive is an error, never a silent gap (P-03). No name is predefined, so `CHARSET = 'EBCDIC'` is false.

**Predicted, before the run:**
- the seven cases with only IF-family or `>>D` directives (`4913`, `4938`, `4963`, `5338`, `run_fundamental-105`,
  `7528`, `7551`) no longer stop at `>`; the other six still do;
- NOPARSE 153 to between 146 and 149 (one of the seven may meet a different first error, `5338`'s `COMP-1` or
  `105`'s `CALL "dump"` into a C file are the likely ones);
- PASS 53 rises by 3 to 5 (`4913`, `4938`, `4963`, `7528`, `7551` print only OK, so they pass if the run is exact;
  `7528` and `7551` only if the `D` lines are comments without WITH DEBUGGING MODE); FAIL, NORUN or NOLOWER take the rest;
- no golden but `lowered.golden` changes; `front/corpus.golden` is untouched (no corpus program holds a `>>`).

The rival: reading `>>` in the grammar's lexer. The directives would then reach the parse tree, and the source map
(R-019's origins) would have to carry them through; blanking before the grammar keeps the grammar as it is forked.

## 59. 3.1, the `>>` directives amended: a name from outside the source, compiled-out code seen, IBM's own directives (J-252)

**Ghost's word (2026-10-23), after Opus reviewed R-081.** R-081 is not fail-closed in one case, and has two smaller gaps.

**1. A name's value can come from outside the source.** On z/OS the DEFINE compiler option on the compile step can
define a name that `>>IF NAME IS DEFINED` or `NAME = literal` tests. R-081 predefines nothing, so a program that relies
on it compiles the other branch without a word, and both lanes agree on it because cobc is also run without the
definition: the oracle cannot see this error. Read from the code: `Directives.condition` answers `false` for a name
nothing `>>DEFINE`d. *Choice, error or finding:* the front end reports what stops it as an error in a list
(`Preprocessor.Result.errors`, `Parse.Result.errors`; `Parse.clean()` is false and the IR is not built); findings
(`NE-`, `HY-`) are said of a program that parsed, about what the lowering cannot make exact. Which text is the program
is decided before there is a program, and a wrong answer is a wrong program, not an inexact one, so it is an error
(P-03: not silent output). It is lifted only by a declaration: the job says its compile-time definitions (a fixture
line, as `tabs` and `dfsort.pad` say theirs), and the front end takes them as an argument. A declaration may say
"none": then an undefined name is undefined by the job's word, and the census, which compiles every case with no
`-D`, says exactly that.
*The lanes:* the cobc lane builds with `Compiler`'s argument list, to which `-D name=value` can be added (to be
checked on the pinned cobc 3.1.2 with a case: a `>>IF X DEFINED` program built with and without `-D X`); the java lane
takes no compile options of its own, the front end lowering the source being its compile step, so it gets the same
definitions as an argument to the parse.

**2. Compiled-out code must be visible.** A false condition removes lines from the program that is proven, and
nothing in the report says so. *Where findings reach the report:* `ProofReport.write` takes the programs' findings
(`NonExact.findings`, per Semantic IR program) and prints them under "Findings" and "Valid under hypothesis". The
excluded ranges are known to the front end only, so the parse result carries them and `sir/check` turns them into
findings, one a range, with the directive that excluded it; the report prints them in a section of their own when
there are any (so no existing report changes).

**3. Which directives are IBM's.** Katana's dialect is IBM Enterprise COBOL. *What I recall of IBM's Language
Reference (6.4), not read this session:* its conditional-compilation directives are `>>DEFINE` (`AS literal`,
`PARAMETER`, `OFF`), `>>IF` ... `>>ELSE` ... `>>END-IF`, and `>>EVALUATE` ... `>>WHEN` ... `>>END-EVALUATE`; I recall no
`>>ELIF` among them (GnuCOBOL's and Micro Focus's), and `>>D` is a free-form debugging line of Micro Focus and
GnuCOBOL, where IBM's debugging line is a `D` in column 7 (R-019). *Could not confirm:* this session has no network
(ibm.com and publibfp.dhe.ibm.com answer 403 or do not resolve), and no copy of the manual is in the repository, so
the recollection is unverified. Settled the fail-closed way: an unconfirmed directive is not read under IBM's dialect.
`>>ELIF` and `>>D` stay errors unless the caller says the dialect allows them (the GnuCOBOL extensions, which the
autotest census is in, D-010); no fixture, and no profile field, says it yet, a profile field being a new profile hash
for every fixture (docs/PROFILE.md), so a decision to give the profile that field is proposed, not made.

**Predicted, before the build:**
- the fast loop stays green; `DirectivesTest` gains cases for the undeclared name (an error naming the name and the
  line), a declared one (read), "none" declared, `ELIF` and `>>D` as errors under IBM and read with the extensions, and
  the excluded ranges;
- the census, declaring no definitions and the extensions, is unchanged: NOPARSE 146, PASS 55, `lowered.golden` not
  rewritten; run without any declaration, the cases that test an undefined name (`4913`, `4938`, `4963` and the others
  that read a name never defined) would stop at the new error, which is the point;
- `front/corpus.golden` and the other goldens are untouched (no corpus program holds a `>>`); `-D` on cobc 3.1.2 defines
  a name `>>IF` sees, so a fixture's declaration reaches both lanes.

The rival: predefining the names IBM's compiler defines. None is known to be documented, and a name wrongly defined is
the same silent error as a name wrongly undefined.

## 60. 3.1, variably located items: what follows an OCCURS DEPENDING ON table is located after its current length (R-088; J-265)

**Ghost's word (2026-10-23).** Take TODO 3.1's variably-located-items lowering item; the four `-fodoslide` cases are its
check (R-088). `lower/Exprs.variable`'s `// Unsure:` (J-133) is where Katana keeps the maximum.

**What the existing notes quote** (`semantic_traps.md`, line 122, from IBM Enterprise COBOL 6.4 LR, OCCURS DEPENDING ON
clause, quoted there and not re-read here): "Any nonsubordinate item that follows an item described with an OCCURS
DEPENDING ON clause is a variably located item"; a sending group uses "only that part of the table area that is
specified by the object at the start of the operation"; a receiving group whose object is inside it uses "the maximum
length of the group item". `semantic_traps.md` line 147 states the consequence for the runtime (an offset computed at
each reference, the group's length as sender and as receiver). **From recollection, not read** (R-082; no manual in the
sandbox, `corpus/ibm-manuals/` absent): that an item subordinate to the same group but after the table is also located
after its current length; that LENGTH OF and FUNCTION LENGTH of such a group give its current length; that more than one
ODO table in one record is allowed in Enterprise COBOL 6; and what INITIALIZE does to a table with an ODO (current or
maximum occurrences). Where only the cases' expected output speaks, it is the evidence (the cobc `-fodoslide` run), not IBM.

**The cases, read one at a time (R-045 as amended).** Four runnable cases carry `-fodoslide`:
- `run_extensions-609` and `-739`: a group of three ODO tables (VX, VY, ZZ; objects I, J, K), 739 with a fixed item VFIX
  after the last. They display `LENGTH OF` and `FUNCTION LENGTH` of the group and the group itself at I,J,K = 1,1,1 and
  at 3,2,1: expected lengths 6 and 10 (609), 9 and 13 (739): the sum of the current lengths, the trailing item included.
  Both stop today at `not lowered: sending LENGTH OF` (a MOVE of `LENGTH OF` a group), a gap next to the slide, not the slide.
- `run_extensions-1151`: no ODO use that depends on the slide; it stops at the parser (`INITIALIZE ... ALL TO VALUE`).
- `run_file-11361`: held by `-fnotrunc` (R-087) and calls EXTFH; it cannot pass in this step.
So the check is reachable for two of the four; the other two are blocked by things that are not this item.

**Design.** An item is variably located when, in its record, an ODO table lies wholly before it in the maximum layout and
does not contain it. Its offset at a reference is its static offset plus, for each such table, (object's value - maximum)
times the occurrence length. This is one function of the item and the object values, so it belongs in one place in
`lower/` (reference sites call it) and decides nothing the IR does not (R-024): the table, its object and its bounds
are the IR's. A group's current length as sender is its maximum length plus the same sum over the tables under it
(identical to the existing `Table.current` for one table at the end; the existing text is kept for that shape). A table
nested in an OCCURS (complex ODO) stays at the maximum, with the `// Unsure:` kept for that shape only.

**Predicted, before the build:**
- the fast loop stays green;
- the census moves two cases, `run_extensions-609` and `-739`, from NORUN to PASS: 48 PASS to 50, FLAG 65, FLAGPASS 11,
  NOPARSE 102, NOLOWER 7, NOJAVAC 4, NORUN 74 to 72, FAIL 31; floors 48 and 59 raised by hand to 50 and 61 (R-086);
  `-1151` stays NOPARSE and `-11361` stays FLAG;
- no other golden changes: NIST through the Java lane, CardDemo and the passes' goldens hold no program with an item
  after an ODO table in its record (the lowering tests' ODOS and VARTRIAL have the table last), so the shift is never
  emitted for them; the one risk is a NIST program (NC247A, IX/SQ ODO records) that does, which the suites show;
- what could make it wrong: INITIALIZE of a group holding ODO tables (current or maximum occurrences) changes a byte
  the cases display; I expect not, both read the same displayed bytes.

## 61. 3.1, `ROUNDED MODE`: ten autotests that do not parse, and what accepting the phrase would need (J-268)

**Ghost's word (2026-10-24).** Take TODO 3.1's next item: the autotests that do not parse, by cause, the largest first;
`MODE` after ROUNDED leads. R-089's rule: a form is accepted only if IBM has it.

**The cases, found by searching the corpus** (`ROUNDED MODE` in every `prog.cob` under `corpus/gnucobol-autotests/cases/`, and
each case's `flags.txt`). Twelve programs hold the phrase; ten are in the 378 and end at NOPARSE on `extraneous input 'MODE'`:
`run_fundamental-4719` (AWAY-FROM-ZERO), `-4773` (AWAY-FROM-ZERO), `-4836` (NEAREST-AWAY-FROM-ZERO), `-4899` (NEAREST-EVEN),
`-4962` (NEAREST-TOWARD-ZERO), `-5025` (TOWARD-GREATER), `-5088` (TOWARD-LESSER), `-5151` (TRUNCATION), `run_misc-12977` and
`-13145` (AWAY-FROM-ZERO after a plain ROUNDED; `-13145` carries `-std=ibm`, mapped). `run_fundamental-*` and `run_misc-12977` carry no flags; none carries one the census does not
map. The two others that hold it (`data_display-16942`, `data_packed-28785`) are not in the runnable 378. (The other files a
looser search for `mode is` finds hold ACCESS MODE and the like.) The next causes are
single cases or a defect of the extraction, not an IBM form: PROGRAM-ID with no division header (`run_misc-190`, `-221`,
`run_ml-605`, `-685`, `-716`, `-753`: the program text starts at PROGRAM-ID), `procedure` after a stray `[` first line
(`run_ml-1081`, `-1170`, `-1253`, `-1328`, `-789`, `-856`: each case file begins with a line holding `[`; the `[[x]]` hypothesis of J-093 is open), `BINARY-LONG`
(4), `,` (6, in `run_misc-*`), each one-off after that.

**What IBM has: not confirmed.** Recollection, not read (R-082; `corpus/ibm-manuals/` is absent from the sandbox): Enterprise COBOL
has a `ROUNDED [MODE IS] mode` phrase with AWAY-FROM-ZERO, NEAREST-AWAY-FROM-ZERO, NEAREST-EVEN, NEAREST-TOWARD-ZERO, PROHIBITED,
TOWARD-GREATER, TOWARD-LESSER and TRUNCATION, plain ROUNDED meaning NEAREST-AWAY-FROM-ZERO. The only text in the repository is
`semantic_traps.md` line 48, which cites a COBOL 2002 tutorial, not IBM; no repository file quotes IBM on it, and the 10 cases speak
for GnuCOBOL, whose acceptance is no evidence of IBM's (D-010, R-089). So the phrase cannot be confirmed here, and the rule is: say so,
and keep it an error.

**What accepting it would also need** (the meaning, P-03). Every mode but the default is a different rounding of the stored result, and
`rt/` carries ROUNDED as one boolean from `lower/ArithVerbs` through `Decimal.store` (HALF_UP) and `Scalars.store`. The last operation
keeps one extra place under ROUNDED (`ArithProfile.last`) and truncates the rest, so a mode that must know whether anything non-zero was
dropped (TOWARD-GREATER, TOWARD-LESSER, AWAY-FROM-ZERO, NEAREST-EVEN on an exact half) cannot be decided from that cut value: a
sticky digit is lost. That is a change to Appendix A's carry rule (`rt/Decimal`, R-020), which is a design question for Ghost, and a
differential against IBM's text, which is not in the sandbox. Only NEAREST-AWAY-FROM-ZERO and TRUNCATION (no ROUNDED) are today's
two behaviours.

**Options.** (1) Accept the phrase in the grammar and the IR, and stop the lowering with a finding on every mode but
NEAREST-AWAY-FROM-ZERO: sound on meaning, but acceptance is the unconfirmed step; a case under NEAREST-AWAY-FROM-ZERO could pass, and
if IBM lacks the phrase that pass is a grammar leak (R-089). (2) Build the modes in `rt/` and lowering as well: a design change, and
unconfirmed. (3) Keep the phrase an error, record why, and pin it with a test so no change accepts it by accident (taken).

**Predicted, before the build:**
- no front-end change, so no corpus golden changes (`front/corpus.golden`, `sir/ir.golden`, `sir/datamap.golden`): none of the corpus
  programs outside the autotests holds `ROUNDED MODE` (searched: `grep -rli "ROUNDED MODE"` over `corpus/` and `oracle/` finds only the
  autotest cases);
- the census unchanged: 36 NOSOURCE, 64 FLAG, 12 FLAGPASS, 102 NOPARSE, 7 NOLOWER, 4 NOJAVAC, 72 NORUN, 31 FAIL, 50 PASS; floors 50
  and 62 as they are; the ten cases stay at NOPARSE `extraneous input 'MODE'`;
- the fast loop green with one added test (a ROUNDED MODE is a syntax error);
- what could make it wrong: a program in the NIST or CardDemo corpus that uses the phrase (the search says none).

## 62. 3.1, the reader's `[[x]]` quoting and the missing division header: two groups of autotests that do not parse (J-269)

**Ghost's word (2026-10-24).** Settle, for the two groups J-268 left (TODO 3.1), whether each is a fault of `oracle/autotest/Autotests` or
the case's real text. R-045 as amended: the reader's own extraction first (`corpus/gnucobol-autotests/cases/<case>/`), and a case's own
text in `tests/testsuite.src/<file>.at` only for the one case, by line range.

**The cases, found by scanning what the reader wrote** (every file under `cases/`, not only `prog.cob`; first character `[` and last `]`):
- *`[` first line.* J-268 counted six `run_ml-*` cases; only two begin with `[` (`run_ml-1081`, `run_ml-856`). The other four it listed
  (`-1170`, `-1253`, `-1328`, `-789`) begin with a blank line, as the rest do, and stop at `processing procedure` of an `XML PARSE`: a different
  cause. The reader writes a leading `[` line and a closing `]` line into 46 other files besides: `prog.cob` of `listings-7099`,
  `run_extensions-6264`, `run_manual_screen-1219`, `-1350`, `progs.cob` of `run_file-11361`, the C files of `backcomp-*`, `run_file-*`,
  `run_misc-*`, an expected stdout (`listings-7099`, `run_ml-856`) and expected stderr files. Every one is an argument the .at wrote as
  `[[ ... ]]`. Of the 378 runnable, only `run_ml-1081`, `run_ml-856` and `run_file-11361` (FLAG on `-fnotrunc`) are among them.
- *PROGRAM-ID with no division header.* `run_misc-190`, `-221`, `run_ml-605`, `-685`, `-716`, `-753`: the file's first text line is
  `PROGRAM-ID.`. The case's own text in the .at (read for `run_misc-190` and `run_ml-605`, lines around their `AT_DATA`) is the same: `AT_DATA([prog.cob], [`
  then a blank line and `       PROGRAM-ID.`, single-quoted, nothing to undo.

**The reading.** m4 strips a quote level when it collects a macro's arguments, and strips again when the macro's body, with the argument
substituted, is rescanned: `AT_DATA([f], [[x]])` writes `x`, `AT_DATA([f], [x])` writes `x`. The reader strips once (`quoted()` keeps inner
brackets), so `[[x]]` comes out `[x]`. This is my recollection of how Autoconf's Autotest works (the manual is not read here); the cases bear it
out without it: `run_ml-856`'s `$COMPILE prog.cob` is expected to exit 0, yet its program text begins with a line `[` (no COBOL), and its expected
stdout begins `[len: 400` where the program DISPLAYs `'len: '` and ends with a line `]`. The other `@<:@`-style brackets (`run_misc-1762`'s `|@:>@`)
are quadrigraphs for a real `]` and must stay.

**Verdicts, before the build.**
- `[` first line: a fault of the reader, a second quote level left in. The fix: in `AT_DATA`'s second argument and `AT_CHECK`'s stdout and stderr, an
  argument that is, after the first strip, one whole `[ ... ]`, is unquoted once more, before the quadrigraphs are replaced (so a real `[` from `@<:@`
  is never taken for a quote). The hypothesis of J-093 is thereby settled for these arguments.
- No division header: the case's real text. A grammar form (`IDENTIFICATION DIVISION.` header omitted). Whether IBM has it: not confirmed
  (R-082); my recollection is that Enterprise COBOL's format shows the header as required. The front end is left alone.

**Predicted, before the build:**
- the reader's unit sample passes as it is (its `[[1]]` is an inner pair, not a whole argument); one added test for the whole-argument case and one for
  a quadrigraph `[` that is not unquoted;
- `AutotestCorpusIntegrationTest`: the cases folder is rewritten; the bracketed-stdout list loses `run_ml.at:856` and keeps `run_misc.at:1762`
  (a quadrigraph); its assertion and the reader's `// Unsure:` are amended; `oracle/autotest/autotests.golden` (counts of cases, runnable,
  conditional) does not change, since the change touches text, not the shape of a case;
- the census does not move: 36 NOSOURCE, 64 FLAG, 12 FLAGPASS, 102 NOPARSE, 7 NOLOWER, 4 NOJAVAC, 72 NORUN, 31 FAIL, 50 PASS; floors 50 and 62.
  `run_ml-1081` and `run_ml-856` stay NOPARSE: a fixed-form parse already takes a `[` in the sequence area (columns 1 to 6) as nothing, so the
  `[` line was not their cause; they stop at `processing procedure`, `XML PARSE`, which the grammar lacks. `run_file-11361` stays FLAG. The only line of
  `lowered.golden` that could change is `run_ml-856`'s reason text (its trailing `]` line is gone), and I expect none does;
- the cases it can move: `run_ml-1081`, `run_ml-856`, `run_file-11361` (runnable), and nothing else in the census;
- what could make it wrong: a runnable case whose prog.cob has a `[`-wrapped whole argument that I did not find (the scan covers every file of every case).

## 63. 3.1, R-090's four steps: EXIT PERFORM [CYCLE] / PARAGRAPH / SECTION built, a `$` line, a level outside IBM's set, ACCEPT FROM and DISPLAY UPON names (J-273)

**The task (Ghost, R-090, J-272).** In this order: (1) the grammar's exit statement (`EXIT PROGRAM?`) gets `PERFORM [CYCLE]`, `PARAGRAPH`, `SECTION`;
(2) a `$` in column 7 is an error (`front/Lines.java`); (3) a level number outside 01-49, 66, 77, 88 is refused (`sir/build/DataMap.java`);
(4) ACCEPT FROM and DISPLAY UPON resolved against SPECIAL-NAMES and IBM's environment-names, else a finding (`lower/Verbs.java`). Each refusal names the
construct, IBM's set and "(recollection, R-082)": the Language Reference is not in `corpus/ibm-manuals/` here.

**The scan, before any prediction** (the program text of CardDemo, OMP, BankDemo, the NIST programs `NistSources` writes to `build/corpus-nist`, the fixtures,
`sir/golden`, `front/ibm`, the tests' inline programs, and every file of every autotest case, not only `prog.cob`; R-045: the cases' own files only).
- *`$` in column 7.* None in CardDemo, OMP, BankDemo, NIST, the fixtures, the goldens' programs or the tests. In the autotests, 16 cases hold one in a
  `.cob`; 8 of them runnable, and in only two is it `prog.cob`, the file the census compiles: `run_subscripts-437` (`$SET BOUND`; PASS today) and
  `run_subscripts-506` (`$SET NOSSRANGE`, `$SET SSRANGE`; FAIL `ssrange`). The other six runnable ones hold it in a second program the census never compiles
  (`run_extensions-5257` `prog2.cob`, `run_file-11361` `progs.cob`, `syn_misc-2023` `prog2.cob`, and `backcomp-816`, `data_binary-23`, `-205` are NOSOURCE).
- *A level outside IBM's set.* Only 78 appears (2416 lines in 83 cases; no 00, 50-65, 67-76, 79-87, 89-99 anywhere). Outside the autotests: **BankDemo's
  `DFHUAINS.cbl` line 94, `78 78-srv-cmsg-entry-name value 'SRV_CONSOLE_MSG'`**; none in CardDemo, OMP, NIST (the `66`, `69` and `SECTION 50` the scan
  catches are an alphabet's ordinals and section names), the fixtures or the goldens' programs. Of the 83 cases, the runnable ones by stage today: FAIL
  `level78` 3 (`run_extensions-2831`, `-2854`, `-2880`); NORUN 2 (`run_file-12428`, `run_fundamental-3375`); NOLOWER 2 (`data_packed-1971`, `-2083`);
  FLAG about 20; NOPARSE about 11; the rest not reached.
- *ACCEPT FROM / DISPLAY UPON a name.* In the autotests: `STDOUT` (`run_extensions-101`, FLAG; `run_fundamental-7502`, NOPARSE), `PRINTER`
  (`run_misc-13585`, NOJAVAC), `ENVIRONMENT-NAME`/`-VALUE`/`ARGUMENT-VALUE`/`ARGUMENT-NUMBER` (`run_extensions-2785` FAIL `env-name`; `run_misc-13305` and
  `-13371` FLAG), `MICROSECOND-TIME` (`run_accept-141`, NOPARSE), and `SYSERR`, `SYSOUT`, `CONSOLE`, `SYSPCH`, `SYSPUNCH` in cases at other stages. In the
  NIST programs: `UPON DISPLAY-OUTPUT-DEVICE` (NC220M), `UPON THE-SYSTEM-PRINTER` (DB203A), `UPON VDUNIT` (NC401M), `ACCEPT ... FROM ACCEPT-INPUT-DEVICE`,
  `INPUT-DEVICE`, `SYSIN`: mnemonics, which SPECIAL-NAMES must resolve. **BankDemo's `DFHZCSGM.cbl` has `DISPLAY ... UPON ENVIRONMENT-NAME` (3) and
  `ACCEPT ... FROM ENVIRONMENT-VALUE` (1).** CardDemo and OMP: none.
- *EXIT forms.* In the autotests, runnable: `run_misc-2433` (`EXIT PERFORM CYCLE`), `run_misc-2410` (`EXIT PERFORM`), `run_file-5542` (`PERFORM UNTIL EXIT`
  and `AT END EXIT PERFORM`), `run_file-13907`, `run_misc-14060`, `-14111`, `-14367` (`EXIT PERFORM`), `run_misc-15280` (`EXIT PARAGRAPH`, `EXIT SECTION`).
  None in CardDemo, OMP, BankDemo or NIST (its `CCVS-EXIT SECTION.` is a section name, one word).

**The guard.** BankDemo is held by `front/corpus.golden` (`corpus bankdemo 82/82`, parse only) and its IR is built and counted privately by
`CorpusIntegrationTest` (printed, not asserted); no lane lowers it. Steps 3 and 4 touch two of its programs. R-090 did not weigh BankDemo; if steps 3 or 4
would move a golden or lane over a program they hold, they wait for Ghost. Steps 1 and 2 touch no program outside the autotests.

**The reading, step 1.** `EXIT PERFORM` leaves the innermost inline PERFORM; `EXIT PERFORM CYCLE` ends the current cycle and "continues with the next
iteration": for TIMES the count is decremented as at the end of a body, for UNTIL the condition is tested next, for VARYING the step runs first and then the
test. (My recollection of Enterprise COBOL 6.x, ~85%, R-082; cobc -std=ibm accepts it: the differential below holds the meaning, not the census case.) EXIT
PARAGRAPH goes to the end of the paragraph, EXIT SECTION to the end of the section; the lowering already writes both (`Verbs.exit`, by a keyword the grammar
never produces), so they are a grammar gap.

**Predicted, before the build.**
- Step 1 (grammar `exitStatement`, `Verbs.exit`, `Performs`): `run_misc-2433` FAIL to PASS, `run_misc-2410` NOPARSE to PASS (its error `extraneous input '.\n'`
  is `PERFORM` read as a statement after `EXIT`); census PASS 50 to 52 (PASS+FLAGPASS 62 to 64), floors raised by hand to 52 and 64 (more only for a named 'could move' case that passes). Could move, stage unknown: `run_misc-14060` and
  `-14111` (the `exit perform` inside ON SIZE ERROR; they may parse past it and stop at FLOAT or UPON SYSERR), `run_misc-15280` (FLAG, may become FLAGPASS) and
  `run_file-13907` (FLAG, may become FLAGPASS). Will not move: `run_file-5542` (its `UNTIL EXIT` is not IBM's; NOPARSE stays, perhaps with another message),
  `run_misc-14367` (ENTRY). Goldens besides the census and the corpus: none changes (no program of CardDemo, OMP, NIST, BankDemo or the fixtures has the forms).
  A differential program in `test/katana/lower/programs/` (TIMES, UNTIL and VARYING with EXIT PERFORM CYCLE; nested loops; the step run) agrees with cobc -std=ibm.
- Step 2 (`Lines.normalize` adds an error): `run_subscripts-437` PASS to NOPARSE, `run_subscripts-506` FAIL to NOPARSE; after step 2 PASS 52 to 51 and PASS+FLAGPASS 64 to 63 (437 alone; 50 and 62 before step 1): the floor lowered by hand, reason "`$SET BOUND` was blanked; the pass was a coincidence (R-090)". 506's FAIL
  line (`ssrange`) goes. No other golden changes: no `$` line in any program they hold.
- Step 3 (not built unless Ghost rules on BankDemo): would move `run_extensions-2831`, `-2854`, `-2880` FAIL to NOLOWER, `run_file-12428` and
  `run_fundamental-3375` NORUN to NOLOWER; the FLAG cases stay FLAG; BankDemo `DFHUAINS` would no longer build, in the private IR count only (the parse golden
  `bankdemo 82/82` is unchanged).
- Step 4 (not built unless Ghost rules): would move `run_extensions-2785` FAIL to NOLOWER (a finding), maybe `run_misc-13305`/`-13371` (FLAG, stay); the NIST
  lane's mnemonics must still resolve; BankDemo `DFHZCSGM` is parsed only.
- The fast loop green after each step. After steps 1 and 2 the census, by stage: PASS 51 (50 +2433 +2410 -437); FAIL 29 (-2433, -506); NOPARSE 103
  (-2410, +437, +506); PASS+FLAGPASS 63; the other stages as they are unless a named 'could move' case moves.
- What could make it wrong: a `$` or EXIT form in a copybook a case pulls from outside its folder; `EXIT PERFORM` inside a PERFORM of a paragraph (not inline): IBM
  allows EXIT PERFORM only in an inline PERFORM, so the build must refuse or ignore it; the census's FLAG cases whose stage I cannot read from the golden alone.

## 64. 3.1, R-090's steps 3 and 4 after Ghost's word: a level outside IBM's set, and the names ACCEPT FROM and DISPLAY UPON take (J-277)

**The task.** Ghost's word (2026-10-24, on J-275's question): BankDemo's `DFHUAINS.cbl` (a level 78) and `DFHZCSGM.cbl` (`DISPLAY UPON ENVIRONMENT-NAME`, `ACCEPT FROM
ENVIRONMENT-VALUE`) do not stop these steps; go ahead. (3) `sir/build/DataMap.java` refuses a level number outside 01-49, 66, 77, 88, in the IR build; (4) `lower/Verbs.java` resolves
ACCEPT FROM and DISPLAY UPON against SPECIAL-NAMES and IBM's environment-names, else a finding with `Unsupported.fail`. Each refusal names the construct, IBM's set and
"(recollection, R-082)": the Language Reference is not in `corpus/ibm-manuals/`. Section 63 predicted both before Ghost's word; this section repeats the scan from the files
and the census golden, with the stage a throw and a finding each give, which 63 did not separate.

**The scan, repeated.** Every file of every runnable autotest case (a `.cob`, `.cbl`, `.cpy` in the case's folder, not only `prog.cob`; R-045), and CardDemo, OMP, BankDemo,
the NIST programs `CorpusIntegrationTest` writes, the fixtures, `sir/golden`, `front/ibm`, `oracle/` and the tests' inline programs.
- *Levels.* Only 78 is a level number anywhere (the 91, 60, 64 and 65 a looser scan finds in `run_initialize-655`, `run_reportwriter-9346` and `run_misc-16006` are table values,
  a REPORT's COLUMNS and a THRU range). Outside the autotests: BankDemo `DFHUAINS.cbl` (parse only in `front/corpus.golden`; its IR is built and counted privately, and it
  already does not build, `comp-x`, so `built 81` of 82 stays). None in CardDemo, OMP, NIST, the fixtures, `sir/golden`, `oracle/` or the tests. In the autotests, 31 runnable
  cases hold a 78 as a level; their stages in `lowered.golden`: FAIL (`level78`) `run_extensions-2831`, `-2854`, `-2880`; NORUN `run_file-12428`, `run_fundamental-3375`; NOLOWER
  `data_packed-1971`, `-2083`; FLAG 14; NOPARSE 10 (and `run_misc-15671`'s second program, which the census never builds).
- *Names.* ACCEPT FROM / DISPLAY UPON targets, outside the date and time words: CardDemo `ACCEPT ... FROM SYSIN` (COBSWAIT, CBPAUP0C, no SPECIAL-NAMES: SYSIN must resolve as an
  IBM environment-name); BankDemo `UPON CONSOLE` (six, IBM's) and `UPON ENVIRONMENT-NAME`/`ACCEPT FROM ENVIRONMENT-VALUE` (`DFHZCSGM`, parse and IR only, no lane lowers it); the NIST
  programs `UPON DISPLAY-OUTPUT-DEVICE`, `THE-SYSTEM-PRINTER`, `VDUNIT` and `ACCEPT FROM ACCEPT-INPUT-DEVICE`, each declared in its SPECIAL-NAMES (`SYSOUT IS ...`); the fixtures and
  `sir/golden`: none; the lane's `Generated.java` writes `UPON SYSERR`. In the autotests (29 runnable cases hold an UPON or a FROM name): `SYSERR` (13 cases, `run_misc-1075` PASS),
  `CONSOLE` (10), `SYSOUT` (4), `SYSPCH`/`SYSPUNCH` (`run_misc-13585`), `PRINTER` (`run_misc-13585`, declared `PRINTER IS PRINTER`), `STDOUT` (`run_extensions-101` FLAG,
  `run_fundamental-7502` NOPARSE), `ENVIRONMENT-NAME`/`-VALUE`, `ARGUMENT-VALUE` (`run_extensions-2785` FAIL, `run_misc-13305` and `-13371` FLAG), `MICROSECOND-TIME`
  (`run_accept-141` NOPARSE, already refused by `Verbs.accept`).

**The reading (recollection, R-082).** IBM's level numbers: 01-49, 66, 77, 88 (about 95%). IBM's environment-names: for ACCEPT FROM, SYSIN, SYSIPT and CONSOLE; for DISPLAY UPON,
SYSOUT, SYSLIST, SYSLST, SYSPUNCH, SYSPCH and CONSOLE, and SYSERR, which the lowering already writes to the error stream and the lane's own programs use (about 70% that
the Language Reference lists SYSERR for DISPLAY: kept because dropping it would take `run_misc-1075`'s pass and the lane's abend messages away on a doubt). A name is IBM's
by these lists or by a SPECIAL-NAMES mnemonic (the mnemonic's own environment-name is not checked here). The lowering's writing of each (all to SYSOUT but SYSERR) is not changed.

**Predicted, before the build.**
- Step 3 (`DataMap.format1` throws `IllegalArgumentException` for a level outside the set; the census's `build` then ends at NOLOWER, since the parse was clean): FAIL to NOLOWER
  `run_extensions-2831`, `-2854`, `-2880`; NORUN to NOLOWER `run_file-12428`, `run_fundamental-3375`. Unchanged: `data_packed-1971`, `-2083` (already NOLOWER), the 14 FLAG (FLAG
  holds whatever the raw stage that is not PASS), the 10 NOPARSE (the parse fails first). Census by stage: FAIL 29 to 26, NORUN 72 to 70, NOLOWER 7 to 12; PASS 51 and
  PASS+FLAGPASS 63 unchanged, so the floors stay. `FailSorting`'s three `level78` lines and the `# sorting level78` line go. No other golden changes: no program any
  other golden holds has a level outside the set (BankDemo's is parse-only there, and its private IR count stays 81 of 82).
- Step 4 (a finding, `Unsupported.fail`, which stops the run when reached, so the stage is NORUN, not NOLOWER as 63 guessed): `run_extensions-2785` FAIL to NORUN (its first statement is
  `DISPLAY "TEST_ENV" UPON ENVIRONMENT-NAME`). Unchanged: `run_misc-13305`, `-13371` (FLAG), `run_extensions-101` (FLAG), `run_fundamental-7502`, `run_accept-141` (NOPARSE),
  `run_misc-13585` (NOJAVAC at its own cause; PRINTER is declared), every SYSERR, CONSOLE, SYSOUT, SYSPCH and SYSPUNCH case (IBM's), `run_misc-1075` (PASS stays), CardDemo's
  `FROM SYSIN`. Census: FAIL 26 to 25, NORUN 70 to 71 after step 3; `FailSorting`'s `env-name` line and its sorting line go. The NIST java lane's output, the fixture lane, CardDemo's goldens
  and every other golden do not change: each name they use is a declared mnemonic or an IBM name above. If the NIST lane moves, a mnemonic is not resolved and that is the bug.
- What could make it wrong: a case's FLAG that is really a FLAGPASS under the new throw (none of the 31 is a PASS today, so no); a copybook of a case holding a 78 (the scan read every file of the case);
  the display-upon or accept-from operand of a declared mnemonic arriving as another value kind than `mnemonic`.
