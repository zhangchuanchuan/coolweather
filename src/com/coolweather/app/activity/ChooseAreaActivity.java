package com.coolweather.app.activity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class ChooseAreaActivity extends Activity implements android.view.View.OnClickListener{
	/**
	 * 需要的常量：当前等级
	 */
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	/**
	 * 省，市列表
	 */
	private List<Province> provinceList;
	private List<City> cityList;
	
	/**
	 * 被选中的省或市
	 */
	private Province selectedProvince;
	
	private int currentLevel;
	
	
	/**
	 * 标志位，判断是否是从切换城市功能过来
	 */
	private boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity){
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		//获得控件
		Button search = (Button)findViewById(R.id.search);
		search.setOnClickListener(this);
		listView = (ListView)findViewById(R.id.list_view);
		titleText = (TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(position);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					String cityName = cityList.get(position)
												.getCityName();		
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("city_name", cityName);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}
	
	/**
	 * 查询所有的省，先从数据库查
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null, "province");
		}
	}
	
	
	private void queryFromServer(final String code, final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://webservice.webxml.com.cn/WebServices/WeatherWS.asmx/getSupportCityString"
					+"?theRegionCode="+code;
		}else{
			address = "http://webservice.webxml.com.cn/WebServices/WeatherWS.asmx/getRegionProvince";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}
				
				if(result){
					//通过runOnUiThread的方法回到主线程
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}
						}			
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	

	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city : cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	

	private void showProgressDialog() {
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在努力的加载数据...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	private void closeProgressDialog() {
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	//back按钮事件
	@Override
	public void onBackPressed() {
		 if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else{
			if(isFromWeatherActivity){
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

	private void showSearchDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("查询城市天气");
		View view = LayoutInflater.from(this).inflate(R.layout.dialog_search, null);
		final EditText search_city = (EditText)view.findViewById(R.id.search_city);
		builder.setView(view);
		builder.setPositiveButton("确定", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final String city_name=search_city.getText().toString();
				if(city_name.equals("")){
					Toast.makeText(ChooseAreaActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
					showSearchDialog();
					return;
				}
		
				try {
					String name_url = URLEncoder.encode(city_name, "utf-8");
					String address = "http://apis.baidu.com/apistore/weatherservice/cityname?cityname="+name_url;
					HttpUtil.sendRequestWeather(address, new HttpCallbackListener() {
						
						@Override
						public void onFinish(String response) {
							JSONObject jsonObject;
							try {
								jsonObject = new JSONObject(response);

							String errNum = jsonObject.getString("errNum");
								if(errNum.equals("-1")){
									runOnUiThread(new Runnable(){

										@Override
										public void run() {
											Toast.makeText(ChooseAreaActivity.this, "有这个城市吗？", Toast.LENGTH_SHORT).show();
										}});
									
								}else{
									runOnUiThread(new Runnable(){

										@Override
										public void run() {
											Intent intent = new Intent(ChooseAreaActivity.this,
													WeatherActivity.class);
											intent.putExtra("city_name", city_name);
											startActivity(intent);
											finish();
										}});
								}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						@Override
						public void onError(Exception e) {
							// TODO Auto-generated method stub
							
						}
					});

					} catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.search:
			showSearchDialog();
			break;
		default :
			break;
		}
	}
	
}
