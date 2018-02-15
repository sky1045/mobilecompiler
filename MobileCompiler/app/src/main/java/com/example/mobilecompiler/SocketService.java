package com.example.mobilecompiler;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class SocketService extends Service implements Runnable{     //서버와의 백그라운드소켓통신 서비스
    private final IBinder mBinder = new LocalBinder();
    private static final String IP="172.20.10.6"; //서버 IP 입
    private Socket s; //Socket객체
    private InputStream input; //입력스트림
    private OutputStream output; //출력스트림
    private PrintWriter out;
    private Thread handler; //ChatHandler와 메시지를 주고 받기 위한 쓰레드
    private final Handler h = new Handler();
    private String msg = null;
    int size;
    byte[] words = new byte[1024];

    public class LocalBinder extends Binder{    //서비스와 각 화면간 바인드 처리
        public SocketService getService(){
            return SocketService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        handler = new Thread(this);// Thread객체 생성
        //쓰레드 실행, 자동으로 run()메소드 호출
        handler.start ();
    }

    public void run() {
        try {
            //서버의 IP주소, port번호를 가지고 소켓객체 생성
            s = new Socket (IP, 5000);//클라이언트 1단계
            //입출력스트림 얻어냄 - 클라이언트 2단계
            input = s.getInputStream();
            output = s.getOutputStream();
            out = new PrintWriter(output);
        } catch (IOException ex) {

        }
        finally{//메시지 받는 것이 중단될때 수행

        }
    }
//    public boolean isConnected(){
//        if(s != null) return true;
//        else return false;
//    }

    //서버로부터 받은 메시지를 처리
    public String getList(){
        int size;
        byte[] words = new byte[30000];

        String not = "no return";
        try{
            while(true) {
                //서버로부터 받은 메시지를 얻어냄
                size = input.read(words);
                if(size <= 0) {
                    msg = "";
                    Log.d("wait", msg);
                }
                //메시지에 내용이 있으면 문자열 객체를 생성
                else msg = new String(words, 0, size, "utf-8");
                Log.e("str: ", msg+"here");
                return msg;
            }
        }catch(Exception e){
            return not;
        }finally {

        }

    }
    public void sendFile(String str) {          //클라이언트 -> 서버 텍스트 전송
        out.print(str);
        out.flush();
        Log.d("send", str);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;


    }
    @Override
    public void onDestroy() {                   //연결이 종료 처리
        String dest = "Destroyd";               //서버에 종료 시그널 전송
        sendFile(dest);
        Log.e("onDestroy",dest);
        super.onDestroy();
        try {
            s.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        s = null;
    }

}
