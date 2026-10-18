       IDENTIFICATION DIVISION.
       PROGRAM-ID. STATUS35.
      * T-E3 (research section 33, 34): CBEXPORT's and CBIMPORT's
      * pattern, a file status whose condition-names name only OK and
      * end of file, read after an OPEN of a file that may be absent.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-FILE ASSIGN TO "INFILE"
               ORGANIZATION IS SEQUENTIAL
               FILE STATUS IS WS-IN-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  IN-FILE.
       01  IN-REC PIC X(10).
       WORKING-STORAGE SECTION.
       01  WS-IN-STATUS PIC XX.
           88  WS-IN-OK  VALUE '00'.
           88  WS-IN-EOF VALUE '10'.
       PROCEDURE DIVISION.
       MAIN-PARA.
           OPEN INPUT IN-FILE
           DISPLAY "STATUS " WS-IN-STATUS
           EVALUATE TRUE
               WHEN WS-IN-OK
                   DISPLAY "NAMED"
               WHEN WS-IN-EOF
                   DISPLAY "NAMED"
               WHEN OTHER
                   DISPLAY "UNNAMED"
           END-EVALUATE
           STOP RUN.
