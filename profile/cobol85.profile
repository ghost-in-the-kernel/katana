# GnuCOBOL's COBOL 85 dialect, with nothing set beyond it: the profile of the NIST golden
# (oracle/nist/programs.golden, R-047).
compiler GnuCOBOL
version 3.1.2
dialect cobol85
# NIST's programs were never compiled on z/OS; every profile states every field (PROFILE-02), so
# these are IBM's documented defaults (R-049): what a lowering of NIST would assume.
ibm.trunc STD
ibm.numproc NOPFD
ibm.arith COMPAT
ibm.codepage 1140
ibm.invdata NO
ibm.version 6.4
# DISPLAY's sign form and the byte fresh WORKING-STORAGE is set to (R-049, R-057): NIST states neither, so IBM's defaults.
ibm.dispsign COMPAT
ibm.wsinit NONE
