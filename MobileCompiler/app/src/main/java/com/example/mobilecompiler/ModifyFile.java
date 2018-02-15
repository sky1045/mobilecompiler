package com.example.mobilecompiler;

import android.app.Activity;
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

public class ModifyFile extends Activity implements View.OnClickListener{
    //위젯들
    private EditText editText3;
    private EditText editText4;
    private Button modifyButton;

    SocketService socketService;
    boolean isServiced = false;
    boolean mBound = false;

    public String fileName;
    public String Source;

    //소켓 연결 서비스와 바인드시 처리
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SocketService.LocalBinder mb = (SocketService.LocalBinder) iBinder;
            socketService = mb.getService();
            Toast.makeText(getApplicationContext(),
                    "서비스 연결 성공",
                    Toast.LENGTH_LONG).show();
            isServiced = true;

            socketService.sendFile("modify2!");                         //서버에 파일 수정 시작을 알림
//            SystemClock.sleep(1000);
            fileName = socketService.getList();                             //서버로부터 파일 이름 수신

            Log.e("name", fileName);
            editText4.setText(""+ fileName);                                //파일 이름 표시
            socketService.sendFile("SOURCE");                           //소스코드 전송을 요청
            Source = socketService.getList();                               //소스코드 수신
            if(Source.contains("ALL_OVER")) {                               //소스코드를 다 받았으면
                Source = Source.replace("ALL_OVER", "");  //코드에서 시그널 부분 제거
                Log.e("source", Source);
                editText3.setText("" + Source);                             //화면에 코드 표시
            }
            else if(!Source.contains("ALL_OVER")){
                //한번에 다 못받았을 경우 다 받을때까지 받아서 표시
                while(true) {
                    Source = Source.concat(socketService.getList());
                    if(Source.contains("ALL_OVER")) {
                        Source = Source.replace("ALL_OVER", "");
                        editText3.setText(Source);
                        break;
                    }else continue;
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
        setContentView(R.layout.activity_modify_file);

        SystemClock.sleep(500);
        //소켓연결 서비스와 바인드
        Intent intent = new Intent(this, SocketService.class);
        mBound = bindService(intent, conn, Context.BIND_AUTO_CREATE);

        editText3 = findViewById(R.id.editText3);
        editText4 = findViewById(R.id.editText4);
        modifyButton = findViewById(R.id.button2);

        modifyButton.setOnClickListener(this);

    }
    public void onClick(View view) {
        if(view == modifyButton){
            try {
                //제목이나 코드가 빈칸일 경우 메시지 표시
                if((editText3.getText().toString().length() ==0) || (editText4.getText().toString().length()==0))
                    Toast.makeText(getApplicationContext(), "제목 혹은 코드를 입력하시오",Toast.LENGTH_LONG ).show();
                else {
                    socketService.sendFile("create1!");                     //수정 과정은 생성과정과 같다
                    socketService.sendFile(editText4.getText().toString());     //파일 이름 전송
                    SystemClock.sleep(100);
//            socketService.sendFile(String.valueOf(size));
                    if (socketService.getList().contains("SOURCE")) {           //소스코드 전송 시작을 알림
                        socketService.sendFile(editText3.getText().toString()); //소스코드 전송
                        SystemClock.sleep(100);
                        socketService.sendFile("FILEOVER");                 //소스코드 전송 완료 알림
                        SystemClock.sleep(500);
                        finish();
                    }
                }

            } catch (Exception e) {
                Log.getStackTraceString(e);
            }
        }
    }
    public void onDestroy(){
        super.onDestroy();
        unbindService(conn);
    }
    @Override
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
}
