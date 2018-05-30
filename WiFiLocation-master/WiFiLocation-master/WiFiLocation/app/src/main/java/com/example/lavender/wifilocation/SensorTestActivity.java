package com.example.lavender.wifilocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/*这个页面用来做测试，通过步数和方向来记录轨迹计算坐标，采集数据存入数据库为后期做比对*/

public class SensorTestActivity extends AppCompatActivity implements View.OnClickListener {
    private Button startSensor, stopSensor;
    private TextView showSenserData;
    private EditText memory;
    private String txt = "";
    public static String res = "";
    private int stepCount;

    // 接收服务传来的数据
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stepCount = intent.getIntExtra("step", 0);
            float degree0 = intent.getFloatExtra("degree0", 0);
            calculateDegree(degree0);
            txt += "[" + coordThis[0] + "," + coordThis[1] + "],";
            showSenserData.setText(txt + "||方向角" + degreeNow);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_test);
        init();

        //注册添加对象
        registerReceiver(receiver, new IntentFilter("sensorData"));
        registerReceiver(receiver, new IntentFilter("httpResponse"));
        registerReceiver(receiver, new IntentFilter("step"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 开始采集
            case R.id.btnStartSenser:
                startSensor.setEnabled(false);
                stopSensor.setEnabled(true);
                startGetCoordData();
                break;
            // 结束采集
            case R.id.btnStopSenser:
                startSensor.setEnabled(true);
                stopSensor.setEnabled(false);
                stopGetCoordData();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        SetCoordZero();
        super.onDestroy();
    }

    // 初始化
    private void init() {
        startSensor = (Button) findViewById(R.id.btnStartSenser);
        stopSensor = (Button) findViewById(R.id.btnStopSenser);
        startSensor.setOnClickListener(this);
        stopSensor.setOnClickListener(this);
        stopSensor.setEnabled(false);
        showSenserData = (TextView) findViewById(R.id.showSenserData);
        memory = (EditText) findViewById(R.id.memory);
    }

    // 开始采集坐标数据
    private void startGetCoordData() {
        Intent intent = new Intent(SensorTestActivity.this, GetCoordService.class);
        startService(intent);
    }

    //  结束采集坐标数据
    private void stopGetCoordData() {
        Intent intent = new Intent(SensorTestActivity.this, GetCoordService.class);
        stopService(intent);
        endDegree();
        txt += "[" + coordThis[0] + "," + coordThis[1] + "],";
        showSenserData.setText(txt + "方向角:" + degreeNow);
        // 将采集的数据发送到服务器
        JSONObject json = new JSONObject();

        try {
            json.put("coord", txt.substring(0, txt.length() - 1));
            json.put("memory", memory.getText().toString());
            json.put("addtime", Common.getNowTime());
            json.put("flag", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpConnect httpConnect = new HttpConnect(new HttpConnect.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Toast.makeText(SensorTestActivity.this, output, Toast.LENGTH_SHORT).show();
            }
        });
        httpConnect.execute("POST", httpConnect.APIPOSTTEST, json.toString());
    }

    // 计算坐标
    private float[] degree = new float[100];
    private int degreeLen = 0;
    private float degreeNow = 0;
    private int preStep = 0, nowStep = 0;
    // 正常人步长 50cm
    private static final int STEP = 55;
    // 初始坐标  上一点坐标  当前坐标  单位cm
    private int[] coordInit = {0, 0, 0};
    private int[] coordThis = {0, 0, 0};
    private int[] coordPre = {0, 0, 0};

    // 计算方向角
    private void calculateDegree(float degree0) {
        degree[degreeLen++] = degree0;
        if (degreeLen > 2) {
            // 方向角幅度如果大于20，说明改变方向。小于20代表浮动
            if (Math.abs(degree[degreeLen - 1] - degree[degreeLen - 2]) > 20) {
                degreeNow = average(degree, degreeLen);
                degreeLen = 0;
                nowStep = stepCount - preStep;
                preStep = stepCount;
                GetCoord(STEP * nowStep, degreeNow);
            }
        }
    }

    private void endDegree() {
        degreeNow = average(degree, degreeLen);
        nowStep = stepCount - preStep;
        preStep = stepCount;
        GetCoord(STEP * nowStep, degreeNow);
    }

    private float average(float[] degree, int degreeLen) {
        float sumTotal = 0;
        for (int i = 0; i < degreeLen; i++) {
            sumTotal += degree[i];
        }
        return sumTotal / degreeLen;
    }

    private int tempx = 0, tempy = 0;

    //   获取传感数据，计算坐标
    public void GetCoord(int len, float degree) {
        int[] coord = new int[3];
        coord[0] = (int) (len * Math.sin(Math.toRadians(degree)));
        coord[1] = (int) (len * Math.cos(Math.toRadians(degree)));
        coord[2] = 0;
        tempx = coord[0];
        tempy = coord[1];

        if (IsCoordEqual(coordInit, coordThis)) {
            coordThis = CoordAdd(coordInit, coord);
        } else {
            int[] temp = coordPre;
            coordPre = coordThis;
            coordThis = CoordAdd(temp, coord);
        }
    }

    // 坐标相加
    private int[] CoordAdd(int[] coord1, int[] coord2) {
        int[] result = new int[3];
        result[0] = coord1[0] + coord2[0];
        result[1] = coord1[1] + coord2[1];
        result[2] = coord1[2] + coord2[2];
        return result;
    }

    // 判断坐标相等
    private boolean IsCoordEqual(int[] coord1, int[] coord2) {
        boolean flag = false;
        if (coord1[0] == coord2[0] && coord1[1] == coord2[1] && coord1[2] == coord2[2])
            flag = true;
        else
            flag = false;
        return flag;
    }

    // 坐标清零
    private void SetCoordZero() {
        for (int i = 0; i < 3; i++) {
            coordInit[i] = 0;
            coordPre[i] = 0;
            coordThis[i] = 0;
        }
    }
}
