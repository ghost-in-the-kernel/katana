       IDENTIFICATION DIVISION.
       PROGRAM-ID. REDEFS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 REDEF10.
           02  RDFDATA1  PICTURE X(10) VALUE "ABC98765DE".
           02  RDFDATA3.
               08  RDFDATA4  PICTURE X(6)  VALUE "ALLDON".
               08  RDFDATA5  PICTURE XX99  VALUE "XX66".
           02  RDF3 REDEFINES RDFDATA3.
               03  RDF3-4    PICTURE X(8).
               03  RDF3-5    PIC 99.
               03  RDF3-5-1 REDEFINES RDF3-5.
                   04  RDF3-5-14  PIC 9.
                   04  RDF3-5-15  PIC 9.
                       88  HARD  VALUE 0.
           02  RDFDATA6  PICTURE A(4) VALUE "ZYXW".
           66  RDF3-5-16 RENAMES RDF3-5.
       PROCEDURE DIVISION.
       MAIN-PARA.
           DISPLAY "[" REDEF10 "]"
           DISPLAY "15 " RDF3-5-15 " 16 " RDF3-5-16
           IF HARD DISPLAY "HARD" ELSE DISPLAY "NOT HARD" END-IF
           MOVE ALL "AB" TO REDEF10
           DISPLAY "[" REDEF10 "]"
           MOVE ALL "Q" TO RDF3-5-16
           DISPLAY "[" RDF3 "]"
           DISPLAY "[" ALL "ABCD" "]" "[" ALL SPACES "]" "[" QUOTE "]"
           STOP RUN.
