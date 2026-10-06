       IDENTIFICATION DIVISION.
       PROGRAM-ID. ACCEPTREC.
      * ACCEPT into an item longer than a record: records read "until
      * the receiving area is filled", each concatenated with the one
      * before, the last cut to fit (LR 6.4 pp. 307-308); the next
      * ACCEPT reads the next record.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 LONG-ITEM PIC X(100).
       01 NEXT-ITEM PIC X(11).
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT LONG-ITEM
           DISPLAY "FIRST 10 [" LONG-ITEM (1:10) "]"
           DISPLAY "LAST 20 [" LONG-ITEM (81:20) "]"
           ACCEPT NEXT-ITEM
           DISPLAY "NEXT [" NEXT-ITEM "]"
           STOP RUN.
