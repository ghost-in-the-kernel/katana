       IDENTIFICATION DIVISION.
       PROGRAM-ID. ADDIBM.
      * NIST NC177A's ADD-TEST-F2-11: every intermediate of the ADD is
      * well under 30 digits, so Appendix A carries the sum exactly
      * (PG 6.4 p. 795).
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       77 A PIC S99 VALUE 99.
       77 B PIC S99V9 VALUE 11.1.
       77 C PIC S999V999 VALUE 111.111.
       77 D PIC S99V9(6) VALUE 22.222222.
       77 E PIC SP(8)9 VALUE .000000001.
       77 R PIC S999V9(10).
       77 S PIC 999.9(10).
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE ZERO TO R
           ADD A B C D -1.1111111 +.11111111 E GIVING R
           MOVE R TO S
           DISPLAY "SUM " S
           STOP RUN.
