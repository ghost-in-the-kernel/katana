       IDENTIFICATION DIVISION.
       PROGRAM-ID. EXITS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 I PIC 99 VALUE 0.
       01 J PIC 99 VALUE 0.
       01 K PIC 99 VALUE 0.
       PROCEDURE DIVISION.
       MAIN-PARA.
      * EXIT PERFORM CYCLE: the rest of the body is skipped and the loop
      * goes on to its next cycle (VARYING's step run, UNTIL tested).
           DISPLAY "TIMES"
           MOVE 0 TO K
           PERFORM 3 TIMES
               ADD 1 TO K
               DISPLAY "T " K
               EXIT PERFORM CYCLE
               DISPLAY "NOT OK"
           END-PERFORM
           DISPLAY "UNTIL"
           MOVE 0 TO I
           PERFORM UNTIL I >= 5
               ADD 1 TO I
               IF I = 2
                   EXIT PERFORM CYCLE
               END-IF
               IF I = 4
                   EXIT PERFORM
               END-IF
               DISPLAY "U " I
           END-PERFORM
           DISPLAY "AFTER U " I
           DISPLAY "UNTIL AFTER"
           MOVE 0 TO I
           PERFORM WITH TEST AFTER UNTIL I >= 3
               ADD 1 TO I
               DISPLAY "A " I
               EXIT PERFORM CYCLE
               DISPLAY "NOT OK"
           END-PERFORM
           DISPLAY "AFTER A " I
           DISPLAY "VARYING"
           PERFORM VARYING I FROM 1 BY 1 UNTIL I > 5
               IF I = 3
                   EXIT PERFORM CYCLE
               END-IF
               DISPLAY "V " I
           END-PERFORM
           DISPLAY "AFTER V " I
           PERFORM VARYING I FROM 1 BY 2 UNTIL I > 9
               IF I = 5
                   EXIT PERFORM
               END-IF
               DISPLAY "W " I
           END-PERFORM
           DISPLAY "AFTER W " I
           PERFORM WITH TEST AFTER VARYING I FROM 1 BY 1 UNTIL I >= 3
               IF I = 2
                   EXIT PERFORM CYCLE
               END-IF
               DISPLAY "VT " I
           END-PERFORM
           DISPLAY "AFTER VT " I
           DISPLAY "VARYING AFTER"
           PERFORM WITH TEST AFTER VARYING I FROM 1 BY 1 UNTIL I >= 2
                   AFTER J FROM 1 BY 1 UNTIL J >= 3
               IF J = 2
                   EXIT PERFORM CYCLE
               END-IF
               DISPLAY "VA " I " " J
           END-PERFORM
           DISPLAY "AFTER VA " I " " J
           PERFORM VARYING I FROM 1 BY 1 UNTIL I > 3
                   AFTER J FROM 1 BY 1 UNTIL J > 3
               IF J = 2
                   EXIT PERFORM CYCLE
               END-IF
               IF I = 2 AND J = 3
                   EXIT PERFORM
               END-IF
               DISPLAY "VB " I " " J
           END-PERFORM
           DISPLAY "AFTER VB " I " " J
           DISPLAY "NESTED"
           MOVE 0 TO K
           PERFORM 2 TIMES
               ADD 1 TO K
               MOVE 0 TO J
               PERFORM UNTIL J >= 4
                   ADD 1 TO J
                   IF J = 2
                       EXIT PERFORM CYCLE
                   END-IF
                   IF J = 3
                       EXIT PERFORM
                   END-IF
                   DISPLAY "N " K " " J
               END-PERFORM
               DISPLAY "OUT " K " " J
           END-PERFORM
           DISPLAY "ONCE"
           PERFORM
               DISPLAY "O 1"
               EXIT PERFORM
               DISPLAY "NOT OK"
           END-PERFORM
           PERFORM
               DISPLAY "O 2"
               EXIT PERFORM CYCLE
               DISPLAY "NOT OK"
           END-PERFORM
           DISPLAY "CALLS"
           MOVE 0 TO K
           PERFORM 3 TIMES
               ADD 1 TO K
               PERFORM SHOW
               IF K = 2
                   EXIT PERFORM CYCLE
               END-IF
               DISPLAY "C END " K
           END-PERFORM
           MOVE 0 TO I
           PERFORM UNTIL I >= 4
               ADD 1 TO I
               PERFORM SHOW
               IF I = 2
                   EXIT PERFORM CYCLE
               END-IF
               IF I = 3
                   EXIT PERFORM
               END-IF
               DISPLAY "CU " I
           END-PERFORM
           DISPLAY "AFTER CU " I
           MOVE 0 TO I
           PERFORM WITH TEST AFTER UNTIL I >= 3
               ADD 1 TO I
               PERFORM SHOW
               EXIT PERFORM CYCLE
               DISPLAY "NOT OK"
           END-PERFORM
           DISPLAY "AFTER CA " I
           PERFORM VARYING I FROM 1 BY 1 UNTIL I > 4
               PERFORM SHOW
               IF I = 2
                   EXIT PERFORM CYCLE
               END-IF
               IF I = 4
                   EXIT PERFORM
               END-IF
               DISPLAY "CV " I
           END-PERFORM
           DISPLAY "AFTER CV " I
           PERFORM WITH TEST AFTER VARYING I FROM 1 BY 1 UNTIL I >= 3
                   AFTER J FROM 1 BY 1 UNTIL J >= 2
               PERFORM SHOW
               IF J = 1
                   EXIT PERFORM CYCLE
               END-IF
               DISPLAY "CW " I " " J
           END-PERFORM
           DISPLAY "AFTER CW " I " " J
           PERFORM
               PERFORM SHOW
               EXIT PERFORM
               DISPLAY "NOT OK"
           END-PERFORM
           MOVE 3 TO K
           PERFORM SECT-A
           MOVE 2 TO K
           PERFORM SECT-A
           DISPLAY "END"
           STOP RUN.
       SHOW.
           DISPLAY "SHOW".
       SECT-A SECTION.
       PARA-A.
           DISPLAY "PARA-A"
           EXIT PARAGRAPH
           DISPLAY "NOT OK".
       PARA-B.
           DISPLAY "PARA-B"
           IF K = 2
               EXIT SECTION
           END-IF
           DISPLAY "B END".
       PARA-C.
           DISPLAY "PARA-C".
