package com.example.gxw.indoorlocation;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

//              wifi对应表
//              301：shufang    AP1
//              302：PDCN       AP2
//              303：AP3        AP3
//              304：hhh        AP4
//              305：haku       AP5


public class Collect extends AppCompatActivity {
    Button btn;
    TextView tv;
    EditText editText;
    EditText editText3;
    int count;
    float Xcoord;//X轴坐标
    float Ycoord;//X轴坐标

    StringBuffer buffer;
    float AP1, AP2,AP4, AP5;
    float shufang, PDCN, AP3, hhh, haku;


    //实现wifi信息采集工作，只采集特定的信号源
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        btn = (Button) findViewById(R.id.button4);
        tv = (TextView) findViewById(R.id.textView2);
        editText=findViewById(R.id.editText);
        editText3=findViewById(R.id.editText3);



        System.out.println(Xcoord);
        System.out.println(Ycoord);
        tv.setText("AP1:" + AP1 + "\n"+"AP2:" + AP2 + "\n"+ "AP3:" + AP3 + "\n"+ "AP4:" + AP4 + "\n"+ "AP5:" + AP5);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Xcoord=Float.valueOf(editText.getText().toString());
                Ycoord=Float.valueOf(editText3.getText().toString());
                init();
                waiting();
            }
        });
    }

    public void init() {                 //数据初始化
        AP1 = 0;
        AP2 = 0;
        AP4 = 0;
        AP5 = 0;
        count = 0;
        shufang = 0;
        PDCN = 0;
        AP3 = 0;
        hhh = 0;
        haku = 0;
    }

    private void scan() {
        WifiManager wm;           //WifiManager
        wm=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wm.startScan();                                  //开始扫描AP

        List<ScanResult> results = wm.getScanResults();  //拿到扫描的结果
        for (ScanResult result : results) {
            if (result.SSID.equals("shufang") && result.level > -100) {
                shufang = result.level;
            }
            if (result.SSID.equals("PDCN") && result.level > -100) {
                PDCN = result.level;
            }
            if (result.SSID.equals("AP3") && result.level > -100) {
                AP3 = result.level;
            }
            if (result.SSID.equals("hhh") && result.level > -100) {
                hhh = result.level;
            }
            if (result.SSID.startsWith("haku") && result.level > -100) {
                haku = result.level;
            }
        }
    }
    private void delay(int ms){     //延时函数
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void waiting() {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Object[] objects = new Object[5];
                while (shufang == 0 || PDCN == 0 || AP3 == 0 || hhh == 0 || haku == 0) {
                    scan();
                }
                objects[0] = shufang;
                objects[1] = PDCN;
                objects[2] = AP3;
                objects[3] = hhh;
                objects[4] = haku;
                publishProgress(objects);
                return null;
            }
            @Override
            protected void onProgressUpdate(Object[] values) {
                super.onProgressUpdate(values);
                AP1 = (float) values[0];
                AP2 = (float) values[1];
                AP3 = (float) values[2];
                AP4 = (float) values[3];
                AP5 = (float) values[4];
                System.out.println(AP1);
                System.out.println(AP2);
                System.out.println(AP3);
                System.out.println(AP4);
                System.out.println(AP5);
                tv.setText("AP1:" + AP1 + "\n" + "AP2:" + AP2 + "\n" + "AP3:" + AP3 + "\n" + "AP4:" + AP4 + "\n" + "AP5:" + AP5);

                delay(300);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        URL url = null;
                        HttpURLConnection conn = null;
                        try {
                            url = new URL("http://192.168.1.107:8080/RssiWeb/main");
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                            conn.connect();
                            OutputStream out = conn.getOutputStream();
                            //创建字符流对象并用高效缓冲流包装它，便获得最高的效率
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

                            JSONObject jsonObject=new JSONObject();
                            jsonObject.put("Xcoord",Xcoord);
                            jsonObject.put("Ycoord",Ycoord);
                            jsonObject.put("AP1",AP1);
                            jsonObject.put("AP2",AP2);
                            jsonObject.put("AP3",AP3);
                            jsonObject.put("AP4",AP4);
                            jsonObject.put("AP5",AP5);

                            bw.write(jsonObject.toString());//把json字符串写入缓冲区中
                            System.out.println(jsonObject.toString());
                            bw.flush();//刷新缓冲区，把数据发送出去
                            out.close();
                            bw.close();//使用完关闭

                            //接收服务器数据

                            if (conn.getResponseCode() == 200) {
                                //字符流读取服务端返回的数据
                                InputStream in = conn.getInputStream();
                                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                                String str = null;
                                buffer = new StringBuffer();
                                while ((str = br.readLine()) != null) {
                                    buffer.append(str);
                                }

                                Looper.prepare();
                                Toast.makeText(Collect.this, buffer.toString(), Toast.LENGTH_SHORT).show();
                                Looper.loop();

                                in.close();
                                br.close();
                            }
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        };
        asyncTask.execute();
    }

}
