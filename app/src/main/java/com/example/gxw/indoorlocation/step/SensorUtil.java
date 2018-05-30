package com.example.gxw.indoorlocation.step;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;
import java.util.Stack;

/**
 * 传感器工具类
 */
public class SensorUtil {

    private static final String TAG = "SensorUtil";
    private static final SensorUtil sensorUtil = new SensorUtil(); // 单例常量
    private SensorManager sensorManager;

    private static final int SENSE = 10;        // 方向差值灵敏度
    private static final int STOP_COUNT = 6;    // 停止次数
    private int initialOrient = -1;             // 初始方向
    private int endOrient = -1;                 // 转动停止方向

    private boolean isRotating = false;         // 是否正在转动
    private int lastDOrient = 0;                // 上次方向与初始方向差值
    private Stack<Integer> dOrientStack = new Stack<>(); // 历史方向与初始方向的差值栈

    private SensorUtil() {
    }

    /**
     * 单例获取
     */
    public static SensorUtil getInstance() {
        return sensorUtil;
    }

    /**
     * 获取传感器管理类的实例
     */
    public SensorManager getSensorManager(Context context) {
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        return sensorManager;
    }

    /**
     * 打印所有可用传感器
     */
    public void printAllSensor(Context context) {
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            Log.d(TAG, "所有可用传感器----: " + sensor.getName());
        }
    }

    /**
     * 获取手机转动停止的方向
     * @param orient 手机实时方向
     */

    public int getRotateEndOrient(int orient) {
        if (initialOrient == -1) {
            // 初始化转动
            endOrient = initialOrient = orient;
            Log.i(TAG, "getRotateEndOrient: 初始化，方向：" + initialOrient);
        }

        int currentDOrient = Math.abs(orient - initialOrient); // 当前方向与初始方向差值
        if (!isRotating) {
            // 检测是否开始转动
            lastDOrient = currentDOrient;
            if (lastDOrient >= SENSE) {
                // 开始转动
                isRotating = true;
            }
        } else {
            // 检测是否停止转动
            if (currentDOrient <= lastDOrient) {
                // 至少累计STOP_COUNT次出现当前方向差小于上次方向差
                int size = dOrientStack.size();
                if (size >= STOP_COUNT) {
                    // 只有以前SENSE次方向差距与当前差距的差值都小于等于SENSE，才判断为停止
                    for (int i = 0; i < size; i++) {
                        if (Math.abs(currentDOrient - dOrientStack.pop()) >= SENSE) {
                            isRotating = true;
                            break;
                        }
                        isRotating = false;
                    }
                }

                if (!isRotating) {
                    // 停止转动
                    dOrientStack.clear();
                    initialOrient = -1;
                    endOrient = orient;
                    Log.i(TAG, "getRotateEndOrient: ------停止转动，方向：" + endOrient);
                } else {
                    // 正在转动，把当前方向与初始方向差值入栈
                    dOrientStack.push(currentDOrient);
                    Log.i(TAG, "getRotateEndOrient: 正在转动，方向：" + orient);
                }
            } else {
                lastDOrient = currentDOrient;
            }
        }
        return endOrient;
    }
}