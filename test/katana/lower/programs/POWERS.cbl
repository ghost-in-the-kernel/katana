       IDENTIFICATION DIVISION.
       PROGRAM-ID. POWERS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 A PIC 9(3) VALUE 12.
       01 B PIC 9V9 VALUE 1.5.
       01 N PIC 9 VALUE 3.
       01 M PIC S9 VALUE -2.
       01 Z PIC 9 VALUE 0.
       01 R PIC 9(9)V9(4).
       01 S PIC S9(5)V9(6).
       01 E PIC -(5)9.9(6).
       01 T PIC 9(3).
       PROCEDURE DIVISION.
       MAIN-PARA.
           COMPUTE R = A ** 2
           DISPLAY "A**2 " R
           COMPUTE R = B ** 3
           DISPLAY "B**3 " R
           COMPUTE R = A ** N
           DISPLAY "A**N " R
           COMPUTE S = 2 ** M
           MOVE S TO E
           DISPLAY "2**M " E
           COMPUTE S = B ** -1
           MOVE S TO E
           DISPLAY "B**-1 " E
           COMPUTE R = A ** Z
           DISPLAY "A**Z " R
           COMPUTE T = A ** 3
               ON SIZE ERROR DISPLAY "SIZE ERROR"
           END-COMPUTE
           DISPLAY "T " T
           COMPUTE S = B ** N * 2 - 1
           MOVE S TO E
           DISPLAY "MIX " E
           STOP RUN.
