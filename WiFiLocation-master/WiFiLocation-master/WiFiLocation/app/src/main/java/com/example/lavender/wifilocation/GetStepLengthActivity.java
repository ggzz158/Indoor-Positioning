package com.example.lavender.wifilocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GetStepLengthActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStart, btnEnd;
    // 起始经度，纬度，终点经度，纬度
    private double lat1;
    private double lat2;
    private double lnt1;
    private double lnt2;
    private double distance;

    @Override
    protected void onStop() {
        SharedPreferences sharedPreferences = getSharedPreferences("wifiLocationData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("stepLength", Math.round(stepLength * 100) / 100D + "");
        editor.commit();
        super.onStop();
    }

    //步长
    private double stepLength;
    // 步数
    private int stepCount = 0;
    private TextView txtStepLenght, txtLongitude, txtTitle, txtLatitude, txtStepCount;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean b = intent.hasExtra("startLatitude");
            lat1 = intent.getDoubleExtra("startLatitude", 0.0);
            lat2 = intent.getDoubleExtra("endLatitude", 0.0);
            lnt1 = intent.getDoubleExtra("startLongitude", 0.0);
            lnt2 = intent.getDoubleExtra("endLongitude", 0.0);
            distance = GetDistance(lat1, lnt1, lat2, lnt2);
            txtLongitude.setText(lnt1 + "");
            txtLatitude.setText(lat1 + "");
        }
    };

    private BroadcastReceiver receiverStep = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stepCount += intent.getIntExtra("step", 0);
            txtStepCount.setText(stepCount + "");
        }
    };

    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    // 根据起始经纬度计算两点之间的距离
    public static double GetDistance(double lat1, double lng1, double lat2, double lng2) {
            double radLat1 = rad(lat1);
            double radLat2 = rad(lat2);
            double a = radLat1 - radLat2;
            double b = rad(lng1) - rad(lng2);
            double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                    Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
            s = s * EARTH_RADIUS;
            s = Math.round(s * 100000);
            return s;
    }

    private void calculateCountLength() {
        stepLength = distance / stepCount;
        txtStepLenght.setText(stepLength + "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                Intent intentGps = new Intent(GetStepLengthActivity.this, GetLocationService.class);
                Intent intentStep = new Intent(GetStepLengthActivity.this, GetCoordService.class);
                startService(intentGps);
                startService(intentStep);
                break;
            case R.id.btnEnd:
                Intent intentGps1 = new Intent(GetStepLengthActivity.this, GetLocationService.class);
                Intent intentStep1 = new Intent(GetStepLengthActivity.this, GetCoordService.class);
                stopService(intentGps1);
                stopService(intentStep1);
                calculateCountLength();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_step_length);
        init();
        registerReceiver(receiver, new IntentFilter("gps"));
        registerReceiver(receiverStep, new IntentFilter("sensorData"));
    }

    private void init() {
        btnStart = (Button) findViewById(R.id.btnStart);
        btnEnd = (Button) findViewById(R.id.btnEnd);
        btnStart.setOnClickListener(this);
        btnEnd.setOnClickListener(this);
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        txtStepLenght = (TextView) findViewById(R.id.txtStepLength);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtStepCount = (TextView) findViewById(R.id.txtStepCount);
    }
}
