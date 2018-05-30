package com.example.lavender.wifilocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.lavender.wifilocation.Common.toJson;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    private Button btnLocation, btnScan, btnCollection, btnWalkCollection, btnSetBaseData, btnTest;
    private String apData;
    private TextView showData, txtCoord, txtRoom, txtAlgorithm, txtStepLength;
    private EditText edtActualCoord;
    private Spinner spinner, spinnerAlgorithm;
    private String room_id, algorithm;
    private Paint paint = new Paint();
    private Canvas canvas = new Canvas();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            apData = intent.getStringExtra("ap");
            showData.setText(apData);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        init();
        registerReceiver(receiver, new IntentFilter("apData"));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void init() {
        btnLocation = (Button) findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(this);
        btnLocation.setEnabled(false);
        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
        btnCollection = (Button) findViewById(R.id.btnCollection);
        btnCollection.setOnClickListener(this);
        btnWalkCollection = (Button) findViewById(R.id.btnWalkCollection);
        btnWalkCollection.setOnClickListener(this);
        btnSetBaseData = (Button) findViewById(R.id.btnSetBaseData);
        btnSetBaseData.setOnClickListener(this);
        showData = (TextView) findViewById(R.id.showRssiData);
        txtCoord = (TextView) findViewById(R.id.textCoord);
        txtRoom = (TextView) findViewById(R.id.txtRoom);
        txtAlgorithm = (TextView) findViewById(R.id.txtAlgorithm);
        txtStepLength = (TextView) findViewById(R.id.txtStepLength);
        SharedPreferences sharedPreferences = getSharedPreferences("wifiLocationData", MODE_PRIVATE);
        if (sharedPreferences.contains("stepLength")) {
            txtStepLength.setText(txtStepLength.getText() + sharedPreferences.getString("stepLength", ""));
        } else {
            alertMessage();
        }
        edtActualCoord = (EditText) findViewById(R.id.edtActualCoord);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.room_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                room_id = id + 1 + "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerAlgorithm = (Spinner) findViewById(R.id.spinnerAlogrithm);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.algorithm_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlgorithm.setAdapter(adapter2);
        spinnerAlgorithm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                algorithm = id + "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void alertMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
        builder.setMessage("您的步长信息未采集,请先去采集步长信息");
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intentStep = new Intent(Main2Activity.this, GetStepLengthActivity.class);
                startActivity(intentStep);
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                btnLocation.setEnabled(true);
                btnScan.setEnabled(false);
                apData = "";
                showData.setText("");
                Intent intent = new Intent(Main2Activity.this, GetRSSIService.class);
                startService(intent);
                break;
            case R.id.btnLocation:
                locationPost();
                btnLocation.setEnabled(false);
                btnScan.setEnabled(true);
                break;
            case R.id.btnCollection:
                Intent intent1 = new Intent(Main2Activity.this, GetRssiActivity.class);
                startActivity(intent1);
                break;
            case R.id.btnWalkCollection:
                Intent intent2 = new Intent(Main2Activity.this, SensorTestActivity.class);
                startActivity(intent2);
                break;
            case R.id.btnSetBaseData:
                Intent intent3 = new Intent(Main2Activity.this, GetStepLengthActivity.class);
                startActivity(intent3);
                break;
        }
    }

    private void locationPost() {
        Intent intent = new Intent(Main2Activity.this, GetRSSIService.class);
        stopService(intent);

        JSONObject json = new JSONObject();
        try {
            json.put("room_id", room_id);
            json.put("mobile_id", Common.getMobileModel());
            json.put("actual_coord", edtActualCoord.getText());
            json.put("algorithm", algorithm);
            json.put("ap", toJson(apData));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnect http = new HttpConnect(new HttpConnect.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                txtCoord.setText("您的当前位置为：" + output);
            }
        });
        http.execute("POST", http.LOCATION, json.toString());
    }
}
