package com.coolweather.app.util;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Province;

public class Utility {
	
	/**
	 * 解析处理返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(
			CoolWeatherDB coolWeatherDB, String response){
		
		if(!TextUtils.isEmpty(response)){
			try{
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser xmlParser = factory.newPullParser();
				
				xmlParser.setInput(new StringReader(response));
				int eventType = xmlParser.getEventType();
				String string="";
				while(eventType!=XmlPullParser.END_DOCUMENT){
					String nodeName = xmlParser.getName();
					switch(eventType){
					
					case XmlPullParser.START_TAG:{
						if("string".equals(nodeName)){
							
							string = xmlParser.nextText();
							String[] strs = string.split(",");
							Province p = new Province();
							p.setProvinceName(strs[0]);
							p.setProvinceCode(strs[1]);
							coolWeatherDB.saveProvince(p);
						}
					}
					break;
					
					case XmlPullParser.END_TAG:{
						
					}
					break;
					
					default:
						break;
					}
					eventType=xmlParser.next();
				}
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 解析处理返回的市级数据
	 */
	public synchronized static boolean handleCitiesResponse(
			CoolWeatherDB coolWeatherDB, String response, int provinceId){
		
		if(!TextUtils.isEmpty(response)){
			try{
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser xmlParser = factory.newPullParser();
				
				xmlParser.setInput(new StringReader(response));
				int eventType = xmlParser.getEventType();
				String string="";
				while(eventType!=XmlPullParser.END_DOCUMENT){
					String nodeName = xmlParser.getName();
					switch(eventType){
					
					case XmlPullParser.START_TAG:{
						if("string".equals(nodeName)){
							
							string = xmlParser.nextText();
							String[] strs = string.split(",");
							City c = new City();
							c.setCityName(strs[0]);
							c.setCityCode(strs[1]);
							c.setProvinceId(provinceId);
							coolWeatherDB.saveCity(c);
						}
					}
					break;
					
					case XmlPullParser.END_TAG:{
						
					}
					break;
					
					default:
						break;
					}
					eventType=xmlParser.next();
				}
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 解析服务器返回的JSON数据，并将解析出来的数据保存到本地
	 */
	public static void handleWeatherResponse(Context context, String response){
		try{
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("retData");
			String cityName = weatherInfo.getString("city");
			String temp1 = weatherInfo.getString("l_tmp");
			String temp2 = weatherInfo.getString("h_tmp");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("time");
			saveWeatherInfo(context, cityName, temp1, temp2, weatherDesp, publishTime);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}

	public static void saveWeatherInfo(Context context, String cityName,
			 String temp1, String temp2, String weatherDesp,
			String publishTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
		
	}
	
	
}
