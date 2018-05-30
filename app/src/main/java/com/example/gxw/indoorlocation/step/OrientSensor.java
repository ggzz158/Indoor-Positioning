package com.example.gxw.indoorlocation.step;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * 方向传感器
 */

public class OrientSensor implements SensorEventListener {
    private static final String TAG = "OrientSensor";
    private SensorManager sensorManager;
    private OrientCallBack orientCallBack;
    private Context context;
    float[] accelerometerValues = new float[3];

    float[] magneticValues = new float[3];

    public OrientSensor(Context context, OrientCallBack orientCallBack) {
        this.context = context;
        this.orientCallBack = orientCallBack;
    }

    public interface OrientCallBack {
        /**
         * 方向回调
         */
        void Orient(int orient);
    }

    /**
     * 注册加速度传感器和地磁场传感器
     * @return 是否支持方向功能
     */

    public Boolean registerOrient() {
        Boolean isAvailable = true;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        // 注册加速度传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME)) {
            Log.i(TAG, "加速度传感器可用！");
        } else {
            Log.i(TAG, "加速度传感器不可用！");
            isAvailable = false;
        }

        // 注册地磁场传感器
        if (sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME)) {
            Log.i(TAG, "地磁传感器可用！");
        } else {
            Log.i(TAG, "地磁传感器不可用！");
            isAvailable = false;
        }
        return isAvailable;
    }

    /**
     * 注销方向监听器
     */
    public void unregisterOrient() {
        sensorManager.unregisterListener(this);
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values.clone();
        }

        float[] R = new float[9];
        float[] values = new float[3];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
        SensorManager.getOrientation(R, values);
        int degree = (int) Math.toDegrees(values[0])+90;//以正西为方向
        if (degree < 0) {
            degree += 360;
        }
        orientCallBack.Orient(degree);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

