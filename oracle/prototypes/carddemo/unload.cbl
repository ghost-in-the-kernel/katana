       IDENTIFICATION DIVISION.
       PROGRAM-ID. UNLOAD.
      * Reads an indexed file sequentially (key order) and writes the
      * records as a fixed sequential file: the logical KSDS image.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT INF ASSIGN TO INFILE ORGANIZATION INDEXED
               ACCESS SEQUENTIAL RECORD KEY IS IN-KEY
               FILE STATUS IS IN-ST.
           SELECT OUTF ASSIGN TO OUTFILE ORGANIZATION SEQUENTIAL
               FILE STATUS IS OUT-ST.
       DATA DIVISION.
       FILE SECTION.
       FD INF.
       01 IN-REC.
          05 IN-KEY PIC X(16).
          05 FILLER PIC X(334).
       FD OUTF.
       01 OUT-REC PIC X(350).
       WORKING-STORAGE SECTION.
       01 IN-ST PIC XX.
       01 OUT-ST PIC XX.
       01 N PIC 9(7) VALUE 0.
       PROCEDURE DIVISION.
           OPEN INPUT INF OUTPUT OUTF.
           PERFORM UNTIL IN-ST NOT = "00"
             READ INF NEXT
             IF IN-ST = "00" WRITE OUT-REC FROM IN-REC ADD 1 TO N
             END-IF
           END-PERFORM.
           CLOSE INF OUTF.
           DISPLAY "UNLOAD " N " records, final status " IN-ST.
