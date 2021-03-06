package com.coolweather.app.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				updateWeather();
			}
			}).start();
		AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
		int sexHours = 8*60*60*1000;
		long triggerAtTime = System.currentTimeMillis()+sexHours;
		Intent i = new Intent(this, this.getClass());
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * 更新天气信息
	 */
	private void updateWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String cityName = prefs.getString("city_name", "");
		try {
			cityName = URLEncoder.encode(cityName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String address = "http://apis.baidu.com/apistore/weatherservice/cityname?cityname="+cityName;
		HttpUtil.sendRequestWeather(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}
			
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}
		});
	}

}
