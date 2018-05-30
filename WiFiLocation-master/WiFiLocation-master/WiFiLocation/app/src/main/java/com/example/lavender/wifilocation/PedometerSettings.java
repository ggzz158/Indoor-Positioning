package com.example.lavender.wifilocation;

import android.content.SharedPreferences;
/**
 * 计步器参数设置类
 * @author anyang
 *
 */
public class PedometerSettings {
	
	private SharedPreferences settings;

	// 初始化SharedPreferences
	public PedometerSettings(SharedPreferences settings) {
		// TODO Auto-generated constructor stub
		this.settings = settings;
	}

	// 读取SharedPreferences中的信息，判断计步器服务是否正在运行
	public boolean isServiceRunning() {
        return settings.getBoolean("service_running", false);
    }
	
	 public void clearServiceRunning() {
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putBoolean("service_running", false);
	        editor.commit();
	    }
	 
	 public void saveServiceRunning(boolean running) {
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putBoolean("service_running", running);
	        editor.commit();
	    }
}
