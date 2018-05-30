package com.example.gxw.indoorlocation;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.List;

public class PlanButton extends AppCompatActivity {
    EditText editText4;   //终点控件
    Button button6;       //规划控件
    Button button8;       //定位控件
    PathView mPathView;
    float Xcoord;         //X轴坐标
    float Ycoord;         //Y轴坐标

    float Spoint;//起点
    float Epoint;//终点

    float AP1, AP2,AP4, AP5;
    float shufang, PDCN, AP3, hhh, haku;


    StringBuffer buffer1;           //用于接收定位信息
    StringBuffer buffer;           //用于接收路径信息
    static ArrayList<Integer> list = new ArrayList<Integer>();  //使用链表存储路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_button);

        editText4=findViewById(R.id.editText4);
        button6=findViewById(R.id.button6);
        button8=findViewById(R.id.button8);
        mPathView = (PathView) findViewById(R.id.path_surfaceView);

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getsPoint(Xcoord,Ycoord);
                Epoint=Float.valueOf(editText4.getText().toString());
                waiting();
            }
        });
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
                Twaiting();
            }
        });


    }
    public void waiting()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                HttpURLConnection conn = null;
                try {
                    url = new URL("http://192.168.1.107:8080/RssiWeb/FindPathW");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.connect();
                    OutputStream out = conn.getOutputStream();
                    //创建字符流对象并用高效缓冲流包装它，便获得最高的效率
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("Spoint",Spoint);
                    jsonObject.put("Epoint",Epoint);

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

                        int pathCount=Integer.parseInt(RjsonObject.getString("总步数"));
                        for(int i=0;i<pathCount;i++)
                        {
                            list.add(Integer.parseInt(RjsonObject.getString("第"+i+"步")));
                        }
                        System.out.println(list);    //list是路径上的点

                        //更新view
                        mPathView.autoAddPoint(list);
                        list.clear();
                        mPathView.autoAddPoint(list);//清除图上的点

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
    private void Twaiting()      //用于获得定位信息
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
                                buffer1 = new StringBuffer();
                                while ((str = br.readLine()) != null) {
                                    buffer1.append(str);
                                }
                                JSONObject RjsonObject=new JSONObject(buffer1.toString()); //返回的json数据,从其中获得坐标
                                System.out.println(RjsonObject);
                                Xcoord=Float.valueOf(RjsonObject.getString("X")); //获得坐标X和Y
                                Ycoord=Float.valueOf(RjsonObject.getString("Y"));

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPathView.getCoord(Xcoord,Ycoord);//将X、Y的坐标传送的view中去
                                    }
                                }).start();

                                Looper.prepare();
                                Toast.makeText(PlanButton.this, buffer1.toString(), Toast.LENGTH_SHORT).show();
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
public void getsPoint(float Xcoord,float Ycoord)    //使用坐标的到路径规划的起点
{
    if((int)Xcoord<5)
    {
         if((int)Ycoord<3)
         {
             Spoint=0;
         }
        if((int)Ycoord<7&&(int)Ycoord>=3)
        {
            Spoint=3;
        }
        if((int)Ycoord<11&&(int)Ycoord>=7)
        {
            Spoint=6;
        }
    }

    if((int)Xcoord>=5&&(int)Xcoord<7)
    {
        if((int)Ycoord<3)
        {
            Spoint=1;
        }
        if((int)Ycoord<7&&(int)Ycoord>=3)
        {
            Spoint=4;
        }
        if((int)Ycoord<11&&(int)Ycoord>=7)
        {
            Spoint=7;
        }
    }

    if((int)Xcoord<=11&&(int)Xcoord>=7)
    {
        if((int)Ycoord<3)
        {
            Spoint=2;
        }
        if((int)Ycoord<7&&(int)Ycoord>=3)
        {
            Spoint=5;
        }
        if((int)Ycoord<11&&(int)Ycoord>=7)
        {
            Spoint=8;
        }
    }
}
}
