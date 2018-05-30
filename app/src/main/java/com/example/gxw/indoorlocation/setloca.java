package com.example.gxw.indoorlocation;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gxw.indoorlocation.step.OrientSensor;
import com.example.gxw.indoorlocation.step.SensorUtil;
import com.example.gxw.indoorlocation.step.StepSensorAcceleration;
import com.example.gxw.indoorlocation.step.StepSensorBase;

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

public class setloca extends AppCompatActivity implements StepSensorBase.StepCallBack, OrientSensor.OrientCallBack {

    private TextView mStepText;
    private TextView mOrientText;
    private StepView mStepView;
    private StepSensorBase mStepSensor; // 计步传感器
    private OrientSensor mOrientSensor; // 方向传感器
    private int mStepLen = 50; // 步长
    private Button button5;    //定位按钮

    StringBuffer buffer;
    float AP1, AP2,AP4, AP5;
    float shufang, PDCN, AP3, hhh, haku;
    public float Xcoord;//X轴坐标
    public float Ycoord;//X轴坐标

    @Override
    public void Step(int stepNum) {           //使用接口来定义step方法的具体实现
        //  计步回调
        mStepText.setText("步数:" + stepNum);
        mStepView.autoAddPoint(mStepLen);
    }

    @Override
    public void Orient(int orient) {         //使用接口来定义Orient方法的具体实现
        // 方向回调
        mOrientText.setText("方向:" + orient);
//        获取手机转动停止后的方向
//        orient = SensorUtil.getInstance().getRotateEndOrient(orient);
        mStepView.autoDrawArrow(orient);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SensorUtil.getInstance().printAllSensor(this); // 打印所有可用传感器 step_surfaceView
        setContentView(R.layout.setloca);

        mStepText = (TextView) findViewById(R.id.step_text);
        mOrientText = (TextView) findViewById(R.id.orient_text);
        mStepView = (StepView) findViewById(R.id.step_surfaceView);
        button5=findViewById(R.id.button5);

        // 注册计步监听
        mStepSensor = new StepSensorAcceleration(this, this);
        if (!mStepSensor.registerStep()) {
            Toast.makeText(this, "计步功能不可用！", Toast.LENGTH_SHORT).show();
        }
//        }
        // 注册方向监听
        mOrientSensor = new OrientSensor(this, this);
        if (!mOrientSensor.registerOrient()) {
            Toast.makeText(this, "方向功能不可用！", Toast.LENGTH_SHORT).show();
        }


        button5.setOnClickListener(new View.OnClickListener() {     //定位实现
            @Override
            public void onClick(View view) {
                init();
                Twaiting();
            }

        });

    }
    public void init() {                 //数据初始化
        AP1 = 0;
        AP2 = 0;
        AP4 = 0;
        AP5 = 0;
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
    private void Twaiting()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (shufang == 0 || PDCN == 0 || AP3 == 0 || hhh == 0 || haku == 0) {
                    scan();
                }
                AP1 = shufang;
                AP2 = PDCN;
                AP4 = hhh;
                AP5 = haku;
                System.out.println(AP1);
                System.out.println(AP2);
                System.out.println(AP3);
                System.out.println(AP4);
                System.out.println(AP5);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        URL url = null;
                        HttpURLConnection conn = null;
                        try {
                            url = new URL("http://192.168.1.107:8080/RssiWeb/knnmanger");
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                            conn.connect();
                            OutputStream out = conn.getOutputStream();
                            //创建字符流对象并用高效缓冲流包装它，便获得最高的效率
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

                            JSONObject jsonObject=new JSONObject();
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
                                JSONObject RjsonObject=new JSONObject(buffer.toString()); //返回的json数据,从其中获得坐标
                                System.out.println(RjsonObject);
                                Xcoord=Float.valueOf(RjsonObject.getString("X")); //获得坐标X和Y
                                Ycoord=Float.valueOf(RjsonObject.getString("Y"));

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mStepView.getCoord(Xcoord,Ycoord);//将X、Y的坐标传送的view中去
                                    }
                                }).start();
                                Looper.prepare();
                                Toast.makeText(setloca.this, buffer.toString(), Toast.LENGTH_SHORT).show();
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
        }).start();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销传感器监听
        mStepSensor.unregisterStep();
        mOrientSensor.unregisterOrient();
    }
    public float getX()       //得到X轴坐标
    {
        return Xcoord;
    }
    public float getY()       //得到Y轴坐标
    {
        return Ycoord;
    }
}

