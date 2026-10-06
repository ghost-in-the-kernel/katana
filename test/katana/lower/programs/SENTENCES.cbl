       IDENTIFICATION DIVISION.
       PROGRAM-ID. SENTENCES.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 TBL.
          05 ENT PIC X(2) OCCURS 4 INDEXED BY IX.
       01 N PIC 9 VALUE 1.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE "AABBCCDD" TO TBL
           IF N = 1 NEXT SENTENCE ELSE DISPLAY "ELSE 1"
           DISPLAY "SKIPPED 1".
           DISPLAY "AFTER 1"
           IF N = 2 NEXT SENTENCE ELSE DISPLAY "ELSE 2".
           DISPLAY "AFTER 2"
           SET IX TO 1
           SEARCH ENT
               AT END DISPLAY "END"
               WHEN ENT (IX) = "CC" NEXT SENTENCE
           END-SEARCH
           DISPLAY "SKIPPED 3".
           DISPLAY "AFTER 3 " ENT (IX)
           PERFORM NEXT-PARA
           DISPLAY "BACK".
       NEXT-PARA.
           IF N = 1
               DISPLAY "IN NEXT-PARA"
               NEXT SENTENCE
           END-IF
           DISPLAY "SKIPPED 4".
       LAST-PARA.
           DISPLAY "LAST"
           STOP RUN.
