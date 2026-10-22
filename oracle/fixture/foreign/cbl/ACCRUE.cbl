      * Katana's own, for 8A.1's trial (research §48): accrues 3.75%
      * interest on each balance, the interest truncated to cents (no
      * ROUNDED) and the sum truncated at 99999.99 (no SIZE ERROR).
       IDENTIFICATION DIVISION.
       PROGRAM-ID. ACCRUE.
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
       COPY ACCTREC.
       FD  OUT-FILE.
       01  OUT-REC             PIC X(20).
       WORKING-STORAGE SECTION.
       01  EOF                 PIC X VALUE 'N'.
       01  ACCRUED             PIC 9(5) VALUE 0.
       PROCEDURE DIVISION.
           OPEN INPUT IN-FILE OUTPUT OUT-FILE
           PERFORM UNTIL EOF = 'Y'
               READ IN-FILE
                   AT END MOVE 'Y' TO EOF
                   NOT AT END
                       COMPUTE ACCT-INT = ACCT-BAL * 0.0375
                       ADD ACCT-INT TO ACCT-BAL
                       WRITE OUT-REC FROM ACCT-REC
                       ADD 1 TO ACCRUED
               END-READ
           END-PERFORM
           CLOSE IN-FILE OUT-FILE
           DISPLAY 'ACCRUED ' ACCRUED ' RECORDS'
           STOP RUN.
