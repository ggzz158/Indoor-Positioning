package com.example.lavender.wifilocation;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import static com.example.lavender.wifilocation.Common.toJson;

/**
 * Created by lavender on 2017/3/21.
 */

/**
 * 该类作用：http通信类，主要负责app端和服务器端数据通信。
 * 在调用execute方法时传入三个参数：请求方式   请求接口地址  发送的数据
 */

public class HttpConnect extends AsyncTask<String, String, String> {
    // 服务器端IP地址
    public static final String IP = "192.168.43.249";
    public static final String IP2 = "192.168.191.1";
    public static final String IP3 = "192.168.155.1";

    public static final String BASE_URL = "http://" + IP2 + ":8080";
    //    public static final String BASE_URL = "https://www.baidu.com/";
    public static final String TAG = "HttpConnect";
    public static final String UTF_8 = "UTF-8";
    public static final String APITEST = "/api/DataTest/26";         // 测试http get接口
    public static final String APIPOSTTEST = "/api/DataTest/";      // 上传传感器数据接口
    public static final String FINGERPRINT = "/api/Fingerprint/";   // 采集指纹库接口
    public static final String LOCATION = "/api/Location/";          // 定位接口
    public static final String ROOMLIST = "/api/Room/";

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public HttpConnect(AsyncResponse delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        delegate.processFinish(s);
        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(String[] params) {
        // 三个参数：请求方式   请求接口地址  发送的数据
        String method = params[0];
        String url = BASE_URL + params[1];
        JSONObject data = toJson(params[2]);

        // get 返回的数据  post， delete 返回的状态
        String getData = "";
        boolean temp = false;

        // 根据请求方式不同调用不同的函数
        switch (method) {
            case "GET":
                getData = HttpGet(url, data);
                break;
            case "POST":
                getData = HttpPost(url, data);
                break;
        }
        return getData;
    }

    // get
    public String HttpGet(String url, JSONObject data) {
        // 发送请求
        HttpURLConnection httpURLConnection = null;
        StringBuilder response = new StringBuilder();
        // 在url上添加get的参数
        String urlData = "";
        if (data.keys().hasNext()) {
            urlData = JsonToString_url(data);
            if (urlData != "") {
                url += "?" + urlData;
            }
        }

        try {
            Log.i(TAG, "new url");
            URL myurl = new URL(url);
            httpURLConnection = (HttpURLConnection) myurl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setConnectTimeout(8000);

            Log.i(TAG, "connect");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == httpURLConnection.HTTP_OK) {
                InputStream in = httpURLConnection.getInputStream();
                Log.i(TAG, "read inputstream data");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF_8));
                Log.i(TAG, "add data in response");
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "GET failure");
            response.append("请求失败,网络不可达！");
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return response.toString();
    }

    // post
    public String HttpPost(String url, JSONObject data) {
        // 发送请求
        HttpURLConnection httpURLConnection = null;
        StringBuilder response = new StringBuilder();
        try {
            Log.i(TAG, "new url");
            URL myurl = new URL(url);
            httpURLConnection = (HttpURLConnection) myurl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Charset", UTF_8);

            Log.i(TAG, "add post data");
            httpURLConnection.connect();

            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF_8));
            writer.append(data.toString());
            writer.flush();
            writer.close();
            os.close();

            Log.i(TAG, "connect");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == httpURLConnection.HTTP_OK) {
                InputStream in = httpURLConnection.getInputStream();
                Log.i(TAG, "read inputstream data");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF_8));
                Log.i(TAG, "add data in response");
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } else if (responseCode >= 500) {
                response.append("服务器错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Post failure");
            response.append("请求失败,网络不可达！");
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return response.toString();
    }

    //    解析json，获取键值对 转换成get的参数
    public String JsonToString_url(JSONObject jsonObject) {
        String response = "";
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            try {
                String key = keys.next().toString();
                String value = jsonObject.getString(key);
                response += key + "=" + value + "&";
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "json getString error");
            }
        }
        response = response.substring(0, response.length() - 1);
        return response;
    }

}
