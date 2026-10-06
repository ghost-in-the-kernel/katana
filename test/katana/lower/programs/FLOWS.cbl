       IDENTIFICATION DIVISION.
       PROGRAM-ID. FLOWS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 K PIC 9 VALUE 2.
       01 C PIC 9(2) VALUE 0.
       01 V PIC 9(3) VALUE 15.
       PROCEDURE DIVISION.
       START-P.
           GO TO A1 A2 A3 DEPENDING ON K.
           DISPLAY "FELL".
       A1.
           DISPLAY "A1".
       A2.
           DISPLAY "A2".
           ALTER SW TO PROCEED TO B2.
           PERFORM SW-P THRU SW-END.
           EVALUATE TRUE
               WHEN V < 10 DISPLAY "SMALL"
               WHEN V < 20 DISPLAY "MEDIUM"
               WHEN OTHER DISPLAY "LARGE"
           END-EVALUATE
           EVALUATE V ALSO K
               WHEN 1 THRU 9 ALSO ANY DISPLAY "ONE"
               WHEN 15 ALSO 2 DISPLAY "FIFTEEN-TWO"
               WHEN OTHER DISPLAY "OTHER"
           END-EVALUATE
           PERFORM B1 3 TIMES
           DISPLAY C
           STOP RUN.
       A3.
           DISPLAY "A3".
       SW-P.
       SW.
           GO TO B1.
       B1.
           ADD 1 TO C.
       B2.
           DISPLAY "B2".
       SW-END.
           EXIT.
