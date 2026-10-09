       IDENTIFICATION DIVISION.
       PROGRAM-ID. VARTRIAL.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT VF ASSIGN TO "VFILE" FILE STATUS IS S.
           SELECT F2 ASSIGN TO "F2FILE" FILE STATUS IS S.
           SELECT F3 ASSIGN TO "F3FILE" FILE STATUS IS S.
       DATA DIVISION.
       FILE SECTION.
       FD  VF RECORD IS VARYING IN SIZE FROM 2 TO 10 CHARACTERS
              DEPENDING ON L.
       01  R5 PIC X(5).
       01  R10 PIC X(10).
       FD  F2 RECORD CONTAINS 4 TO 8 CHARACTERS.
       01  S4 PIC X(4).
       01  S8 PIC X(8).
       FD  F3.
       01  OR3.
           05 N PIC 9.
           05 T PIC X OCCURS 1 TO 5 DEPENDING ON N.
       WORKING-STORAGE SECTION.
       01  S PIC XX.
       01  L PIC 99.
       01  W PIC X(12).
       PROCEDURE DIVISION.
       MAIN-PARA.
           OPEN OUTPUT VF
           MOVE "AAAAAAAAAA" TO R10 MOVE 10 TO L WRITE R10
           DISPLAY " 1 " S " " L
           MOVE "BBBBBBBBBB" TO R10 MOVE 5 TO L WRITE R10
           DISPLAY " 2 " S " " L
           MOVE "CCCCCCCCCC" TO R10 MOVE 3 TO L WRITE R10
           DISPLAY " 3 " S " " L
           MOVE "DDDDDDDDDD" TO R10 MOVE 11 TO L WRITE R10
           DISPLAY " 4 " S " " L
           MOVE 1 TO L WRITE R10
           DISPLAY " 5 " S " " L
           CLOSE VF
           OPEN INPUT VF
           MOVE ALL "Z" TO R10 MOVE 0 TO L
           READ VF AT END DISPLAY "   AE" END-READ
           DISPLAY " 6 " S " " L " [" R10 "]"
           READ VF AT END DISPLAY "   AE" END-READ
           DISPLAY " 7 " S " " L " [" R10 "]"
           READ VF AT END DISPLAY "   AE" END-READ
           DISPLAY " 8 " S " " L " [" R10 "]"
           READ VF AT END DISPLAY "   AE" END-READ
           DISPLAY " 9 " S " " L
           CLOSE VF
           OPEN INPUT VF
           MOVE ALL "#" TO W
           READ VF INTO W
           DISPLAY "10 " S " [" W "]"
           MOVE ALL "#" TO W
           READ VF INTO W
           DISPLAY "11 " S " [" W "]"
           CLOSE VF
           OPEN OUTPUT F2
           MOVE "1111" TO S4 WRITE S4
           MOVE "22222222" TO S8 WRITE S8
           MOVE "3333" TO S4 WRITE S4
           CLOSE F2
           OPEN INPUT F2
           MOVE ALL "." TO S8 READ F2
           DISPLAY "12 " S " [" S8 "]"
           MOVE ALL "." TO S8 READ F2
           DISPLAY "13 " S " [" S8 "]"
           MOVE ALL "." TO S8 READ F2
           DISPLAY "14 " S " [" S8 "]"
           READ F2 AT END DISPLAY "   AE" END-READ
           DISPLAY "15 " S
           CLOSE F2
           OPEN OUTPUT F3
           MOVE 5 TO N MOVE "#####" TO OR3 (2:5)
           MOVE 2 TO N MOVE "a" TO T (1) MOVE "b" TO T (2) WRITE OR3
           MOVE 5 TO N MOVE "vwxyz" TO OR3 (2:5) WRITE OR3
           CLOSE F3
           OPEN INPUT F3
           MOVE ALL "*" TO W
           READ F3 INTO W
           DISPLAY "16 " S " " N " [" W "]"
           MOVE ALL "*" TO W
           READ F3 INTO W
           DISPLAY "17 " S " " N " [" W "]"
           READ F3 AT END DISPLAY "   AE" END-READ
           DISPLAY "18 " S
           CLOSE F3
           OPEN I-O VF
           READ VF
           MOVE 5 TO L REWRITE R10
           DISPLAY "19 " S
           MOVE 10 TO L REWRITE R10
           DISPLAY "20 " S
           CLOSE VF
           STOP RUN.
