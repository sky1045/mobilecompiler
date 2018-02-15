package com.example.mobilecompiler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class GccCompiler extends AppCompatActivity implements View.OnClickListener {

    String out;
    private TextView output;
    private EditText input;
    private Button submit;
    private ScrollView scroll;

    SocketService socketService;
    boolean isServiced = false;
    boolean mBound = false;

    //소켓연결 서비스 바인드시
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SocketService.LocalBinder mb = (SocketService.LocalBinder) iBinder;
            socketService = mb.getService();
            Toast.makeText(getApplicationContext(),
                    "서비스 연결 성공",
                    Toast.LENGTH_LONG).show();
            isServiced = true;

                SystemClock.sleep(100);
                while(true){
                out = socketService.getList();   //서버로부터 파일 실행 결과를 받아옴
                if((!out.contains("getIt!!")) && (!out.contains("RUN_OVER"))) {     //입력을 요구하거나 실행종료가 아닐 경우
                    output.append(out);                                             //실행결과를 화면에 표시
                    continue;                                                       //다음 실행결과를 받아옴
                }else{
                    out = out.replace("getIt!!", "");            //입력을 요구할 경우 입력 요청 시그널을 제거
                    output.append(out);                                             //실행결과를 화면에 표시
                    break;                                                          //실행 종료
                }
                }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("Disconnected ", String.valueOf(isServiced));
            isServiced = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcc_compiler);

        output = findViewById(R.id.textView);
        input = findViewById(R.id.editText5);
        submit = findViewById(R.id.button3);
        scroll = findViewById(R.id.scrollView);
        submit.setOnClickListener(this);

        //소켓 연결 서비스에 바인드
        Intent intent = new Intent(this, SocketService.class);
        mBound = bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == KeyEvent.ACTION_DOWN) { //키 다운 액션 감지
            if (keyCode == KeyEvent.KEYCODE_BACK) { //BackKey 다운일 경우만 처리
                //BackKey 이벤트일 경우 해야할 코드 작성
                socketService.sendFile("BACK");
                finish();

                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onDestroy(){
        super.onDestroy();
        unbindService(conn);
    }


    @Override
    public void onClick(View view) {
        if(view == submit){
                String recv = null;
                if (!output.getText().toString().contains("RUN_OVER")) {                    //실행이 종료되지 않았을 경우
                    socketService.sendFile(input.getText().toString());                     //입력값을 서버로 전송
                    SystemClock.sleep(100);
                    while(true) {
                        recv = socketService.getList();                                     //다음 출력값을 받아옴
                        if((!recv.contains("getIt!!")) && (!recv.contains("RUN_OVER"))) {   //입력을 요청하거나 실행이 종료되지 않았을 경우
                            output.append(recv);                                            //출력값을 화면에 표시
                            continue;
                        }else{
                            recv = recv.replace("getIt!!", "");          //입력 요청 시그널 제거
                            output.append(recv);                                            //화면에 출력 표시
                            scroll.fullScroll(ScrollView.FOCUS_DOWN);
                            break;
                        }
                    }

                }
        }
    }
}



