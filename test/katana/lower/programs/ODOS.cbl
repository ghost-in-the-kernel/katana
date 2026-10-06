       IDENTIFICATION DIVISION.
       PROGRAM-ID. ODOS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 N PIC 9 VALUE 3.
       01 G.
          05 HD PIC X(3) VALUE "HD:".
          05 T PIC X OCCURS 0 TO 9 TIMES DEPENDING ON N.
       01 H.
          05 M PIC 9.
          05 U PIC X OCCURS 0 TO 9 TIMES DEPENDING ON M.
       01 W PIC X(20).
       01 C PIC 99.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE 9 TO N
           MOVE "HD:123456789" TO G
           MOVE 3 TO N
           IF G = "HD:123" DISPLAY "EQ" ELSE DISPLAY "NE" END-IF
           MOVE SPACES TO W
           MOVE G TO W
           DISPLAY "W [" W "]"
           MOVE SPACES TO W
           STRING G DELIMITED BY SIZE "-END" DELIMITED BY SIZE
               INTO W
           DISPLAY "S [" W "]"
           MOVE 0 TO C
           INSPECT G TALLYING C FOR ALL "7"
           DISPLAY "C " C
           MOVE "XYZ" TO G
           MOVE 9 TO N
           DISPLAY "G [" G "]"
           MOVE 2 TO M
           MOVE "5ABCDEFGHI" TO H
           DISPLAY "H [" H "] " M
           MOVE 2 TO M
           MOVE H TO W
           DISPLAY "HW [" W "]"
           STOP RUN.
