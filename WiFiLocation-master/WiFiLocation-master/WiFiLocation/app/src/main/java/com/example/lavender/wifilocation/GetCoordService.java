package com.example.lavender.wifilocation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*这个服务用来获取传感器数据并计算位置坐标*/
public class GetCoordService extends Service implements SensorEventListener {
    // 测试标签
    private static final String TAG = "GetCoordService";
    // 窗口大小
    private static final int WINDOWSIZE = 24;
    // 正常人步长 50cm左右，这里定值用于测试
    private static final int STEP = 55;
    // 初始坐标  上一点坐标  当前坐标  单位cm
    private int[] coordInit = {0, 0, 0};
    private int[] coordThis = {0, 0, 0};
    private int[] coordPre = {0, 0, 0};
    // 步数统计结果显示
    private StepDisplayer stepDisplayer;
    // 传感器管理类
    private SensorManager sensorManager;
    // 传感器类
    private Sensor accSensor;
    private Sensor magnSensor;
    // 定时器
    private Timer timer = null;
    // 步数变量
    private int steps;
    // 存储加速度传感器数据数组
    private float[] acc50 = new float[3];
    private float[] acc20 = new float[3];
    private List<Double> accList = null;
    // 存储方向传感器数据数组
    private float[] ori50 = new float[3];
    // 存储重力传感器数据数组
    private float[] magn50 = new float[3];
    // 记录起始时间和结束时间
    private long start = 0, end = 0;
    private PedometerSettings pedometerSettings;
    // 采样数据列表
    private List<Double> gvSample = new ArrayList<>();

    @Override
    public void onCreate() {
        Toast.makeText(this, "开始采集", Toast.LENGTH_SHORT).show();
        //  注册检测器
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerDetector();
        stepDisplayer = new StepDisplayer(pedometerSettings);
        stepDisplayer.setSteps(0);
        stepDisplayer.addListener(stepListener);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // 停止检测步数
        unregisterDetector();
        Toast.makeText(this, "采集完毕", Toast.LENGTH_SHORT).show();
        //sendStepCount();
        super.onDestroy();
    }

    // 返回当前步数
    private void sendStepCount() {
        Intent intent = new Intent();
        intent.setAction("step");
        intent.putExtra("stepCount", stepDisplayer.getmCount());
        sendBroadcast(intent);
    }

    public GetCoordService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // 声明一个回调接口
    public interface ICallback {
        public void stepsChanged(int value);
    }

    // 初始化接口变量
    private ICallback mCallback;

    // 自定义事件
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    // 从PaceNotifier传递步数到activity（回调方法）
    private StepDisplayer.Listener stepListener = new StepDisplayer.Listener() {
        public void stepsChanged(int value) {
            steps = value;
            if (mCallback != null) {
                mCallback.stepsChanged(steps);
            }
        }
    };

    private void registerDetector() {
        // 获取指定类型的传感器对象（加速度传感器）
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        // 将传感器对象和传感器操作类绑定
        // 将Sensor实例与SensorEventListener实例相互绑定，20000microseconds采样一次,即50HZ。
        sensorManager.registerListener(this, accSensor, 20000);
        sensorManager.registerListener(this, magnSensor, 20000);
        // 初始化list，存放加速度数据，之后用于求动态阈值
        accList = new ArrayList<Double>();

        // 重新启动一个新的线程，以20HZ的频率读取传感器数据，并对这些数据进行处理
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                synchronized (this) {
                    for (int i = 0; i < acc20.length; i++) {
                        acc20[i] = acc50[i];
                    }
                }
                // 计算三轴加速度数据的平方值
                double ax = Math.pow(acc20[0], 2);
                double ay = Math.pow(acc20[1], 2);
                double az = Math.pow(acc20[2], 2);
                // 计算合成加速度
                double gv = Math.sqrt(ax + ay + az);
                // 数据采样
//            	sample(gv);
                /*
            	 *  计算各组数据的标准差及相关系数
            	 *  WINDOWSIZE = 24
            	 */
                if (accList.size() < WINDOWSIZE) {
                    accList.add(gv);
                } else {
                    double[] standardDeviation = new double[8];
                    double[] correlation = new double[8];
                    // 循环7组计算标准差及相关系数
                    for (int i = 1; i < 8; i++) {
                        // aArray,bArray赋值
                        double[] aArray = new double[i + 5];
                        double[] bArray = new double[i + 5];
                        for (int j = 0; j < aArray.length; j++) {
                            aArray[j] = accList.get(j);
                            bArray[j] = accList.get(j + 7);
                        }
                        // 计算bArray的标准差σb
                        standardDeviation[i] = standardDeviation(bArray);
                        // 计算aArray与bArray的相关系数ρab
                        correlation[i] = correlation(aArray, bArray);
                    }
                    // 求出7个ρab的最大值MAX及对应该次的σb和数组bArray的长度bLength
                    int tag = maxTag(correlation);
                    Log.d(TAG, "standardDeviation[tag]:" + standardDeviation[tag]);
                    Log.d(TAG, "correlation[tag]:" + correlation[tag]);
                    // 判断是否发生一步
                    if ((standardDeviation[tag] > 0.5) /*&& (correlation[tag] > 0.7)*/) {
                        // 获取系统当前时间（毫秒）
                        end = System.currentTimeMillis();
                        // 因为人们的反应速度最快为0.2s，因此当发生动作的时间间隔小与0.2s时，则认为是外界干扰
                        if (end - start > 500) {
                            Log.d(TAG, "step");
                            // 发送通知，步数加一
                            stepDisplayer.onStep();
                            sendMsgAddSetp();
                            start = end;
                        }
                    }
                    // 删除accList的前bLength(tag+5)项，剩余项依次平移至数组前端
                    accList = removeElements(accList, (tag + 5));

                }
            }
        }, 1000, 50);

    }

    // 计算标准差
    private double standardDeviation(double[] array) {
        double result = 0;
        double sumb = 0;

        for (int m = 0; m < array.length; m++) {
            sumb += array[m];
        }

        double avgb = sumb / array.length;
        sumb = 0;
        for (int n = 0; n < array.length; n++) {
            sumb += Math.pow((array[n] - avgb), 2);
        }
        result = Math.sqrt(sumb / array.length);

        return result;
    }

    // 计算相关系数
    private double correlation(double[] aArray, double[] bArray) {
        double result = 0;
        double suma = 0;
        double sumb = 0;
        double avga = 0;
        double avgb = 0;
        double sumUp = 0;
        double sumDown1 = 0;
        double sumDown2 = 0;

        for (int m = 0; m < aArray.length; m++) {
            suma += aArray[m];
            sumb += bArray[m];
        }
        avga = suma / aArray.length;
        avgb = sumb / bArray.length;

        for (int n = 0; n < aArray.length; n++) {
            sumUp += (aArray[n] - avga) * (bArray[n] - avgb);
            sumDown1 += Math.pow((aArray[n] - avga), 2);
            sumDown2 += Math.pow((bArray[n] - avgb), 2);
        }

        result = sumUp / Math.sqrt(sumDown1 * sumDown2);

        return result;
    }

    // 返回数组中最大值的下标
    private int maxTag(double[] correlation) {
        int tag = 0;
        correlation[0] = 0;
        for (int m = 1; m < correlation.length; m++) {
            if (correlation[m] > correlation[m - 1]) {
                tag = m;
            }
        }
        return tag;
    }

    // 移除前number项元素
    private List<Double> removeElements(List<Double> list, int number) {
        List<Double> result = new ArrayList<Double>();

        for (int m = number; m < list.size(); m++) {
            result.add(list.get(m));
        }
        list = null;
        return result;

    }


    // 获取传感器数据信息并进行处理，判断是否是有效步数
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

        // 获取传感器对象
        Sensor sensor = event.sensor;
//        Log.d(TAG, "sensor.getType():" + sensor.getType());

        synchronized (this) {
            // 判断传感器类型
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            	Log.d(TAG, "TYPE_ACCELEROMETER");
                // 加速度传感器
                acc50 = event.values.clone();
            } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magn50 = event.values.clone();
            }
            float[] R = new float[9];
            SensorManager.getRotationMatrix(R, null, acc50, magn50);
            SensorManager.getOrientation(R, ori50);
            ori50[0] = (float) Math.toDegrees(ori50[0]);
            Log.d("MainActivity", "value[0] is " + Math.toDegrees(ori50[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // 注销步数检测服务
    private void unregisterDetector() {
        sensorManager.unregisterListener(this);
        // 停止定时器
        if (timer != null) {
            timer.cancel();
        }
    }

    // 步数加一
    private void sendMsgAddSetp() {
        // 前进一步的举例
        Intent intent = new Intent();
        intent.setAction("sensorData");
        intent.putExtra("degree0", ori50[0]);
        intent.putExtra("step", stepDisplayer.getmCount());
        sendBroadcast(intent);
    }
}
