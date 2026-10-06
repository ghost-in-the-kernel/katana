       IDENTIFICATION DIVISION.
       PROGRAM-ID. CORRS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 SRC.
          05 A PIC 9(3) VALUE 123.
          05 B PIC X(4) VALUE "BBBB".
          05 FILLER PIC X(2) VALUE "FF".
          05 C PIC X(3) VALUE "CCC".
          05 D REDEFINES C PIC 9(3).
          05 E OCCURS 2 PIC X VALUE "E".
          05 SUB.
             10 S1 PIC X(2) VALUE "S1".
             10 S2 PIC 9(2) VALUE 42.
          05 GRP.
             10 G1 PIC X(2) VALUE "G1".
          05 ONLY-SRC PIC X VALUE "O".
       01 DST.
          05 ONLY-DST PIC X VALUE "-".
          05 S2 PIC X(2) VALUE "--".
          05 SUB.
             10 S2 PIC 9(4) VALUE 0.
             10 S1 PIC X(3) VALUE "---".
          05 C PIC X(5) VALUE "-----".
          05 A PIC 9(5)V9 VALUE 0.
          05 B PIC X(2) VALUE "--".
          05 FILLER PIC X(2) VALUE "--".
          05 D PIC 9(3) VALUE 0.
          05 E PIC X VALUE "-".
          05 GRP PIC X(4) VALUE "----".
       01 DST2.
          05 A PIC X(3) VALUE "---".
          05 B PIC X(6) VALUE "------".
       01 TBL.
          05 ROW OCCURS 2.
             10 K PIC 9 VALUE 0.
             10 V PIC X(2) VALUE "..".
       01 ONE.
          05 K PIC 9 VALUE 7.
          05 V PIC X(2) VALUE "VV".
       01 NA.
          05 P PIC 9(3) VALUE 5.
          05 Q PIC X(2) VALUE "QQ".
          05 R1 PIC 9V9 VALUE 1.5.
       01 NB.
          05 P PIC 9(3) VALUE 10.
          05 Q PIC 9(2) VALUE 7.
          05 R1 PIC 9 VALUE 2.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE CORRESPONDING SRC TO DST DST2
           DISPLAY "DST " DST
           DISPLAY "DST2 " DST2
           MOVE CORR ONE TO ROW (2)
           DISPLAY "TBL " TBL
           ADD CORR NA TO NB ROUNDED
           DISPLAY "ADD " NB
           SUBTRACT CORRESPONDING NA FROM NB
           DISPLAY "SUB " NB
           STOP RUN.
