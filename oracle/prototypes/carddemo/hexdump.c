#include <stdio.h>
int hexdump(unsigned char *p, int n){for(int i=0;i<n;i++)printf("%02X",p[i]);printf("\n");return 0;}
