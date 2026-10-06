       IDENTIFICATION DIVISION.
       PROGRAM-ID. ALPHAIBM.
      * The program collating sequence where cobc -std=ibm does not
      * apply it: a VALUE of LOW-VALUE (LR 6.4 p. 16) and a relation of
      * two literals (p. 123).
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       OBJECT-COMPUTER. X
           PROGRAM COLLATING SEQUENCE IS FUNNY.
       SPECIAL-NAMES.
           ALPHABET FUNNY IS "F" "U" "N".
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 A PIC X VALUE LOW-VALUE.
       01 C PIC X VALUE "F".
       PROCEDURE DIVISION.
       MAIN-PARA.
           IF A = C DISPLAY "VALUE LOW IS F"
               ELSE DISPLAY "VALUE LOW NOT F" END-IF
           IF "U" < "N" DISPLAY "U < N" ELSE DISPLAY "U NOT < N" END-IF
           STOP RUN.
