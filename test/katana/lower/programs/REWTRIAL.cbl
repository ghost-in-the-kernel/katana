       IDENTIFICATION DIVISION.
       PROGRAM-ID. REWTRIAL.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT RWF ASSIGN TO "RFILE" FILE STATUS IS S.
       DATA DIVISION.
       FILE SECTION.
       FD  RWF.
       01  REC PIC XX.
       WORKING-STORAGE SECTION.
       01  S PIC XX.
       PROCEDURE DIVISION.
       MAIN-PARA.
           OPEN OUTPUT RWF
           MOVE "R1" TO REC WRITE REC
           MOVE "R2" TO REC WRITE REC
           MOVE "R3" TO REC WRITE REC
           CLOSE RWF
           OPEN I-O RWF
           MOVE "X0" TO REC REWRITE REC
           DISPLAY " 1 " S
           READ RWF AT END DISPLAY "   AE" END-READ
           DISPLAY " 2 " S " " REC
           MOVE "N1" TO REC REWRITE REC
           DISPLAY " 3 " S
           MOVE "M1" TO REC REWRITE REC
           DISPLAY " 4 " S
           READ RWF AT END DISPLAY "   AE" END-READ
           DISPLAY " 5 " S " " REC
           READ RWF AT END DISPLAY "   AE" END-READ
           DISPLAY " 6 " S " " REC
           READ RWF AT END DISPLAY "   AE" END-READ
           DISPLAY " 7 " S
           MOVE "N3" TO REC REWRITE REC
           DISPLAY " 8 " S
           CLOSE RWF
           DISPLAY " 9 " S
           OPEN INPUT RWF
           READ RWF DISPLAY "   " REC
           READ RWF DISPLAY "   " REC
           READ RWF DISPLAY "   " REC
           CLOSE RWF
           OPEN INPUT RWF
           READ RWF
           MOVE "Z1" TO REC REWRITE REC
           DISPLAY "10 " S
           CLOSE RWF
           STOP RUN.
