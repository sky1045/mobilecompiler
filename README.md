# mobilecompiler
리눅스서버(gcc)와 안드로이드 클라이언트간 소켓통신을 통해 모바일 환경에서 소스코드를 작성 및 실행할 수 있습니다.

서버 실행환경: 우분투 터미널(gcc컴파일러 stdio_ext라이브러리 포함)
클라이언트: 안드로이드 7.0(api24)이상

서버: mobilecompiler.c, incode.c, DieWithError.c를 같이 컴파일하여 실행
     서버를 먼저 실행하지 않으면 클라이언트가 실행될 수 없다.
     
클라이언트: MobileCompiler폴더를 프로젝트로 임포트하여 실행, SocketService.java에서 IP를 사용중인 네트워크의 IP로 변경해주어야 한다.
