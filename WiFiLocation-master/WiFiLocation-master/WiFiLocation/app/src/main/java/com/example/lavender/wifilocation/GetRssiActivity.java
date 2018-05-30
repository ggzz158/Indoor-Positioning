package com.example.lavender.wifilocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.lavender.wifilocation.Common.toJson;


public class GetRssiActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnStartGetRssi, btnStopGetRssi;
    private EditText edtCoord, edtMemory;
    private TextView txtShow, txtRoom;
    private String apData = "";
    private Spinner spinner;
    private String room_id;
    private String[] data = new String[]{"科协办公室", "实验室1", "实验室2"};

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            apData = intent.getStringExtra("ap");
            txtShow.setText(apData);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_rssi);
        // 初始化
        init();

        // 注册监听事件
        registerReceiver(receiver, new IntentFilter("apData"));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        stopScan();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartGetRssi:
                startScan();
                break;
            case R.id.btnStopGetRssi:
                stopScan();
                // 发送数据到服务器
                sendDataToServer();
                break;
        }
    }

    // 初始化
    private void init() {
        btnStartGetRssi = (Button) findViewById(R.id.btnStartGetRssi);
        btnStartGetRssi.setOnClickListener(this);
        btnStopGetRssi = (Button) findViewById(R.id.btnStopGetRssi);
        btnStopGetRssi.setOnClickListener(this);
        btnStopGetRssi.setEnabled(false);
        edtCoord = (EditText) findViewById(R.id.edtCoord);
        edtMemory = (EditText) findViewById(R.id.edtMemory);
        txtShow = (TextView) findViewById(R.id.txtShow);
        txtRoom = (TextView) findViewById(R.id.txtRoom);
        spinner = (Spinner) findViewById(R.id.spinner);
        // 设置房间下拉选框
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                room_id = id + 1 + "";
                txtRoom.setText(room_id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // 开始扫描wifi信号强度
    public void startScan() {
        btnStartGetRssi.setEnabled(false);
        btnStopGetRssi.setEnabled(true);
        Intent intent = new Intent(GetRssiActivity.this, GetRSSIService.class);
        startService(intent);
    }

    // 结束扫描wifi信号强度
    public void stopScan() {
        btnStopGetRssi.setEnabled(false);
        btnStartGetRssi.setEnabled(true);
        Intent intent = new Intent(GetRssiActivity.this, GetRSSIService.class);
        stopService(intent);
    }

    // 发送数据到服务器
    public void sendDataToServer() {
        JSONObject json = new JSONObject();
        try {
            json.put("ap", toJson(apData));  // ap的mac地址和信号强度
            json.put("coord", edtCoord.getText().toString()); //采集坐标
            json.put("memory", edtMemory.getText().toString()); //备注
            json.put("flag", 0); // 标记位（备用，以区别不同数据）
            json.put("addtime", Common.getNowTime()); //添加时间
            json.put("mobile_id", Common.getMobileModel()); //手机型号id
            json.put("room_id", room_id); // 房间id
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpConnect httpConnect = new HttpConnect(new HttpConnect.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Toast.makeText(GetRssiActivity.this, output, Toast.LENGTH_SHORT).show();
            }
        });
        httpConnect.execute("POST", httpConnect.FINGERPRINT, json.toString());
    }
}
