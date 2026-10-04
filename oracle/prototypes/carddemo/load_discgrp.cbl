       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOADdiscgrp.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT INF ASSIGN TO INFILE ORGANIZATION SEQUENTIAL
               FILE STATUS IS IN-ST.
           SELECT OUTF ASSIGN TO OUTFILE ORGANIZATION INDEXED
               ACCESS SEQUENTIAL RECORD KEY IS OUT-KEY
               FILE STATUS IS OUT-ST.
       DATA DIVISION.
       FILE SECTION.
       FD INF.
       01 IN-REC PIC X(50).
       FD OUTF.
       01 OUT-REC.
          05 OUT-KEY PIC X(10).
          05 FILLER PIC X(40).
       WORKING-STORAGE SECTION.
       01 IN-ST PIC XX.
       01 OUT-ST PIC XX.
       01 N PIC 9(7) VALUE 0.
       PROCEDURE DIVISION.
           OPEN INPUT INF OUTPUT OUTF.
           PERFORM UNTIL IN-ST NOT = "00"
             READ INF
             IF IN-ST = "00"
               MOVE IN-REC TO OUT-REC
               WRITE OUT-REC
               IF OUT-ST NOT = "00" DISPLAY "WRITE " OUT-ST " AT " N
               END-IF
               ADD 1 TO N
             END-IF
           END-PERFORM.
           CLOSE INF OUTF.
           DISPLAY "LOADdiscgrp " N " records, final status " IN-ST.
           STOP RUN.
