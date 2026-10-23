       IDENTIFICATION DIVISION.
       PROGRAM-ID. ODOSLIDE.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 N PIC 9 VALUE 2.
       01 M PIC 9 VALUE 3.
       01 L PIC 99.
       01 REC.
          05 HD PIC X(2) VALUE "HD".
          05 A.
             10 AX PIC X OCCURS 1 TO 5 TIMES DEPENDING ON N.
          05 MID PIC X(2) VALUE "MM".
          05 B.
             10 BT PIC XX OCCURS 1 TO 4 TIMES DEPENDING ON M.
          05 TAIL PIC X(3) VALUE "TTT".
       01 W PIC X(30).
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE 2 TO N
           MOVE 3 TO M
           MOVE "ab" TO A
           MOVE "XY" TO MID
           MOVE "112233" TO B
           MOVE "end" TO TAIL
           MOVE LENGTH OF REC TO L
           DISPLAY "LEN " L
           MOVE FUNCTION LENGTH (REC) TO L
           DISPLAY "FLEN " L
           DISPLAY "REC [" REC "]"
           MOVE 4 TO N
           MOVE 1 TO M
           MOVE "abcd" TO A
           MOVE "ZZ" TO MID
           MOVE "99" TO BT (1)
           MOVE "tl" TO TAIL
           DISPLAY "MID [" MID "] BT1 [" BT (1) "] TAIL [" TAIL "]"
           MOVE LENGTH OF REC TO L
           DISPLAY "LEN " L
           MOVE SPACES TO W
           MOVE REC TO W
           DISPLAY "W [" W "]"
           MOVE 1 TO N
           MOVE 2 TO M
           MOVE "q" TO AX (1)
           MOVE "rs" TO BT (2)
           INITIALIZE TAIL
           DISPLAY "A [" A "] B [" B "] TAIL [" TAIL "]"
           DISPLAY "REC [" REC "]"
           STOP RUN.
