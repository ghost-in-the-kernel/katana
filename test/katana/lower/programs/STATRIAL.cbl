       IDENTIFICATION DIVISION.
       PROGRAM-ID. STATRIAL.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT OPTIONAL OPTF ASSIGN TO "ABSENT1"
               FILE STATUS IS S1.
           SELECT NOPT ASSIGN TO "ABSENT2" FILE STATUS IS S2.
           SELECT WF ASSIGN TO "WFILE" FILE STATUS IS S3.
       DATA DIVISION.
       FILE SECTION.
       FD  OPTF.
       01  OREC PIC X(4).
       FD  NOPT.
       01  NREC PIC X(4).
       FD  WF.
       01  WREC PIC X(4).
       WORKING-STORAGE SECTION.
       01  S1 PIC XX.
       01  S2 PIC XX.
       01  S3 PIC XX.
       PROCEDURE DIVISION.
       DECLARATIVES.
       NOPT-ERRORS SECTION.
           USE AFTER STANDARD ERROR PROCEDURE ON NOPT.
       NOPT-SAID.
           DISPLAY "   D2 " S2.
       INPUT-ERRORS SECTION.
           USE AFTER STANDARD ERROR PROCEDURE ON INPUT.
       INPUT-SAID.
           DISPLAY "   DI " S1 " " S2 " " S3.
       END DECLARATIVES.
       MAIN-SECTION SECTION.
       MAIN-PARA.
           OPEN INPUT OPTF
           DISPLAY " 1 " S1
           READ OPTF AT END DISPLAY "   AE" END-READ
           DISPLAY " 2 " S1
           READ OPTF AT END DISPLAY "   AE" END-READ
           DISPLAY " 3 " S1
           CLOSE OPTF
           DISPLAY " 4 " S1
           OPEN INPUT NOPT
           DISPLAY " 5 " S2
           READ NOPT AT END DISPLAY "   AE" END-READ
           DISPLAY " 6 " S2
           CLOSE NOPT
           DISPLAY " 7 " S2
           OPEN OUTPUT WF
           DISPLAY " 8 " S3
           OPEN OUTPUT WF
           DISPLAY " 9 " S3
           MOVE "W1" TO WREC
           WRITE WREC
           DISPLAY "10 " S3
           READ WF AT END DISPLAY "   AE" END-READ
           DISPLAY "11 " S3
           CLOSE WF WITH LOCK
           DISPLAY "12 " S3
           OPEN INPUT WF
           DISPLAY "13 " S3
           CLOSE WF
           DISPLAY "14 " S3
           WRITE WREC
           DISPLAY "15 " S3
           STOP RUN.
