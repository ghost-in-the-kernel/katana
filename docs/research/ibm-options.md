# IBM Enterprise COBOL dialect options: sourced facts

Collected 2026-10-04 for Katana's dialect profile. Each fact is tagged **[verbatim]** (quoted
from the named source), **[paraphrase]** (the source's content, reworded by the digest tool),
or **[unverified]** (recollection of the Programming Guide, not reached this session; treat as
a hypothesis to confirm against the manual).

**Access note.** The Programming Guide (PG) and Language Reference (LR) PDFs are reachable but the
fetch tool digests only their first ~30 pages (the contents), so the PG option chapter and
Appendix A were not read; `ibm.com/docs` HTML is 403 except a few indexed pages. Primary sources
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

## 4. Appendix A, "Intermediate results and arithmetic precision"

Not reached in any primary form this session (PG PDF digest stops at the TOC; `ibm.com/docs`
HTML 403; cadcobol.com.br mirror suspended; Micro Focus and Wayback disallowed). What follows is
**[unverified]** recollection of the 3.x-6.x text, which is essentially unchanged across versions
except for 30 -> 31 under ARITH(EXTEND):

- Terms: i1,i2 / d1,d2 = integer / decimal digits of op1, op2; ir = intermediate result; dmax =
  in a given statement, the greatest of the decimal places of the final receiver(s) (plus 1 when
  ROUNDED is specified), the decimal places of any operand except divisors and exponents, and the
  dmax of any containing expression.
- Fixed-point ir digits: `+`/`-`: i = max(i1,i2)+1, d = max(d1,d2). `*`: i = i1+i2, d = d1+d2.
  `/`: i = i1+d2, d = max(d1-d2, dmax). `**` with an integer literal exponent n: i = i1*n,
  d = d1*n (treated as repeated multiplication); `**` with a data-name or non-integer exponent:
  floating point.
- Cap: if i+d > 30 (COMPAT) / 31 (EXTEND): if d > dmax, d is reduced to max(dmax, 30-i) (31-i);
  if i+d still exceeds the cap, high-order integer digits are lost (size error). Division is
  computed with d = max(d1-d2, dmax) so quotients carry at least dmax places.
- Floating point is used when any operand or receiver is COMP-1/COMP-2/external float or a
  floating-point literal, or when an exponent has decimal places or is a non-literal.
- In conditions, dmax is the maximum decimal places of all operands on both sides; in
  non-arithmetic statements (subscripts, reference modification, etc.) the expression is
  evaluated as if assigned to an integer receiver with dmax = max operand decimals.

Secondary confirmation only: [paraphrase, datafield.dev ch. 5, S12] `*` sums integer and decimal
digits of both operands. [paraphrase, APARs PH41526/PH42579, S13] compiler abends on COMPUTE ...
ROUNDED with intermediate results "of more than 31 digits" (31 is the EXTEND ceiling).

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

