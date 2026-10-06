       IDENTIFICATION DIVISION.
       PROGRAM-ID. NOTRSVD.
      * NOTHING is not one of IBM's reserved words (LR 6.4 Appendix E,
      * Table 83, p. 770: NO, NOT, NULL, NULLS), so it names data.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       77 NOTHING PIC X(4) VALUE "ZILC".
       PROCEDURE DIVISION.
       MAIN-PARA.
           DISPLAY "NOTHING " NOTHING
           STOP RUN.
