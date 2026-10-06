       IDENTIFICATION DIVISION.
       PROGRAM-ID. SUBSCRIPTS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 TBL.
          05 ROW OCCURS 3 INDEXED BY RX.
             10 CELL PIC X(2) OCCURS 4 INDEXED BY CX.
       01 W-1 PIC 9 VALUE 2.
       01 K PIC 9 VALUE 3.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE "a1a2a3a4b1b2b3b4c1c2c3c4" TO TBL
           SET RX TO 3
           SET CX TO 2
           DISPLAY "1 " CELL (RX - 2, CX + 1)
           DISPLAY "2 " CELL (RX - 1 CX + 2)
           DISPLAY "3 " CELL (W-1  4)
           DISPLAY "4 " CELL (W-1 1)
           DISPLAY "5 " CELL (K; 2)
           DISPLAY "6 " CELL (K - 1, W-1 + 1)
           DISPLAY "7 " CELL (1, K)
           STOP RUN.
