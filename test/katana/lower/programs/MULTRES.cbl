       IDENTIFICATION DIVISION.
       PROGRAM-ID. MULTRES.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 A PIC 99V9.
       01 B PIC 99V9.
       01 C PIC 9.
       01 T.
          05 D PIC 9 OCCURS 3.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE 2 TO A MOVE 3 TO B
           ADD A TO A B
           DISPLAY "1 " A " " B
           MOVE 2 TO A MOVE 3 TO B
           MULTIPLY A BY A B
           DISPLAY "2 " A " " B
           MOVE 8 TO A MOVE 16 TO B
           DIVIDE A INTO A B
           DISPLAY "3 " A " " B
           MOVE 1 TO C MOVE ZEROS TO T
           ADD C TO C D(C)
           DISPLAY "4 " C " " T
           STOP RUN.
