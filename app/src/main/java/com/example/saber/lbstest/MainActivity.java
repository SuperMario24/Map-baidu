package com.example.saber.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BaiduMap baiduMap;

    private boolean isFirstLocation = true;

    public LocationClient locationClient;

    private TextView tvPosition;

    private String s;

    private MapView mapView;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    tvPosition.setText((String)msg.obj);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化,一定要在setContentView之前调用
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);



        tvPosition = (TextView) findViewById(R.id.tv_position);
        mapView = (MapView) findViewById(R.id.map_view);
        baiduMap = mapView.getMap();//获取BaiduMap实例
        baiduMap.setMyLocationEnabled(true);//开启随时在地图上显示位置的功能

        Log.d("info", "OnCreate--CurrentThread: "+Thread.currentThread().getId());

        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());

        List<String> permissionList = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            requestLocation();
        }

    }

    private void requestLocation(){
        initLocation();
        locationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//使用传感器模式--GPS
        locationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for (int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    /**
     * 获取定位的内部类，实现BDLocationListener
     */
    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            /**
             * 以文本形式打印出位置
             */
            StringBuilder sb = new StringBuilder();
            sb.append("纬度:").append(bdLocation.getLatitude()).append("\n");
            sb.append("经度:").append(bdLocation.getLongitude()).append("\n");
            sb.append("国家:").append(bdLocation.getCountry()).append("\n");
            sb.append("省:").append(bdLocation.getProvince()).append("\n");
            sb.append("市:").append(bdLocation.getCity()).append("\n");
            sb.append("区:").append(bdLocation.getDistrict()).append("\n");
            sb.append("街道:").append(bdLocation.getStreet()).append("\n");
            sb.append("定位方式:");
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                sb.append("GPS");
            }else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("网络");
            }
            s = sb.toString();

            //应该是在工作线程中执行的，所以发消息通知主线程更新UI
            Message msg = Message.obtain();
            msg.what =1;
            msg.obj = s;
            handler.sendMessage(msg);

            Log.d("info", "LocationPosition: "+s);
            Log.d("info", "CurrentThread: "+Thread.currentThread().getId());


            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                //定位到我的位置
                navigateTo(bdLocation);
            }

        }
        @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }
    }

    /**
     * 定位到我的位置
     * @param bdLocation
     */
    private void navigateTo(BDLocation bdLocation) {
        if(isFirstLocation){
            LatLng latlng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());//定位到经纬度上的类
            Log.i("info", "LocationPosition: "+bdLocation.getLatitude()+","+bdLocation.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(latlng,16f);//缩放到经纬度的类---缩放到的级别，越大越清晰（3-19）之间
            baiduMap.animateMapStatus(update);
            isFirstLocation = false;
        }

        //MyLocationData.Builder 获取经纬度
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(bdLocation.getLatitude());
        locationBuilder.longitude(bdLocation.getLongitude());
        //build方法生成MyLocationData的实例
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
