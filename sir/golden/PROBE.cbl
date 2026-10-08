      * Tithe pass 2, A1: every keyword-bearing construct the IR once dropped, in one program;
      * A3: each kind of literal, and VALUEs with their spaces; A4: a mask, the flags, a RELATIVE KEY.
      * SIR-09 and SIR-10: OPTIONAL, both FILE STATUS items, a USE.
      * SIR-11: an FD's clauses, LINAGE by integers and by items.
       IDENTIFICATION DIVISION.
       PROGRAM-ID. PROBE.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT REL-FILE ASSIGN TO "REL" ORGANIZATION RELATIVE
               ACCESS RANDOM RELATIVE KEY RK.
           SELECT OPTIONAL SEQ-FILE ASSIGN TO "SEQ"
               FILE STATUS IS FS VS.
           SELECT LIST-FILE ASSIGN TO "LIST".
           SELECT VAR-FILE ASSIGN TO "VAR".
       DATA DIVISION.
       FILE SECTION.
       FD  REL-FILE.
       01  REL-REC      PIC X(10).
       FD  SEQ-FILE IS GLOBAL
           LABEL RECORDS ARE STANDARD
           BLOCK CONTAINS 10 RECORDS
           RECORD CONTAINS 10 CHARACTERS
           RECORDING MODE IS F
           DATA RECORD IS SEQ-REC.
       01  SEQ-REC      PIC X(10).
       FD  LIST-FILE
           LINAGE IS 60 LINES WITH FOOTING AT 55
           LINES AT TOP 2 LINES AT BOTTOM 4.
       01  LIST-REC     PIC X(20).
       FD  VAR-FILE
           RECORD IS VARYING IN SIZE FROM 1 TO 30 CHARACTERS
               DEPENDING ON VLEN
           LINAGE IS PAGE-LINES LINES WITH FOOTING AT FOOT-LINE.
       01  VAR-REC      PIC X(30).
       WORKING-STORAGE SECTION.
       01  A            PIC S9(5)V99.
       01  B            PIC S9(5)V99.
       01  C            PIC 9(3).
       01  D            PIC 9(3).
       01  N            PIC 9(3).
       01  X            PIC X(10).
       01  Y            PIC X(20) VALUE 'A  ''B''  C'.
       01  Z            PIC S9(3)V99 VALUE -1.50.
           88  Z-LOW    VALUE -999.99 THRU 0, 1.
       01  RK           PIC 9(4).
       01  FS           PIC XX.
       01  VS           PIC X(6).
       01  VLEN         PIC 99.
       01  PAGE-LINES   PIC 99 VALUE 20.
       01  FOOT-LINE    PIC 99 VALUE 18.
       01  M            PIC $$,$$9.99CR.
       01  J            PIC X(5) JUSTIFIED RIGHT.
       01  K            PIC 9(4) BLANK WHEN ZERO.
       01  EXT          PIC X EXTERNAL.
       01  GLB          PIC XXBX/0 GLOBAL.
       01  G1.
           05  F1       PIC X.
           05  F2       PIC 9.
       01  G2.
           05  F1       PIC X.
           05  F2       PIC 9.
       PROCEDURE DIVISION.
       DECLARATIVES.
       SEQ-ERRORS SECTION.
           USE AFTER STANDARD ERROR PROCEDURE ON SEQ-FILE.
       SEQ-SAID.
           DISPLAY FS.
       INPUT-ERRORS SECTION.
           USE GLOBAL AFTER EXCEPTION PROCEDURE INPUT.
       END DECLARATIVES.
       MAIN-SECTION SECTION.
       MAIN.
           COMPUTE A ROUNDED = B * (C + 1) ** 2 / - D
               ON SIZE ERROR DISPLAY 'SIZE'
           END-COMPUTE
           ADD 1 TO A ROUNDED
           EVALUATE A ALSO TRUE
               WHEN 1 ALSO ANY
               WHEN 2 THRU 5 ALSO B > 0
                   DISPLAY 'ONE'
               WHEN OTHER
                   DISPLAY 'OTHER'
           END-EVALUATE
           INSPECT X TALLYING N FOR ALL 'A' LEADING 'B'
           STRING X DELIMITED BY SIZE INTO Y
           PERFORM P1 WITH TEST AFTER UNTIL A > 1 AND < 5
           IF A NOT = B AND C IS NUMERIC
               MOVE CORRESPONDING G1 TO G2
           ELSE
               MOVE SPACES TO X
           END-IF
           DISPLAY X'C1F0' Z'AB' N'NX' "Q""Q" +.5 -12.250 1.5E+3 .25E-2
           STOP RUN.
       P1.
           SUBTRACT 1 FROM A.
