       IDENTIFICATION DIVISION.
       PROGRAM-ID. SEARCHES.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 TBL.
          05 ENT OCCURS 6 ASCENDING KEY IS ENT-KEY INDEXED BY IX JX.
             10 ENT-KEY PIC 9(3).
             10 ENT-VAL PIC X(2).
       01 N PIC 9(3) VALUE 0.
       01 K PIC 9(3) VALUE 0.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE "010AA020BB030CC040DD050EE060FF" TO TBL
           SET IX TO 1
           SEARCH ENT
               AT END DISPLAY "END 1"
               WHEN ENT-VAL (IX) = "CC" DISPLAY "FOUND CC " ENT-KEY (IX)
           END-SEARCH
           SET IX TO 1
           SEARCH ENT
               WHEN ENT-KEY (IX) > 35 DISPLAY "GT35 " ENT-VAL (IX)
               WHEN ENT-VAL (IX) = "BB" DISPLAY "BB FIRST"
           END-SEARCH
           SET IX TO 5
           SEARCH ENT
               AT END DISPLAY "END 2"
               WHEN ENT-VAL (IX) = "AA" DISPLAY "NO"
           END-SEARCH
           SET IX TO 7
           SEARCH ENT
               AT END DISPLAY "END 3"
               WHEN ENT-VAL (IX) = "FF" DISPLAY "NO"
           END-SEARCH
           SET JX TO 2
           SEARCH ENT VARYING JX
               WHEN ENT-VAL (JX) = "EE" CONTINUE
           END-SEARCH
           SET N TO JX
           DISPLAY "JX " N
           MOVE 40 TO K
           SEARCH ALL ENT
               AT END DISPLAY "ALL END 1"
               WHEN ENT-KEY (IX) = K DISPLAY "ALL " ENT-VAL (IX)
           END-SEARCH
           SEARCH ALL ENT
               AT END DISPLAY "ALL END 2"
               WHEN ENT-KEY (IX) = 45 DISPLAY "NO"
           END-SEARCH
           SEARCH ALL ENT
               WHEN ENT-KEY (IX) = 10
                   DISPLAY "ALL FIRST " ENT-VAL (IX)
           END-SEARCH
           SEARCH ALL ENT
               WHEN ENT-KEY (IX) = 60 DISPLAY "ALL LAST"
           END-SEARCH
           SET IX TO 1
           PERFORM UNTIL IX > 2
               SEARCH ENT
                   AT END DISPLAY "END 4"
                   WHEN ENT-VAL (IX) = "BB" DISPLAY "LOOP BB"
                       SET IX UP BY 1
               END-SEARCH
           END-PERFORM
           STOP RUN.
