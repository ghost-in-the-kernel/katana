# GnuCOBOL's IBM dialect as it stands, with nothing set beyond it: the profile CardDemo's batch
# programs were compiled under in the study (F-02, `cobc -x -std=ibm`). What the dialect changes
# from cobol85 on NIST is measured in oracle/nist/IBM-CHANGES.md.
compiler GnuCOBOL
version 3.1.2
dialect ibm
# ibm.trunc STD below: binary items truncated to their PICTURE, which `-std=ibm` alone does not do
# (oracle/nist/IBM-CHANGES.md, binary-truncation), so the lane sets it (PROFILE-04).
flag binary-truncate=yes
# How the source was compiled on z/OS (R-049): CardDemo states no compile options, so these are
# IBM's documented defaults (docs/PROFILE.md, docs/research/ibm-options.md).
ibm.trunc STD
ibm.numproc NOPFD
ibm.arith COMPAT
ibm.codepage 1140
ibm.invdata NO
ibm.version 6.4
