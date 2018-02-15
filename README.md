# mobilecompiler
리눅스서버(gcc)와 안드로이드 클라이언트간 소켓통신을 통해 모바일 환경에서 소스코드를 작성 및 실행할 수 있습니다.

서버 실행환경: 우분투 터미널(gcc컴파일러 stdio_ext라이브러리 포함)
클라이언트: 안드로이드 7.0(api24)이상

서버: mobilecompiler.c, incode.c, DieWithError.c를 같이 컴파일하여 실행
서버를 먼저 실행하지 않으면 클라이언트가 실행될 수 없다.
