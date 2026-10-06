       IDENTIFICATION DIVISION.
       PROGRAM-ID. POWERIBM.
      * Exponentiation where cobc -std=ibm's value is no reading of
      * IBM's: each result is exact and within its receiver.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 A PIC 9(3) VALUE 12.
       01 N PIC 9 VALUE 3.
       01 R PIC 9(9)V9(4).
       01 S PIC S9(5)V9(6).
       01 E PIC -(5)9.9(6).
       PROCEDURE DIVISION.
       MAIN-PARA.
           COMPUTE R = 10 ** 2 + A * 2
           DISPLAY "SUM " R
           COMPUTE R = (A + 1) ** (N - 1)
           DISPLAY "EXPR " R
           COMPUTE S = 3 ** -1
           MOVE S TO E
           DISPLAY "NEG " E
           STOP RUN.
