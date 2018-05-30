package com.example.lavender.wifilocation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*这个服务用来获取wifi信号列表，并对信号进行滤波，聚类进行优化处理。最终得到一组准确度高的信号强度列表。*/

public class GetRSSIService extends Service {
    private WifiManager wifiManager;
    private Timer timer;
    // 存储获取的信号强度列表
    public ArrayList<WifiRssi> wifiList = new ArrayList<WifiRssi>();
    // 存储已经采集的ap的mac地址列表
    public ArrayList<String> wifiMacList = new ArrayList<String>();
    // 发送至服务器的数据
    public JSONObject sendToServerData = new JSONObject();

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "开始扫描信号强度", Toast.LENGTH_SHORT).show();
        // 清空当前数据
        wifiList.clear();
        wifiMacList.clear();
        //打开wifi
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 关闭定时器，停止采集
        stopTimer();
        // 将数据发送到前台显示
        sendToRssiActivity();
        Toast.makeText(this, "wifi信号强度扫描结束", Toast.LENGTH_SHORT).show();
    }

    public GetRSSIService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // 开启计时器
    private void startTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getWifiRssi();
            }
        };
        timer.schedule(task, 1000, 3000);
    }

    // 停止计时器
    private void stopTimer() {
        timer.cancel();
    }

    // 获取wifi信号强度
    private void getWifiRssi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        Log.i("WIFI", "get wifiManager");
        // 获取到周围的wifi列表
        List<ScanResult> scanResults = wifiManager.getScanResults();
        Log.i("WIFI", "get scanResults");
        for (ScanResult scanResult : scanResults) {
            WifiRssi wifiRssi = new WifiRssi();
            if (scanResult.SSID.equals("Landlord"))  // 目前只获取iXAUT的wifi是因为这些路由器是固定的，便于实验
            {
                // 判断wifiList中是否有该ap的Rssi对象
                if (wifiMacList.contains(scanResult.BSSID)) {
                    WifiRssi wifiRssi1 = getWifiRssi(scanResult.BSSID);
                    if (!wifiRssi1.addRssi(scanResult.level)) {
                        Log.e("WIFI", "addRssi error");
                    }
                } else {
                    // 将新的Rssi对象添加进wifi列表
                    wifiRssi.setMacValue(scanResult.BSSID);
                    if (wifiRssi.addRssi(scanResult.level)) {
                        wifiList.add(wifiRssi);
                        wifiMacList.add(scanResult.BSSID);
                    }
                }
            }
            if (scanResult.SSID.equals("haku"))  // 目前只获取iXAUT的wifi是因为这些路由器是固定的，便于实验
            {
                // 判断wifiList中是否有该ap的Rssi对象
                if (wifiMacList.contains(scanResult.BSSID)) {
                    WifiRssi wifiRssi1 = getWifiRssi(scanResult.BSSID);
                    if (!wifiRssi1.addRssi(scanResult.level)) {
                        Log.e("WIFI", "addRssi error");
                    }
                } else {
                    // 将新的Rssi对象添加进wifi列表
                    wifiRssi.setMacValue(scanResult.BSSID);
                    if (wifiRssi.addRssi(scanResult.level)) {
                        wifiList.add(wifiRssi);
                        wifiMacList.add(scanResult.BSSID);
                    }
                }
            }
        }
        sendToRssiActivity();
    }

    // 将获取的信号强度按照json格式返回给Activity
    private void sendToRssiActivity() {
        sendToServerData = new JSONObject();
        for (int i = 0; i < wifiList.size(); i++) {
            WifiRssi wifiRssi = wifiList.get(i);
            wifiRssi.calculateRssiArg();
            try {
                sendToServerData.put("ap" + i, wifiRssi.getApiFormat());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent();
        intent.setAction("apData");
        intent.putExtra("ap", sendToServerData.toString());
        sendBroadcast(intent);
    }

    // 通过mac地址获取wifiRssi对象
    private WifiRssi getWifiRssi(String mac) {
        WifiRssi wifiRssi = new WifiRssi();
        for (int i = 0; i < wifiList.size(); i++) {
            wifiRssi = wifiList.get(i);
            if (wifiRssi.getMacValue().equals(mac)) {
                break;
            }
        }
        return wifiRssi;
    }
}
