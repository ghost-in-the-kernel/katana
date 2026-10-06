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
           MOVE 2 TO A MOVE 3 TO B
           ADD A TO A B
           DISPLAY "2 " A " " B
           MOVE 2 TO A MOVE 3 TO B
           MULTIPLY A BY A B
           DISPLAY "3 " A " " B
           MOVE 8 TO A MOVE 16 TO B
           DIVIDE A INTO A B
           DISPLAY "4 " A " " B
           MOVE 5 TO A
           COMPUTE A B = A / 3 + 1
           DISPLAY "5 " A " " B
           MOVE 5 TO A MOVE 1 TO B
           SUBTRACT A FROM B GIVING A C
           DISPLAY "6 " A " " C
           STOP RUN.
