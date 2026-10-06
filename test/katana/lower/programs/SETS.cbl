       IDENTIFICATION DIVISION.
       PROGRAM-ID. SETS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 TBL.
          05 ENTRY-X PIC X(3) OCCURS 5 INDEXED BY IX.
       01 N PIC 9(3) VALUE 0.
       01 FLAG PIC X VALUE "N".
          88 FLAG-ON VALUE "Y".
          88 FLAG-OFF VALUE "N".
       01 Q PIC 9(3).
       01 R PIC 9(3).
       01 I PIC 9 VALUE 0.
       01 J PIC 9 VALUE 0.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE "AAABBBCCCDDDEEE" TO TBL
           SET IX TO 2
           DISPLAY ENTRY-X (IX)
           SET IX UP BY 2
           DISPLAY ENTRY-X (IX)
           SET IX DOWN BY 1
           DISPLAY ENTRY-X (IX)
           SET N TO IX
           DISPLAY N
           SET FLAG-ON TO TRUE
           DISPLAY FLAG
           IF FLAG-ON DISPLAY "ON" END-IF
           DIVIDE 7 INTO 45 GIVING Q REMAINDER R
           DISPLAY Q " " R
           DIVIDE 45 BY 6 GIVING Q REMAINDER R
           DISPLAY Q " " R
           PERFORM VARYING I FROM 1 BY 1 UNTIL I > 2
                   AFTER J FROM 1 BY 1 UNTIL J > 3
               DISPLAY I J
           END-PERFORM
           STOP RUN.
