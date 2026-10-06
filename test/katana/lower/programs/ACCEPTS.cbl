       IDENTIFICATION DIVISION.
       PROGRAM-ID. ACCEPTS.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           SYSIN IS INPUT-DEVICE.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 D6 PIC 9(6).
       01 D8 PIC 9(8).
       01 Y5 PIC 9(5).
       01 Y7 PIC 9(7).
       01 W PIC 9.
       01 T PIC 9(8).
       01 TX REDEFINES T.
          05 HH PIC 99.
          05 MI PIC 99.
          05 SS PIC 99.
          05 HU PIC 99.
       01 G.
          05 G1 PIC X(4).
          05 G2 PIC X(4).
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT D6 FROM DATE
           IF D6 NUMERIC AND D6 (3:2) >= "01" AND D6 (3:2) <= "12"
               DISPLAY "DATE OK" END-IF
           ACCEPT D8 FROM DATE YYYYMMDD
           IF D8 (3:6) = D6 DISPLAY "YYYYMMDD OK" END-IF
           ACCEPT Y5 FROM DAY
           ACCEPT Y7 FROM DAY YYYYDDD
           IF Y7 (3:5) = Y5 AND Y5 (3:3) >= "001"
               DISPLAY "DAY OK" END-IF
           ACCEPT W FROM DAY-OF-WEEK
           IF W >= 1 AND W <= 7 DISPLAY "DAY-OF-WEEK OK" END-IF
           ACCEPT T FROM TIME
           IF HH < 24 AND MI < 60 AND SS < 61 DISPLAY "TIME OK" END-IF
           ACCEPT G FROM DATE
           IF G (7:2) = SPACES DISPLAY "GROUP PADDED" END-IF
           STOP RUN.
