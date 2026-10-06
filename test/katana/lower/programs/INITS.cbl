       IDENTIFICATION DIVISION.
       PROGRAM-ID. INITS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 G.
          05 A PIC X(3) VALUE "AAA".
          05 N PIC 9(3) VALUE 123.
          05 E PIC Z9.9 VALUE "1.5".
          05 AE PIC X/X VALUE "Q/R".
          05 AB PIC A(2) VALUE "AB".
          05 T PIC 99 OCCURS 2 VALUE 7.
          05 FILLER PIC X VALUE "F".
       01 S PIC X(2) VALUE "ss".
       PROCEDURE DIVISION.
       MAIN-PARA.
           INITIALIZE G REPLACING NUMERIC BY 42
           DISPLAY "1 " G
           INITIALIZE G REPLACING ALPHANUMERIC BY "zz"
               NUMERIC-EDITED DATA BY 3.25
           DISPLAY "2 " G
           INITIALIZE G REPLACING ALPHABETIC BY S
               ALPHANUMERIC-EDITED BY "XY"
           DISPLAY "3 " G
           INITIALIZE G
           DISPLAY "4 " G
           STOP RUN.
