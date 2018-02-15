#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <arpa/inet.h>
#include <stdio_ext.h>

#define MAXPENDING 5        
                            //Maximum outstanding connection requests
int AcceptTCPConnection(int servSock, struct sockaddr_in echoClntAddr, char *clntIP);
void DieWithError(char *errorMessage);                  //Error handling function
int ls(int clntSock, char *IP);
char *incode(char *filename, char *clntIP);
int main(void){

	char WR[1024];
	char buff[1024];
	char search[15];
	char mn[15];
	char sch;
	int pid, in[2], out[2];
	int wad= 0;
	char mo[55];
	char *filename;
	char cmd[50] = "gcc ";
	char clntIP[15];
	char request[7];	
	
	int servSock;                                   //Socket descriptor for server
        int clntSock;                                   //Socket descriptor for client
        struct sockaddr_in echoServAddr;                //Local address
        struct sockaddr_in echoClntAddr;                //Client address
        unsigned short echoServPort;                    //Server port
        unsigned int clntLen;                           //Length of client address data structure
        unsigned int childProcCount = 0;

        int echoPort;

        printf("port : ");
        scanf("%d", &echoPort);					//포트번호를 입력받는다.
        echoServPort = (unsigned short)echoPort;                //First arg: local port

        //Create socket for incoming connections
        if((servSock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0)//소켓연결 실패시 에러 처리
                DieWithError("socket() failed");

        //Construct local address structure
        memset(&echoServAddr, 0, sizeof(echoServAddr)); //Zero out structure
        echoServAddr.sin_family = AF_INET;              //Internet address family
        echoServAddr.sin_addr.s_addr = htonl(INADDR_ANY);//Any incoming interface
        echoServAddr.sin_port = htons(echoServPort);    //Local port

        //Bind to the local address
        if(bind(servSock, (struct sockaddr*)&echoServAddr, sizeof(echoServAddr))< 0)
                DieWithError("bind() failed");
        //Mark the socket so it will listen for incoming connections
        if(listen(servSock, MAXPENDING) < 0)
                DieWithError("listen() failed");
        for(;;) //Run forever
        {
                //Set the size of the in_out parameter
                clntLen = sizeof(echoClntAddr);

                clntSock = AcceptTCPConnection(servSock, echoClntAddr, clntIP);
		
		//자식 프로세스에서 클라이언트와 연결
                pid_t processID = fork();
                if(processID < 0)
                        DieWithError("fork() failed");
                else if(processID == 0){
                        close(servSock);
	memset(buff, '\0', 1024);

	while(1){
	recv(clntSock, buff, 8, 0);		//클라이언트로부터 시그널 수신
	if(!strncmp(buff, "RESTART_", 8)){	//클라이언트가 메인화면으로 돌아갔을 경우
		memset(buff, '\0', 1024);
		ls(clntSock, clntIP);		//파일 목록 전송
		memset(buff, '\0', 1024);
		continue;
	}
	//메인 화면으로 돌아가는 경우들에 대한 처리
	if(!strncmp(buff, "Destroyd", 8)) break;
	if(strlen(buff)==0) break;			
	if(!strncmp(buff, "FILEOVER", 8)) break;
	//클라이언트가 뒤로 가기 버튼을 눌렀을 경우
	if(!strncmp(buff, "BACK", 4)){

		memset(buff, '\0', 1024);
		continue;
	}
        printf("buff: %s\n", buff);		//서버가 수신한 값을 출력
	if(!strncmp(buff, "create1!", 8)){	//클라이언트가 생성 버튼을 입력한 경우
		char name1[32];
		char newFile[80];
		int fileSize = 0;
	memset(name1, '\0', 32);
	memset(newFile, '\0', 80);
		memset(buff, '\0', 1024);
		recv(clntSock, buff, 1024, 0);	//파일의 이름을 받는다.

		strncpy(name1, buff, 32);
		name1[strlen(name1)] = '\0';
		memset(buff, '\0', 1024);
		        char rm[55];
                        sprintf(rm, "rm %s/%s", clntIP, name1);	//파일 이름을 받는다.
                        system(rm);				//같은 이름의 파일이 있으면 삭제한다.(수정요청에서 필요한 처리)
		sprintf(newFile, "%s/%s", clntIP, name1);
		printf("%s is path", newFile);
	send(clntSock, "SOURCE", 6, 0);	
			FILE *CFP = fopen(newFile, "w");	//생성할 파일
			if(CFP == NULL) DieWithError("CFP Error");
		while(1){
			recv(clntSock, buff, 1024, 0);		//소스코드 수신
			printf("creat: %s\n", buff);		//서버에서 확인
			if(!strncmp(buff, "FILEOVER", 8)) break;//소스코드를 다 받았을 경우 반복문 탈출
			else{
				buff[strlen(buff)] = '\0';
				fputs(buff, CFP);
			}
			memset(buff, '\0', 1024);
		}
			fclose(CFP);
			memset(name1, '\0', 32);
			memset(newFile, '\0', 80);
			memset(buff, '\0', 1024);

	}else if(!strncmp(buff, "delete1!", 8)){		//삭제요청
		char name[30];
		char rmFile[80];
		memset(name, '\0', 30);
		memset(buff, '\0', 1024);
		while(1){					//클라이언트로부터 삭제할 파일의 이름을 수신한다.
			char rm[55];
			memset(rm, '\0', 55);
			recv(clntSock, buff, 1024, 0);
			if(!strncmp(buff, "RM_OVER", 7)) break;	//이름 전송이 완료되었다는 시그널을 수신하면 반복문 탈출
			else{					//삭제
				strncpy(name, buff, strlen(buff));
				sprintf(rm, "rm %s/%s", clntIP, name);

				printf("rm: %s", rm);
				system(rm);
			}
			memset(name, '\0', 30);
			memset(buff, '\0', 1024);		
		}

	}else if(!strncmp(buff, "modify1!", 8)){		//수정 요청
		char name[30];
		char path[80];
		memset(buff, '\0', 1024);
		recv(clntSock, buff, 1024, 0);			//파일의 이름 수신
		printf("buffName : %s\n", buff);		//서버에서 이름 확인
		if(strstr(buff, "modify2!") == NULL)		//클라이언트가 보낸 string에 다음 시그널이 없으면
		strncpy(name, buff, 30);			
		else{
			strncpy(name, strtok(buff, "modify2!"), strlen(buff)-8); //시그널이 포함되어 있으면 시그널만 떼고 복사
			send(clntSock, "modify2!", 8, 0);	
		}
		name[strlen(name)] = '\0';
		buff[strlen(buff)] = '\0';
		printf("name : %s\n", name);


		sprintf(path, "%s/%s", clntIP, buff);

		path[strlen(path)] = '\0';
		printf("path : %s\n", path);
		FILE *MFP = fopen(path, "r");			//수정할 파일 열기
		if(MFP == NULL) DieWithError("open() failed");

		memset(buff, '\0', 1024);
		recv(clntSock, buff, 8, 0);			//다음 시그널 수신
		printf("buff : %s ", buff);
		if(!strncmp(buff, "modify2!", 8)){		//클라이언트가 수신 준비가 되었다는 시그널을 보내면
			memset(buff, '\0', 1024);
			send(clntSock, name, strlen(name), 0);	//클라이언트로 파일의 이름을 보낸다(클라이언트는 화면이 바뀌었으므로 이름을 다시 보내야 한다.
			recv(clntSock, buff, 1024, 0);		//소스코드 전송요청 시그널 수신
			if(!strncmp(buff, "SOURCE", 6))
			{
				memset(buff, '\0', 1024);
 				while(!feof(MFP)){
					fgets(buff, 32, MFP);
				
					printf("%s_OVER\n", buff);
					send(clntSock, buff, strlen(buff), 0);	//소스코드 전송
				
					memset(buff, '\0', 1024);
				}
			
			send(clntSock, "ALL_OVER", 8, 0);			//소스코드 전송이 끝났음을 알림
			}
			fclose(MFP);
		}
	}//modify1
	else if(!strncmp(buff, "compile!", 8)){					//컴파일 요청

	char nameSet[50];
memset(nameSet, '\0', 50);
	memset(buff, '\0', 1024);
	strcpy(buff, clntIP);
	send(clntSock, buff, strlen(buff), 0);					//클라이언트로 IP 송신
	memset(buff, '\0', 1024);

	while(1){
	char newFile[20];
	memset(newFile, '\0', 1024);
	recv(clntSock, buff, 1024, 0);						//파일의 이름 수신

	if(!strncmp(buff, "NAME_OVER", 9)) break;				//이름 전송이 끝났다는 시그널을 받을때까지 계속 수신한다.
	if(strstr(buff, "NAME_OVER") != NULL){
		memset(buff, '\0', 1024);
		strncpy(mo, strtok(buff, "NAME_OVER"), strlen(buff)-9);		//이름과 시그널이 같이 수신되었을 경우 분리한다.
	}else	
	strncpy(mo, buff, strlen(buff));
	filename = incode(mo, clntIP);						//기존 코드를 수정하여 새로운 파일을 만들고 파일의 이름을 저장

	//파일이 여러 개일 경우 nameSet에 계속 이름을 붙여준다.
	strcpy(newFile, filename);
	printf("filaname: %s\n", filename);
	printf("mo : %s\n", mo);
	printf("newFile : %s\n", newFile);
	printf("nameSet : %s\n", nameSet);
	strncat(nameSet, newFile, strlen(newFile));
	strcat(nameSet, " ");
	memset(buff, '\0', 1024);
	memset(mo, '\0', 55);
	memset(filename, '\0', 20);
	memset(newFile, '\0', 20);
	}
	char exe[30];	//컴파일 명령 문자열
	char err[30];	//에러발생시 리다이렉션 파일
		
	//컴파일 명령어 생성 gcc IP/파일이름 2> err
	strcpy(exe, clntIP);
	strcat(exe, ".out");
	strcpy(err, clntIP);
	strcat(err, "error");
	strcat(cmd, nameSet);
	strcat(cmd, "-o ");
	strncat(cmd, exe, strlen(exe));
	strcat(cmd, " 2>");
	strncat(cmd, err, strlen(err));


	printf("%s\n", cmd);	//생성된 컴파일 명령어 확인
	
	system(cmd);		//컴파일 명령어 실행	
	memset(cmd, '\0', 50);
	strcpy(cmd,"gcc ");	//다른 파일의 다음 실행을 위해 처음으로 초기화
	memset(mo, '\0', 55);

	//2개의 파이프 생성
	if(pipe(in) < 0){
		perror("pipe error");
		exit(-1);
	}
	if(pipe(out) < 0){
		perror("pipe2 error");
		exit(-1);
	}
	
	pid = fork();		//파일 실행은 자식 프로세스에서
	if(pid == -1){
		perror("Failed to Create Process");
		exit(-1);
	}else if(pid == 0){	//파일을 실행하는 자식프로세스
	
	//파일 실행 명령어 생성
	char eexe[30] = "./";
	strncat(eexe, exe, strlen(exe));	
		close(1);
		close(0);
		dup2(in[0], 0);	//파일 디스크립터 복사: 파이프로의 입력이 프로세스의 표준입력이 된다.
		close(in[1]);
		dup2(out[1], 1);//파일 디스크립터 복사: 프로세스의 표준출력이 파이프에 입력된다.
		close(out[0]);

		sleep(0.1);

		FILE *fp = fopen(exe, "r");
		if(fp == NULL){					//실행파일이 없을 경우 에러 파일에서 에러를 전송한다.
			FILE *er = fopen(err, "r");
			while(!feof(er)){
				char buf[100];
				fgets(buf, 100, er);
				send(clntSock, buf, 100, 0);
				memset(buf, '\0', 100);
			}
			fclose(er);
			exit(0);
		}	
		else{						//실행파일이 존재할 경우 실행명령 실행
		fclose(fp);
		execlp(eexe, "test.out", NULL);
		}
	}else {
		int i=0;	
		int status = 0;
	fcntl(out[0], F_SETFL, O_NONBLOCK);			//실행결과를 nonblock으로 받는다.	
		while(1){
		memset(buff, '\0', 1024);
		kill(pid, SIGCONT);

		read(out[0], buff, 1024);		

		buff[strlen(buff)]='\0';
		
		
		if(!strncmp(buff, "BACK", 4)){			//클라이언트가 뒤로가기 버튼을 눌렀을 경우 반복문 탈출
	                break;
	        }
		if(strncmp(buff, "getIt!!", 7)){		//자식프로세스가 입력을 요청하지 않은경우
			if((strlen(buff) != 0)&& (strstr(buff, "getIt!!") == NULL)){
			printf("%s\n", buff);
			send(clntSock, buff, strlen(buff), 0);				//자식프로세스의 출력을 클라이언트로 송신
			if(waitpid(-1, &status, WNOHANG) == pid) break;			//자식프로세스가 종료되었으면 반복문을 탈출한다.
			}
			if(strstr(buff, "getIt!!") != NULL){	//다른 출력과 입력요청이 같이 들어오게 되는 경우 클라이언트에 전송한다.
				printf("%s\n", buff);
                        	send(clntSock, buff, strlen(buff), 0);
				send(clntSock, "getIt!!", 7, 0);

				memset(WR, '\0', 1024);
                        	memset(buff, '\0', 1024);
                        	recv(clntSock, buff, 1024, 0);	//클라이언트의 입력값을 수신한다.
                        	printf("%s\n", buff);

                        	if(!strncmp(buff, "BACK", 4)){	//입력이 뒤로가기 버튼이면 탈출한다.
                        	        break;
                        	}
	
        	                strncpy(WR, buff, strlen(buff));
                	        WR[strlen(WR)] = '\0';
                        kill(pid, SIGSTOP);			
                	        int n = write(in[1], WR, 1024);//클라이언트로부터 받은 입력값을 자식프로세스에 전달
			}
		}
		else if(strstr(buff ,"getIt!!") != NULL){	//자식프로세스가 입력을 요청하는경우(위의 과정과 동일)
			send(clntSock, "getIt!!", 7, 0);
			memset(WR, '\0', 1024);
			memset(buff, '\0', 1024);
			recv(clntSock, buff, 1024, 0);
			printf("%s\n", buff);

			if(!strncmp(buff, "BACK", 4)){
        		        break;
        	        }

			strncpy(WR, buff, strlen(buff));
			WR[strlen(WR)] = '\0';
			kill(pid, SIGSTOP);	
			int n =	write(in[1], WR, 1024);


		}		
		if(waitpid(-1, &status, WNOHANG) == pid){//자식프로세스가 종료되었을경우 마지막 출력을 클라이언트로 전송하고 실행을 종료한다.
		if(read(out[0], buff, 1024) != 0){
                buff[strlen(buff)]='\0';
		send(clntSock, buff, strlen(buff), 0);
		memset(buff, '\0', 1024);
		}
		 break;
		}
		}
		printf("status: %d\n", status);
		if(strstr(buff, "BACK") == NULL)
		send(clntSock, "RUN_OVER", 8, 0);	//실행 종료를 알리는 시그널 전송
		memset(buff, '\0', 1024);

		//수정한 소스코드와 생성된 실행파일 삭제
		char rmF[30] = "rm ";
		char rmS[30] = "rm ";
		strncat(rmF, exe, strlen(exe));
		strncat(rmS, nameSet, strlen(nameSet));
		system(rmF);
		system(rmS);
		memset(exe, '\0', 30);
		memset(nameSet, '\0', 50);
	}
	memset(nameSet, '\0', 50);
	}//compile
	else {	//클라이언트가 메인화면으로 돌아감에 따라 파일목록을 다시 전송
		memset(buff, '\0', 1024);
		ls(clntSock, clntIP);
		memset(buff, '\0', 1024);
		continue;
	}
	}//while
	}//for
	}//fork
	return 0;

}
int AcceptTCPConnection(int servSock, struct sockaddr_in echoClntAddr, char *clntIP){
        //Set the size of the in_out parameter
        int clntLen = sizeof(echoClntAddr);
        int clntSock;
        //Wait for a client to connect
        if((clntSock = accept(servSock, (struct sockaddr*)&echoClntAddr, &clntLen)) < 0)
                        DieWithError("accept() failed");
	else
		printf("client IP1 : %s\n", inet_ntoa(echoClntAddr.sin_addr));
	strcpy(clntIP, inet_ntoa(echoClntAddr.sin_addr));
	ls(clntSock, inet_ntoa(echoClntAddr.sin_addr));
        return clntSock;
}
int ls(int clntSock, char *IP){
	//클라이언트의 IP를 이름으로 하는 폴더를 생성하고 그곳에 ls명령을 리다이렉션하여 보관한다.
	char dir[32] = "mkdir ";
	char ls[50] = "ls ";
	char ip[15];
	char buff[32];
	strncpy(ip, IP, 11);
	ip[strlen(ip)]='\0';
	strcat(dir, IP);
	system(dir);
	strcat(ls, IP);
	strcat(ls, " > ./");
	strcat(ls, IP);
	strcat(ls, "/");
	strcat(ls, IP);
	strcat(ls, ".txt");
	system(ls);
	
	//파일목록이 저장된 파일의 내용을 클라이언트로 전송
	char name[60];
	sprintf(name, "%s/%s.txt", IP, IP);
	name[strlen(name)] = '\0';
	printf("%s\n", name);
	FILE *fp = fopen(name, "r");
	if(fp == NULL)
		DieWithError("fopen() error");
	int n=0;
	while(!feof(fp)){
	fgets(buff, 32, fp);
	printf("%d : %s", n, buff);
	send(clntSock, buff, strlen(buff), 0);
	n++;
	memset(buff, '\0', 32);
	sleep(0.1);
	}
	send(clntSock, "LS_OVER", 7, 0);
	

	fclose(fp);
}
