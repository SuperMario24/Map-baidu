# Map-baidu

1.菜单配置文件
<meta-data
   android:name="com.baidu.lbsapi.API_KEY"
   android:value="0q6so4GqNpcjLCsWmHjsge4YOqGIHmqw"
    />
    
 <service
    android:name="com.baidu.location.f"
    android:enabled="true"
    android:process=".remote">
 </service>


2.集合转数组
String[] permissions = permissionList.toArray(new String[permissionList.size()]);

3.自定义类实现DBLocationListener接口，在onReceiveLocation方法里可获取当前经纬度。
4.用LocationClient注册自定义类。
5.自定义类里的方法貌似在工作线程中执行的。

6.LocationClientOption对象，setScanSpan()------设置更新的间隔。最后调用locationClient.setLocOption(option);


7.活动被销毁时，调用LocationClient的stop方法停止定位。

8.LocationClientOption.setIsNeedAddress(true);----获取当前位置的详细信息

9.SDKInitializer.initialize(getApplicationContext);-------初始化一定要在setContentView之前调用


10.MapStatusUpdate---------缩放定位的类
 LatLng latlng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());//定位到经纬度上的类
 MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(latlng,16f);//缩放到经纬度的类---缩放到的级别，越大越清晰（3-19）之间
 baiduMap.animateMapStatus(update);
 
 
 11.显示我的位置-------MyLocationData.Builder(封装当前位置)  MyLocationData(将位置显示出来)
 //MyLocationData.Builder 获取经纬度
 MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
 locationBuilder.latitude(bdLocation.getLatitude());
 locationBuilder.longitude(bdLocation.getLongitude());
//build方法生成MyLocationData的实例
 MyLocationData locationData = locationBuilder.build();
 baiduMap.setMyLocationData(locationData);
 
 
 
 
 
 
 
