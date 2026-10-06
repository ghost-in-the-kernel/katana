       IDENTIFICATION DIVISION.
       PROGRAM-ID. VARYAFTER.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 I PIC S99.
       01 J PIC S99.
       01 K PIC S99.
       01 MINUS1 PIC S9 VALUE -1.
       01 SIX PIC 9 VALUE 6.
       01 EI PIC -9.
       01 EJ PIC -99.
       PROCEDURE DIVISION.
       MAIN-PARA.
           PERFORM BODY WITH TEST AFTER
               VARYING I FROM 2 BY MINUS1 UNTIL I = 1
               AFTER J FROM 2 BY SIX UNTIL J > 19
           MOVE I TO EI MOVE J TO EJ
           DISPLAY "END I " EI " J " EJ
           PERFORM WITH TEST AFTER VARYING K FROM 1 BY 1 UNTIL K > 2
               MOVE K TO EI
               DISPLAY "INLINE K " EI
           END-PERFORM
           PERFORM BODY WITH TEST AFTER
               VARYING I FROM 5 BY 1 UNTIL I > 0
               AFTER J FROM 1 BY 1 UNTIL J > 0
           MOVE I TO EI MOVE J TO EJ
           DISPLAY "ONCE I " EI " J " EJ
           PERFORM BODY
               VARYING I FROM 2 BY MINUS1 UNTIL I = 0
               AFTER J FROM 1 BY 1 UNTIL J > 2
           MOVE I TO EI MOVE J TO EJ
           DISPLAY "BEFORE END I " EI " J " EJ
           MOVE 0 TO K
           PERFORM COUNT-IT
               VARYING I FROM 1 BY 1 UNTIL I > 3
               AFTER J FROM I BY 1 UNTIL J > 3
           MOVE K TO EJ
           DISPLAY "TRIANGLE " EJ
           STOP RUN.
       COUNT-IT.
           ADD 1 TO K.
       BODY.
           MOVE I TO EI MOVE J TO EJ
           DISPLAY "I " EI " J " EJ.
