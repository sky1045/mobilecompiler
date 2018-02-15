package com.example.mobilecompiler;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity
        implements OnClickListener {


    SocketService socketService;
    boolean isServiced = false;
    boolean mBound = false;
    //위젯들
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button5;
    private ListView listView;
    public static ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter<String> simpleAdapter1;


    private String list = null;
    //소켓연결 서비스와 바인드시 콜백함수
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
            arrayList.clear();

            while(true) {
                String list2;
                //서버로부터 파일목록을 받아옴
                list = socketService.getList();
                if(list.contains("no return")) continue;

                    list2 = list.replace("LS_OVER", "");    //파일 목록이 끝났을 경우 받는 시그널 제거
                String arg[] = list2.split("\n");
                for (String e : arg) {              //파일목록을 리스트에 추가
                    if ((e != "RUN_OVER") )
                        arrayList.add(e);
                    Log.i("e: ", e);
                }
                if(list.contains("LS_OVER")) break;
                else{
                    continue;
                }
            }
            simpleAdapter1.notifyDataSetChanged();  //화면에 리스트 표시
        }
        //연결 종료시
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("Disconnected ", String.valueOf(isServiced));

            isServiced = false;
        }
    };

    public void onStart(){
        super.onStart();

                simpleAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, arrayList);
                listView.setAdapter(simpleAdapter1);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
    public void onRestart(){
        super.onRestart();
        //메인화면으로 다시 돌아왔을 경우 서버로부터 파일목록을 다시 요청
        socketService.sendFile("RESTART_");
        Intent intent = getIntent();
        finish();
        startActivity(intent);

        simpleAdapter1.notifyDataSetChanged();

    }
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //StrictMode의 모든 규약을 허용 후 적용 -네트워크 사용때문에 기술
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //위젯들을 참조하기 위해 업어냄
        button1 = (Button) findViewById(R.id.Button1);
        button2 = (Button) findViewById(R.id.Button2);
        button3 = (Button) findViewById(R.id.Button3);
        button5 = (Button) findViewById(R.id.Button5);
        listView = (ListView) findViewById(R.id.listView);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button5.setOnClickListener(this);

        //액티비티 시작시 소켓연결서비스와 자동으로 바인드
        Intent intent = new Intent(this, SocketService.class);
        mBound = bindService(intent, conn, Context.BIND_AUTO_CREATE);

        //Checked if my service is running
        if (!isMyServiceRunning()) {
            //if not, I start it.
            startService(new Intent(this,SocketService.class));
        }

    }
    //소켓 연결상태 확인
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (SocketService.class.getName().equals(
                    service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void onStop(){
        super.onStop();

    }
    public void onDestroy(){
        super.onDestroy();
        unbindService(conn);        //액티비티 종료시 소켓 연결 해제
    }

    //메시지를 서버로 보내는 부분을 담당
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == button1) {     //파일 생성 버튼
            try {
                //파일 생성 화면으로 이동
                Intent intent = new Intent(this, CreateFile.class);
                startActivity(intent);

            } catch (Exception e) {
            }
        }else if(v == button2)  //삭제 버튼
        {
            if(listView.getCheckedItemCount() < 1 ){    //선택된 항목이 없을경우 메시지 출력
                Toast.makeText(getApplicationContext(),
                        "삭제할 파일을 선택하시오.",
                        Toast.LENGTH_LONG).show();
            }else {
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                int count = simpleAdapter1.getCount();

                socketService.sendFile("delete1!"); //삭제를 위한 시그널 전송
                SystemClock.sleep(100);
                for (int i = count - 1; i >= 0; i--) {  //선택된 항목의 이름을 전송
                    if (checkedItems.get(i)) {
                        socketService.sendFile(simpleAdapter1.getItem(i));
                        Log.e("item: ", simpleAdapter1.getItem(i));
                        SystemClock.sleep(500);
                        simpleAdapter1.remove(simpleAdapter1.getItem(i));   //선택된 항목을 리스트에서 제거
                    }
                }

                socketService.sendFile("RM_OVER");  //이름 전송이 끝났음을 알림
                listView.clearChoices();                //선택항목 초기화
                simpleAdapter1.notifyDataSetChanged();  //리스트 갱신
            }
        }else if(v == button3){    //수정 버튼
            if(listView.getCheckedItemCount() != 1){    //선택된 항목이 한 개가 아닐 경우 메시지 출력
                Toast.makeText(getApplicationContext(),
                        "하나의 파일을 선택하시오.",
                        Toast.LENGTH_LONG).show();
            }else{
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                int count = simpleAdapter1.getCount();
                socketService.sendFile("modify1!"); //수정을 위한 시그널 전송
                SystemClock.sleep(100);
                for(int i = count-1; i>=0; i--){
                    if (checkedItems.get(i)){           //선택된 항목의 이름 전송
                        socketService.sendFile(simpleAdapter1.getItem(i));
                        Log.e("item: ", simpleAdapter1.getItem(i));

                        break;
                    }
                }
                //파일을 수정하는 액티비티로 이동
                Intent intent = new Intent(this, ModifyFile.class);
                startActivity(intent);
            }
        }else if(v == button5){                             //컴파일 버튼
            if(listView.getCheckedItemCount() < 1 ){        //선택된 항목이 없을 경우 메시지 출력
                Toast.makeText(getApplicationContext(),
                        "실행할 파일을 선택하시오.",
                        Toast.LENGTH_LONG).show();
            }else {
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                int count = simpleAdapter1.getCount();

                socketService.sendFile("compile!");     //컴파일을 위한 시그널 전송
                SystemClock.sleep(100);
                String ip = socketService.getList();        //클라이언트의 IP 수신
                for (int i = count - 1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        socketService.sendFile(ip+"/"+simpleAdapter1.getItem(i));       //IP/파일제목 형태로 전송
                        SystemClock.sleep(500);
                    }
                }
                SystemClock.sleep(500);

                socketService.sendFile("NAME_OVER");    //이름 전송이 끝났음을 알림
                //파일 실행 액티비티로 이동
                Intent intent = new Intent(this, GccCompiler.class);
                startActivity(intent);
            }
        }
    }

}



