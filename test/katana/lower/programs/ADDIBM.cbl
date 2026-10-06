       IDENTIFICATION DIVISION.
       PROGRAM-ID. ADDIBM.
      * NIST NC177A's ADD-TEST-F2-11 to -13 (NC118A's SIG-TEST-GF-16 to
      * -18): the same seven addends in three orders, every intermediate
      * well under 30 digits, so Appendix A carries each sum exactly
      * (PG 6.4 p. 795); NC250A's IF--TEST-37, two exact sums of 123.45;
      * and NC175A's SUB-TEST-F2-6 (NC119A's SUB-TEST-GF-14), an exact
      * difference within its receiver.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       77 A PIC S99 VALUE 99.
       77 B PIC S99V9 VALUE 11.1.
       77 C PIC S999V999 VALUE 111.111.
       77 D PIC S99V9(6) VALUE 22.222222.
       77 E PIC SP(8)9 VALUE .000000001.
       77 R PIC S999V9(10).
       77 S PIC 999.9(10).
       77 TWENTY PIC 99 VALUE 20.
       77 F PIC S9(5) VALUE 11111.
       77 G PIC SV9(5) VALUE .11111.
       77 H PIC S9(6)V9(6) VALUE 333333.333333.
       77 K PIC S999V999 VALUE 333.333.
       77 Q PIC S9(6)V9(6).
       77 QE PIC -9(6).9(6).
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE ZERO TO R
           ADD A B C D -1.1111111 +.11111111 E GIVING R
           MOVE R TO S
           DISPLAY "SUM " S
           ADD E +.11111111 -1.1111111 D C B A GIVING R
           MOVE R TO S
           DISPLAY "SUM " S
           ADD D A -1.1111111 B E +.11111111 C GIVING R
           MOVE R TO S
           DISPLAY "SUM " S
           IF 100 + (TWENTY + 3.4) + .05 EQUAL TO
              .05 + (100 + TWENTY) + 3.4
               DISPLAY "EQUAL" ELSE DISPLAY "NOT EQUAL" END-IF
           SUBTRACT F G H K FROM ZERO GIVING Q
           MOVE Q TO QE
           DISPLAY "SUB " QE
           STOP RUN.
