package com.example.lavender.wifilocation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lavender on 2017/3/29.
 * wifi信号强度对象，用来存放扫描到的wifi对象的mac地址和rssi，并且对获取的rssi求平均，组合成数据接口需要的格式。
 */

public class WifiRssi {
    private final int MAX = 300;   // 数组的最大长度
    private String mac = "";             // 无线ap的mac
    private int[] rssi = new int[MAX];   // 存放rssi的数组
    private int argRssi = 0;             // 求得的rssi平均值
    private int Size = 0;                // rssi数组的下标

    // 设置mac值
    public void setMacValue(String s) {
        this.mac = s;
    }

    // 获取mac值
    public String getMacValue() {
        return mac;
    }

    // 获取rssi平均值
    public int getArgRssi() {
        return this.argRssi;
    }

    // rssi数组添加值
    public boolean addRssi(int rssi) {
        if (this.Size < MAX) {
            this.rssi[this.Size++] = rssi;
            return true;
        } else {
            return false;
        }
    }

    // rssi数组求平均
    public void calculateRssiArg() {
        int sum = 0;
        if (this.Size <= 0)  // 数组为空直接结束
        {
            return;
        }
        for (int i = 0; i < this.Size; i++) {
            sum += this.rssi[i];
        }
        this.argRssi = sum / this.Size;
    }

    //  将mac和argrssi组成接口所需格式 例如：[23,"ad:aa:2d:d1:2d:3b"]
    public JSONObject getApiFormat() {
        JSONObject json = new JSONObject();
        try {
            json.put("rssi", this.argRssi);
            json.put("mac", this.mac);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
