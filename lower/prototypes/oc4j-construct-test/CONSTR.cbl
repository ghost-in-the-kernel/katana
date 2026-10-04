       IDENTIFICATION DIVISION.
       PROGRAM-ID. CONSTR.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-AMOUNT          PIC S9(7)V99 COMP-3 VALUE 1234.56.
       01  WS-COUNT           PIC 9(4) VALUE 0.
       01  WS-TOTAL           PIC S9(9)V99 VALUE ZERO.
       01  WS-DISP            PIC ZZ,ZZ9.99-.
       01  WS-REC.
           05 WS-KEY          PIC X(8).
           05 WS-DATA         PIC X(12).
       01  WS-REC-NUM REDEFINES WS-REC.
           05 WS-NUM-A        PIC 9(8).
           05 WS-NUM-B        PIC 9(12).
       01  WS-TBL.
           05 WS-ENTRY OCCURS 10 TIMES INDEXED BY WS-IDX.
              10 WS-E-VAL     PIC 9(3).
       01  WS-FLAG            PIC X VALUE 'N'.
           88 DONE            VALUE 'Y'.
       PROCEDURE DIVISION.
       MAIN-PARA.
           PERFORM INIT-PARA THRU INIT-EXIT.
           PERFORM VARYING WS-IDX FROM 1 BY 1 UNTIL WS-IDX > 10
               ADD WS-E-VAL(WS-IDX) TO WS-TOTAL
           END-PERFORM.
           COMPUTE WS-AMOUNT ROUNDED = WS-TOTAL * 1.5 / 3.
           MOVE WS-AMOUNT TO WS-DISP.
           DISPLAY 'TOTAL: ' WS-DISP.
           IF WS-COUNT > 5
               GO TO END-PARA
           END-IF.
           ADD 1 TO WS-COUNT.
           GO TO MAIN-PARA.
       INIT-PARA.
           MOVE 'ABCDEFGH' TO WS-KEY.
           MOVE 7 TO WS-E-VAL(3).
       INIT-EXIT.
           EXIT.
       END-PARA.
           SET DONE TO TRUE.
           STOP RUN.
