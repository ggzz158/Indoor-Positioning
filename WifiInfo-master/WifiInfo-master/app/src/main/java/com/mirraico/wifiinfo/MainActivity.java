package com.mirraico.wifiinfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity {

    private TextView textView;

    private ListView listView;
    private List<String> listStr;
    ArrayAdapter<String> adapter;

    CheckBox has_filter;

    Button btn_log;
    private List<String> log;
    ArrayAdapter<String> adapterLog;

    TextView title;

    String oldMac;

    WifiManager wm;
    WifiReceiver wifiReceiver;
    NetworkChangeReceiver networkChangeReceiver;

    SimpleDateFormat sDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        log = new ArrayList<>();
        sDateFormat = new SimpleDateFormat("[hh:mm:ss] ");

        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if(!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
            log.add(sDateFormat.format(new java.util.Date()) + "检测到wifi未打开，尝试开启wifi");
        }
        textView = (TextView) findViewById(R.id.using_wifi);
        listView = (ListView) findViewById(R.id.scan_wifi);
        listStr = new ArrayList<>();
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, listStr);
        listView.setAdapter(adapter);
        has_filter = (CheckBox) findViewById(R.id.cb_filter);
        btn_log = (Button) findViewById(R.id.btn_log);
        adapterLog = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, log);
        title = (TextView) findViewById(R.id.title);

        wifiReceiver = new WifiReceiver();
        IntentFilter intentFilter1= new IntentFilter();
        intentFilter1.addAction("com.mirraico.wifiinfo.UPDATE");
        registerReceiver(wifiReceiver, intentFilter1);

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter2= new IntentFilter();
        intentFilter2.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, intentFilter2);

        has_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listStr.clear();
                adapter.notifyDataSetChanged();
            }
        });

        btn_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_log.getText().equals("连接日志")) {
                    btn_log.setText("周围连接");
                    title.setText("连接日志：");
                    listView.setAdapter(adapterLog);
                    adapterLog.notifyDataSetChanged();
                } else {
                    btn_log.setText("连接日志");
                    title.setText("周围连接：");
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                WifiPrint conn;
                List<WifiPrint> scan = new ArrayList<>();
                while(true) {
                    WifiInfo wi = wm.getConnectionInfo();
                    conn = null;
                    if (wi.getNetworkId() != -1) {
                        String ssid = wi.getSSID();
                        int rssi = wi.getRssi();
                        String mac = wi.getBSSID();
                        if (ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"')
                            ssid = ssid.substring(1, ssid.length() - 1);
                        conn = new WifiPrint(ssid, rssi, mac);
                    }

                    scan.clear();
                    List<ScanResult> scanres = wm.getScanResults();
                    for (ScanResult sr : scanres) {
                        if (conn != null && sr.BSSID.equals(conn.getMac())) continue;
                        if(has_filter.isChecked() && conn != null && !sr.SSID.equals(conn.getSSID())) continue;
                        scan.add(new WifiPrint(sr.SSID, sr.level, sr.BSSID));
                    }
                    Collections.sort(scan);
                    int scanNum = scan.size();

                    Intent intent = new Intent("com.mirraico.wifiinfo.UPDATE");
                    intent.putExtra("conn", conn != null ? conn.toString() : "无连接");
                    intent.putExtra("scan_num", scanNum);
                    for(int i = 0; i < scanNum; i++) {
                        intent.putExtra("scan_" + i, scan.get(i).toString());
                    }
                    sendBroadcast(intent);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(networkChangeReceiver);
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String conn = intent.getStringExtra("conn");
            int scanNum = intent.getIntExtra("scan_num", 0);
            listStr.clear();
            for(int i = 0; i < scanNum; i++) {
                listStr.add(intent.getStringExtra("scan_" + i));
            }
            textView.setText(conn);
            adapter.notifyDataSetChanged();
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            WifiInfo wi = wm.getConnectionInfo();
            if(wi.getNetworkId() == -1) return;
            if(wi.getBSSID().equals(oldMac)) return;
            String ssid = wi.getSSID();
            int rssi = wi.getRssi();
            String mac = wi.getBSSID();
            if (ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"')
                ssid = ssid.substring(1, ssid.length() - 1);
            if(oldMac == null) log.add(sDateFormat.format(new java.util.Date()) + "网络连接至" + ssid + "(" + mac + ") RSSI：" + rssi + "dB");
            else {
                String oldSSID = null; int oldRssi = 0;
                List<ScanResult> scanres = wm.getScanResults();
                for (ScanResult sr : scanres) {
                    if (sr.BSSID.equals(oldMac)) {
                        oldSSID = sr.SSID;
                        oldRssi = sr.level;
                        break;
                    }
                }
                if(oldSSID == null)
                    log.add(sDateFormat.format(new java.util.Date()) + "网络切换至" + ssid + "(" + mac + ") RSSI：" + rssi + "dB，原网络已搜索不到");
                else
                    log.add(sDateFormat.format(new java.util.Date()) + "网络切换至" + ssid + "(" + mac + ") RSSI：" + rssi + "dB，原网络" + oldSSID + "(" + oldMac + ") RSSI：" + oldRssi + "dB");
            }
            oldMac = wi.getBSSID();
        }
    }
}

