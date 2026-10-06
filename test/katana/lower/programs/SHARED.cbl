       IDENTIFICATION DIVISION.
       PROGRAM-ID. SHARED.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 A PIC 99V9.
       01 B PIC 99V9.
       01 C PIC 99.
       01 D PIC 9V9 VALUE 3.9.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE 10 TO A
           DIVIDE D INTO A GIVING B A ROUNDED C
           DISPLAY "1 " A " " B " " C
           MOVE 5 TO A
           COMPUTE A B = A / 3 + 1
           DISPLAY "5 " A " " B
           MOVE 5 TO A MOVE 1 TO B
           SUBTRACT A FROM B GIVING A C
           DISPLAY "6 " A " " C
           STOP RUN.
