       IDENTIFICATION DIVISION.
       PROGRAM-ID. OVP.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 N1 PIC S9(5) VALUE -12345.
       01 N2 PIC S9(5) VALUE 12345.
       01 P1 PIC S9(5) COMP-3 VALUE -12345.
       01 B1 PIC S9(5) COMP VALUE -12345.
       01 L1 PIC X VALUE "a".
       01 U1 PIC X VALUE "A".
       01 D1 PIC X VALUE "1".
       PROCEDURE DIVISION.
           DISPLAY "neg display raw bytes: " WITH NO ADVANCING.
           CALL "hexdump" USING N1 BY VALUE LENGTH OF N1.
           DISPLAY "pos display raw bytes: " WITH NO ADVANCING.
           CALL "hexdump" USING N2 BY VALUE LENGTH OF N2.
           DISPLAY "comp-3 raw bytes     : " WITH NO ADVANCING.
           CALL "hexdump" USING P1 BY VALUE LENGTH OF P1.
           DISPLAY "comp raw bytes       : " WITH NO ADVANCING.
           CALL "hexdump" USING B1 BY VALUE LENGTH OF B1.
           IF L1 < U1 AND U1 < D1
              DISPLAY "collating: a < A < 1 (EBCDIC)"
           ELSE
              DISPLAY "collating: 1 < A < a (ASCII)".
           STOP RUN.
