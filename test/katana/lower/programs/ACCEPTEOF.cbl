       IDENTIFICATION DIVISION.
       PROGRAM-ID. ACCEPTEOF.
      * ACCEPT with nothing on the input device: the item is unchanged
      * (LR 6.4 p. 308).
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           SYSIN IS INPUT-DEVICE.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 X PIC X(5) VALUE "KEPT".
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT X
           DISPLAY "X " X
           ACCEPT X FROM INPUT-DEVICE
           DISPLAY "X " X
           STOP RUN.
