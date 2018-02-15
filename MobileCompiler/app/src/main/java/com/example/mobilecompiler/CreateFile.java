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
import android.widget.Toast;

public class CreateFile extends AppCompatActivity implements View.OnClickListener {

    //위젯들
    private EditText editText;
    private EditText editText2;
    private Button createButton;

    //서비스 연결에 관한 변수들
    SocketService socketService;
    boolean isServiced = false;
    boolean mBound = false;
    //서비스와 바인드시
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SocketService.LocalBinder mb = (SocketService.LocalBinder) iBinder;
            socketService = mb.getService();
            Toast.makeText(getApplicationContext(),
                    "서비스 연결 성공",
                    Toast.LENGTH_LONG).show();
            isServiced = true;

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
        setContentView(R.layout.activity_create_file);

        //소켓연결 서비스와 바인드
        Intent intent = new Intent(this, SocketService.class);
        mBound = bindService(intent, conn, Context.BIND_AUTO_CREATE);

        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);
        createButton = findViewById(R.id.button);
        createButton.setOnClickListener(this);
    }
    public void onDestroy(){
        //액티비티 종료시 소켓 연결 해제
        super.onDestroy();
        unbindService(conn);
    }
    @Override
    public void onClick(View view) {
        if (view == createButton) {//[보내기]버튼을 클릭한 경우
        try {
            //이름 혹은 코드에 입력된 텍스트가 없을 경우 메시지 출력
            if((editText.getText().toString().length() ==0) || (editText2.getText().toString().length()==0))
                Toast.makeText(getApplicationContext(), "파일 이름 혹은 코드를 입력하시오",Toast.LENGTH_LONG ).show();
            else if(editText2.getText().toString().contains(" "))   //파일 이름에 공백이 포함된 경우
                Toast.makeText(getApplicationContext(), "파일 이름에 공백이 포함될 수 없습니다.",Toast.LENGTH_LONG ).show();
            else {
                socketService.sendFile("create1!");                     //파일 생성을 위한 시그널 전송
                SystemClock.sleep(500);
                socketService.sendFile(editText2.getText().toString());     //파일 이름 전송

                if (socketService.getList().contains("SOURCE")) {           //소스코드 전송을 시작함을 알림
                    socketService.sendFile(editText.getText().toString());
                    SystemClock.sleep(100);
                    socketService.sendFile("FILEOVER");                 //소스코드 전송이 끝났음을 알림
                    SystemClock.sleep(500);
                    finish();
                }
            }

        } catch (Exception e) {
            Log.getStackTraceString(e);
        }
    }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == KeyEvent.ACTION_DOWN) { //키 다운 액션 감지
            if (keyCode == KeyEvent.KEYCODE_BACK) { //BackKey 다운일 경우만 처리
                //BackKey 이벤트일 경우 해야할 코드 작성
                socketService.sendFile("BACK"); //메인 화면으로 이동함을 알려 파일 목록을 요청한다
                finish();

                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
