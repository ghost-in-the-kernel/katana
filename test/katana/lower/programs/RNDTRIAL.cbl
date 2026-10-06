       IDENTIFICATION DIVISION.
       PROGRAM-ID. RNDTRIAL.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 XI PIC 99.
       01 XD PIC 99V9.
       01 Y PIC 99.
       01 Z PIC 99.
       01 R1 PIC 9V9.
       01 R2 PIC 99V9.
       01 E PIC Z9.9.
       01 A PIC 99V9 VALUE 5.
       01 T PIC 9 VALUE 3.
       01 ONE PIC 9 VALUE 1.
       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE 1 TO XI MOVE 4 TO Y MOVE 1 TO Z
           COMPUTE R1 ROUNDED = XI / Y + Z
           MOVE R1 TO E DISPLAY "01 " E
           MOVE 1.1 TO XD MOVE 2 TO Y MOVE 1 TO Z
           COMPUTE R1 ROUNDED = XD / Y + Z
           MOVE R1 TO E DISPLAY "02 " E
           MOVE 1 TO XI MOVE 4 TO Y MOVE 10 TO Z
           COMPUTE R2 ROUNDED = XI / Y + Z
           MOVE R2 TO E DISPLAY "03 " E
           MOVE 1.1 TO XD MOVE 2 TO Y MOVE 10 TO Z
           COMPUTE R2 ROUNDED = XD / Y + Z
           MOVE R2 TO E DISPLAY "04 " E
           MOVE 1 TO XI MOVE 3 TO Y MOVE 2 TO Z
           COMPUTE R1 ROUNDED = XI / Y * Z
           MOVE R1 TO E DISPLAY "05 " E
           MOVE 1.1 TO XD MOVE 2 TO Y MOVE 1 TO Z
           COMPUTE R1 ROUNDED = XD / Y * Z
           MOVE R1 TO E DISPLAY "06 " E
           MOVE 2 TO XI MOVE 3 TO Y MOVE 17 TO Z
           COMPUTE R2 ROUNDED = XI / Y * Z
           MOVE R2 TO E DISPLAY "07 " E
           MOVE 1.1 TO XD MOVE 2 TO Y MOVE 20 TO Z
           COMPUTE R2 ROUNDED = XD / Y * Z
           MOVE R2 TO E DISPLAY "08 " E
           MOVE 1 TO XI MOVE 4 TO Y MOVE 1 TO Z
           COMPUTE R1 ROUNDED = XI / Y / Z
           MOVE R1 TO E DISPLAY "09 " E
           MOVE 1.1 TO XD MOVE 2 TO Y MOVE 1 TO Z
           COMPUTE R1 ROUNDED = XD / Y / Z
           MOVE R1 TO E DISPLAY "10 " E
           MOVE 32 TO XI MOVE 3 TO Y MOVE 1 TO Z
           COMPUTE R2 ROUNDED = XI / Y / Z
           MOVE R2 TO E DISPLAY "11 " E
           MOVE 20.1 TO XD MOVE 2 TO Y MOVE 1 TO Z
           COMPUTE R2 ROUNDED = XD / Y / Z
           MOVE R2 TO E DISPLAY "12 " E
           COMPUTE R2 ROUNDED = A / T + ONE
           MOVE R2 TO E DISPLAY "13 " E
           COMPUTE R2 ROUNDED = A / T + 1
           MOVE R2 TO E DISPLAY "14 " E
           MOVE 1.1 TO XD MOVE 2 TO Y
           COMPUTE R2 ROUNDED = XD / Y + 10
           MOVE R2 TO E DISPLAY "15 " E
           STOP RUN.
