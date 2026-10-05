      * Katana's own: copies INFILE to OUTFILE, record for record, and
      * says how many (phase 1.8's trivial fixture, the cobc lane's side).
       IDENTIFICATION DIVISION.
       PROGRAM-ID. COPYREC.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-FILE ASSIGN TO INFILE
               ORGANIZATION IS SEQUENTIAL.
           SELECT OUT-FILE ASSIGN TO OUTFILE
               ORGANIZATION IS SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  IN-FILE.
       COPY COPYREC.
       FD  OUT-FILE.
       01  OUT-REC             PIC X(20).
       WORKING-STORAGE SECTION.
       01  EOF                 PIC X VALUE 'N'.
       01  COPIED              PIC 9(5) VALUE 0.
       PROCEDURE DIVISION.
           OPEN INPUT IN-FILE OUTPUT OUT-FILE
           PERFORM UNTIL EOF = 'Y'
               READ IN-FILE
                   AT END MOVE 'Y' TO EOF
                   NOT AT END
                       WRITE OUT-REC FROM COPY-REC
                       ADD 1 TO COPIED
               END-READ
           END-PERFORM
           CLOSE IN-FILE OUT-FILE
           DISPLAY 'COPIED ' COPIED ' RECORDS'
           STOP RUN.
