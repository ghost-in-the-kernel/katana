       IDENTIFICATION DIVISION.
       PROGRAM-ID. FLOATS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 W PIC $$,$$$,$$$,$$$,$$$,$$$.99.
       01 V PIC S9(16)V99.
       01 TBL-001.
           02 FILLER PIC S9(16)V99 VALUE 9999.11.
           02 FILLER PIC S9(16)V99 VALUE 1010101.99.
       01 TBL-001-R REDEFINES TBL-001.
           02 TBL-001-O PIC S9(16)V99 OCCURS 2 TIMES.
       01 TBL-002.
           02 FILLER PIC X(25) VALUE "                     $.00".
           02 FILLER PIC X(25) VALUE "                     $.01".
       01 TBL-002-R REDEFINES TBL-002.
           02 TBL-002-O PIC X(25) OCCURS 2 TIMES.
       01 K PIC 9 VALUE 1.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE ZERO TO V
           MOVE V TO W
           DISPLAY "[" W "]"
           MOVE .01 TO V
           MOVE V TO W
           DISPLAY "[" W "]"
           MOVE 9999.11 TO V
           MOVE V TO W
           DISPLAY "[" W "]"
           MOVE 9999999999.99 TO V
           MOVE V TO W
           DISPLAY "[" W "]"
           MOVE TBL-001-O (2) TO W
           DISPLAY "T1 " W
           DISPLAY "T2 [" TBL-002-O (1) "]"
           MOVE ZERO TO TBL-001-O (1)
           MOVE TBL-001-O (K) TO W
           IF W EQUAL TO TBL-002-O (K) DISPLAY "EQ"
               ELSE DISPLAY "NE" END-IF
           STOP RUN.
