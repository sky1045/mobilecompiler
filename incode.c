#include <stdio.h>
#include <string.h>
void DieWithError(char *error);
char * incode(char* filename, char* clntIP){
	
	FILE *read, *wri;
	char arr[100], arr1[100];	//stored array
	char *num;			//string address
	char *print;
	char file[30];
	int i = 0;

	//printf를 재정의하는 코드
	char *inser = "int printf(const char *format, ...){\n	va_list arg;\nint done;\nchar str[1024];\n\nva_start(arg, format);\nmemset(str, \'\\0\', 1024);\ndone = vsprintf(str, format, arg);\nwrite(1, str, strlen(str));\nva_end(arg);\nreturn done;\n}\n";
	//scanf를 재정의하는 코드
	char *scan = "int scanf(const char *format, ...){\nva_list arg;\nint done;\nchar str[1024];\n\nva_start(arg, format);\nread(0, str, 1024);\nstr[strlen(str)]=\'\\0\';\ndone = vsscanf(str, format, arg);\nva_end(arg);\nmemset(str, \'\\0\', 100);\nreturn done;\n}\n";

	strncpy(file, filename, strlen(filename));

	char newfileName[30] = "new";	//기존 소스를 수정하여 새로 생성할 파일의 이름을 저장하는 변수
	char *newfile = newfileName;
	char *uni = "#include <unistd.h>\n#include <stdarg.h>\n#include <stdio_ext.h>\n#include <string.h>\n";	//재정의에 필요한 헤더파일들
	char *spr = "sprintf";
//	char path[50];
//	strncpy(path, filename, strlen(filename));	
	strncat(newfileName, filename+strlen(clntIP)+1, strlen(filename)-strlen(clntIP));
	newfileName[strlen(newfileName)] = '\0';	
	printf("new: %s\n", newfileName);
//	printf("path: %s\n", path);
	file[strlen(file)] = '\0';
	read = fopen(filename, "r");	//인자로 받은 기존 소스코드파일을 읽는다
	wri = fopen(newfileName, "w+");	//기존의 코드를 수정하여 새로 생성할 파일
if(read == NULL) DieWithError(file);
if(wri == NULL) printf("wri failed");
	printf("incode\n");
	//기존 소스파일을 읽는다.
	while(fgets(arr, 100, read))
	{
		if((strstr(arr, "scanf")) != NULL){	//scanf가 있으면 입력을 요청하는 시그널을 같이 출력하도록 수정하여 새로운 파일에 입력
			fputc('\n', wri);
			fputs("sleep(0.3);\nwrite(1,\"getIt!!\\n\",8);\n__fpurge(stdin);\n", wri);
			fputc('\n', wri);
		}
		if((strstr(arr, "unistd") == NULL) && (ftell(wri) == 0))
			fputs(uni, wri);		//재정의에 필요한 헤더파일 삽입
		
		if(strstr(arr, "main") != NULL){
		if(strstr(arr, "{") != NULL)		//main함수의 첫 { 다음
		{
			num = strstr(arr, "{");		//main함수가 시작되는 { 의 포인터를 저장

			while(&arr[i] != num)		//num의 포인터와 같은 위치가 될때까지 새로운 파일에 기존 소스를 복사한다.
			{
				fputc(arr[i], wri);
				i++;
			}
			fputc(arr[i], wri);
			i++;
			
			//num 다음 포인터부터 printf와 scanf를 재정의하는 코드를 삽입한다.
			fputc('\n', wri);
			fputs(inser, wri);
			fputs(scan, wri);
			fputc('\n', wri);
			
			//그 외 나머지는 기존의 코드를 그대로 복사한다.
			while(i != strlen(arr))
			{
				fputc(arr[i], wri);
				i++;
			}
		}
		else
			fputs(arr, wri);
		}else
			fputs(arr, wri);
	}
	
	fclose(read);
	fclose(wri);
	//새로 생성된 코드의 이름을 반환
	return newfile;
}
