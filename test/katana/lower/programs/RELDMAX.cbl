       IDENTIFICATION DIVISION.
       PROGRAM-ID. RELDMAX.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       77 B PIC S9(5)V9(5) VALUE -10.
       77 C PIC 9(8) VALUE 10.
       77 D PIC 9(5) VALUE 1.
       77 E PIC 9999 VALUE 0.
       77 F PIC 9(5) VALUE 10.
       77 BU PIC 9(5)V9(5) VALUE 10.5.
       77 BI PIC S9(5) VALUE -10.
       77 G PIC S99 VALUE -10.
           88 GZ VALUE -10.
           88 NZ VALUE +10.
       PROCEDURE DIVISION.
       MAIN-PARA.
           IF B EQUAL TO C
              DISPLAY "N T" ELSE DISPLAY "N F".
           IF (D + 0 EQUAL TO 1)
              DISPLAY "O T" ELSE DISPLAY "O F".
           IF B EQUAL TO 99 OR (D + 0 EQUAL TO 1)
              DISPLAY "P T" ELSE DISPLAY "P F".
           IF C EQUAL TO 99 OR (D + 0 EQUAL TO 1)
              DISPLAY "Q T" ELSE DISPLAY "Q F".
           IF BU EQUAL TO C OR (D + 0 EQUAL TO 1)
              DISPLAY "R T" ELSE DISPLAY "R F".
           IF BI EQUAL TO C OR (D + 0 EQUAL TO 1)
              DISPLAY "S T" ELSE DISPLAY "S F".
           IF B EQUAL TO C OR D + 0 EQUAL TO 1
              DISPLAY "T T" ELSE DISPLAY "T F".
           IF B EQUAL TO C OR D EQUAL TO 1
              DISPLAY "U T" ELSE DISPLAY "U F".
           IF D + 0 EQUAL TO 1 OR B EQUAL TO C
              DISPLAY "V T" ELSE DISPLAY "V F".
           STOP RUN.
