package com.inone.baidutest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
    public LocationClient mLocationClient;
    private TextView positionText;

    //地图显示
    private MapView mapView;

    //实现位置的移动
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建mLocationClient实例，调用getApplicationContext()方法获取一个全局的Context参数并传入
        mLocationClient = new LocationClient(getApplicationContext());
        //注册一个定位监听器，当获取信息是会回调这个监听器
        mLocationClient.registerLocationListener(new MyLocationListener());

        //初始化操作，初始化工具会自动获取布局文件中的MapView进行填充，之后才能加载正常的地图
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);


        //地图视图
        mapView = (MapView) findViewById(R.id.bmapView);

        //获取BaiduMap的实例
         baiduMap = mapView.getMap();
         baiduMap.setMyLocationEnabled(true);//允许自己位置显示功能开启


        //文本框
        positionText = (TextView) findViewById(R.id.position_text_view);

        //检查权限是否被授予
        List<String> permissionList = new ArrayList<>();//创建一个空的List集合装没有授予的权限
        //检查权限是否授予
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);//GPS定位权限
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE) !=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);//读取手机当前状态
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);//程序写入外部存储
        }
        if(!permissionList.isEmpty()){//权限为空
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            requestLocation();
        }
    }


    private void  navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());//存放经纬度
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);//地图移动到指定的经纬度
            update = MapStatusUpdateFactory.zoomTo(16f);//缩放级别
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }

        //显示自己位置
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
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

    //开始地理位置定位
    private void requestLocation(){
        mLocationClient.start();
        initLocation();
    }
    //实时更新自己位置
    private void initLocation(){
        LocationClientOption   option  = new LocationClientOption();
        option.setScanSpan(5000); //更新时间间隔，5秒
       // option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//强制使用GPS定位
        option.setIsNeedAddress(true);//需要获取当前位置详细信息
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result !=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能运行",Toast.LENGTH_SHORT).show();
                            finish();//关闭当前程序
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    //定位监听器
    public  class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation ||bdLocation.getLocType() ==BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
//                    currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
//                    currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
//                    currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
//                    currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
//                    currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
//                    currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
//                    currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
                    currentPosition.append("地址：").append(bdLocation.getAddrStr()).append("\n");
                    currentPosition.append("定位方式：");
                    if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");
                    }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络");
                    }

                    positionText.setText(currentPosition);

                }
            });
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
