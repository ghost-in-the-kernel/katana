       IDENTIFICATION DIVISION.
       PROGRAM-ID. SEARCHVARY.
      * SEARCH VARYING an item that is not the table's own index: it
      * steps with the search index from its own value (LR 6.4 p. 437).
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 TBL.
          05 ENT OCCURS 6 INDEXED BY IX.
             10 ENT-KEY PIC 9(3).
             10 ENT-VAL PIC X(2).
       01 OTHER-TBL.
          05 OTH PIC X OCCURS 6 INDEXED BY OX.
       01 N PIC 9(3) VALUE 0.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE "010AA020BB030CC040DD050EE060FF" TO TBL
           MOVE "UVWXYZ" TO OTHER-TBL
           SET IX TO 2
           SET OX TO 1
           SEARCH ENT VARYING OX
               WHEN ENT-VAL (IX) = "DD" DISPLAY "DD " OTH (OX)
           END-SEARCH
           SET IX TO 1
           MOVE 0 TO N
           SEARCH ENT VARYING N
               WHEN ENT-KEY (IX) = 30 DISPLAY "N " N
           END-SEARCH
           STOP RUN.
