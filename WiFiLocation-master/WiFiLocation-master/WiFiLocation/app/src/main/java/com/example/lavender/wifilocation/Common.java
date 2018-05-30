package com.example.lavender.wifilocation;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该类用于一些各个页面都会用到的公共的方法
 */

public class Common {
    // 设置手机型号
    public static int getMobileModel() {
        int num = 0;
        String mobile = Build.BRAND;
        HashMap hm = new HashMap();
        hm.put("HUAWEI", "2");
        hm.put("XIAOMI", "3");
        hm.put("OPPO", "6");
        Iterator iter = hm.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = entry.getKey().toString();
            int val = Integer.parseInt(entry.getValue().toString());
            Pattern reg = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
            Matcher matcher = reg.matcher(mobile);
            if (matcher.find()) {
                num = val;
            }
        }
        return num;
    }

    // 获取当前时间
    public static String getNowTime() {
        Date now = new Date();
        DateFormat df = DateFormat.getDateTimeInstance();
        String nowtime = df.format(now);
        return nowtime;
    }

    // 将String转换为Json
    public static JSONObject toJson(String str) {
        try {
            JSONObject json = new JSONObject(str);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
