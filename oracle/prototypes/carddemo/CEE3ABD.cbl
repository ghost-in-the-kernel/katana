       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEE3ABD.
      * Stub for LE CEE3ABD: abend with the given code => exit status.
       DATA DIVISION.
       LINKAGE SECTION.
       01 ABCODE PIC S9(9) COMP.
       01 TIMING PIC S9(9) COMP.
       PROCEDURE DIVISION USING ABCODE TIMING.
           DISPLAY "CEE3ABD: user abend U" ABCODE.
           MOVE ABCODE TO RETURN-CODE.
           STOP RUN.
