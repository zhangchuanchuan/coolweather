package com.coolweather.app.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener{
	/**
	 * 显示的控件
	 */
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	
	private TextView wdText;
	private TextView wsText;
	private TextView srText;
	private TextView ssText;
	
	private LinearLayout bgWeather;
	/**
	 * 更新天气，切换城市
	 */
	private Button switchCity;
	private Button refresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//获得控件实例
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView)findViewById(R.id.city_name);
		publishText = (TextView)findViewById(R.id.publish_text);
		weatherDespText = (TextView)findViewById(R.id.weather_desp);
		temp1Text = (TextView)findViewById(R.id.temp1);
		temp2Text= (TextView)findViewById(R.id.temp2);
		currentDateText = (TextView)findViewById(R.id.current_data);
		wdText = (TextView)findViewById(R.id.wind_state);
		wsText = (TextView)findViewById(R.id.wind_speed);
		srText = (TextView)findViewById(R.id.sunrise);
		ssText = (TextView)findViewById(R.id.sunset);
		bgWeather = (LinearLayout)findViewById(R.id.bg_weather);
		
		switchCity = (Button)findViewById(R.id.switch_city);
		refresh = (Button)findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refresh.setOnClickListener(this);
		
		String cityName = getIntent().getStringExtra("city_name");
		if(!TextUtils.isEmpty(cityName)){
			//查询城市天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(cityName);
		}else{
			showWeather();
		}
	}

	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", "")+"℃");
		temp2Text.setText(prefs.getString("temp2", "")+"℃");
		String desp = prefs.getString("weather_desp", "");
		weatherDespText.setText(desp);
		publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		wdText.setText("风向:"+prefs.getString("wd", ""));
		wsText.setText("风速:"+prefs.getString("ws", ""));
		srText.setText("日出时间:"+prefs.getString("sunrise", ""));
		ssText.setText("日落时间:"+prefs.getString("sunset", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		if(desp.contains("云")){
			bgWeather.setBackgroundResource(R.drawable.ic_weather_cloudy_bg);
		}else if(desp.contains("雪")){
			bgWeather.setBackgroundResource(R.drawable.ic_weather_snow_bg);
		}else if(desp.contains("雨")){
			bgWeather.setBackgroundResource(R.drawable.ic_weather_rain_bg);
		}else{
			bgWeather.setBackgroundResource(R.drawable.ic_weather_sunshine_bg);
		}
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	
	private void queryWeatherInfo(String cityName) {
		try {
			cityName = URLEncoder.encode(cityName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String address = "http://apis.baidu.com/apistore/weatherservice/cityname?cityname="+cityName;
		queryFromServer(address, "cityName");
	}

	private void queryFromServer(String address, final String type) {
		HttpUtil.sendRequestWeather(address, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				if("cityName".equals(type)){
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							showWeather();
						}});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						publishText.setText("同步失败");
					}
					
				});
			}
			
		});
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String cityName = prefs.getString("city_name", "");
			Log.d("ok", "146:"+cityName);
			if(!TextUtils.isEmpty(cityName)){
				queryWeatherInfo(cityName);
			}
			break;
		default:
			break;
		}
	}
}
