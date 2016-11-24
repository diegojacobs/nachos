#include "studio.h"
#include "stdlib.h"
#include "syscall.h"
#define TESTFILE "p3.txt"
#define Bsize 2

int retval;
int fd;
int i;
int ii;
int iii;
int iiii;
int cnt = 2;
char buf[Bsize];
char buf2[Bsize];

int main()
{
	/* code */
	fd = open(TESTFILE);
	buf[0] = 'O';
	buf[1] = 'K';
	buf[Bsize] = '\0';
	ii = write (fd, buf, cnt);
	iii = read(fd, buf2, cnt);

	printf("Lectura: %s", buf2);

	i = close(fd);
	halt();
}