package com.ahstu.mycar.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.ahstu.mycar.R;
import com.ahstu.mycar.bean.User;
import com.ahstu.mycar.me.ShareLocationMessage;
import com.ahstu.mycar.ui.MyOrientationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;

/**
 * @author 吴天洛 2016/4/25
 *         功能:地图定位。导航
 */

public class MapFragment extends Fragment implements OnClickListener, AppCompatCallback,
        TaskStackBuilder.SupportParentable, ActionBarDrawerToggle.DelegateProvider {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Button btn_map_normal;
    private Button btn_map_site;
    private Button btn_map_mode_normal;
    private Button btn_map_mode_following;
    private Button btn_map_mode_compass;
    private Button btn_map_menu;
    private ImageView iv_map_traffic;
    private ImageView iv_myLocation;

    //定位相关变量
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private boolean isFirstIn = true;
    private double mLatitude;
    private double mLongitude;
    private LocationMode mLocationMode;

    //自定义定位图标
    private BitmapDescriptor mbitmapDescriptor;
    private MyOrientationListener mMyOrientationListener;
    private float mCurrentX;

    //地图菜单按钮动画
    private int[] res = {R.id.btn_map_menu, R.id.btn_map_normal, R.id.btn_map_site,
            R.id.btn_map_mode_normal, R.id.btn_map_mode_following, R.id.btn_map_mode_compass};
    private List<Button> ButtonList = new ArrayList<Button>();
    private boolean flag = true;
    //共享状态中介
    private ShareLocationMessage shareLocationMessage=new ShareLocationMessage();
    private Thread querylocationthread;
    private BmobQuery<User> userLocationBmobQuery;

    //获取地图按钮伸缩状态
    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMapMenu();
        ininView();
        initClick();
        initLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);  //java.io.IOException: open failed: EACCES (Permission denied)
        querylocationthread=new Thread(){
            public void run() {
                while(true){
                    try {
                        sleep(2000);
                        Log.e("dsad","dsadsadsad");
//                        userLocationBmobQuery = new BmobQuery<User>();
//                        userLocationBmobQuery.addWhereEqualTo("username",shareLocationMessage.getUsername());
//                        userLocationBmobQuery.findObjects(getActivity(), new FindListener<User>() {
//                            @Override
//                            public void onSuccess(List<User> list) {
//                                if(list!=null){
//                                    for(User userlist:list){
//                                        Log.i("location",""+userlist.getmLat()+"   "+userlist.getmLon());
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onError(int i, String s) {
//
//                            }
//                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        return view;
    }
    

    //将按钮放入动画按钮数组
    private void initMapMenu() {
        for (int i = 0; i < res.length; i++) {
            Button map_button = (Button) getActivity().findViewById(res[i]);
            ButtonList.add(map_button);
        }
    }

    private void initClick() {
        btn_map_menu.setOnClickListener(this);
        btn_map_normal.setOnClickListener(this);
        btn_map_site.setOnClickListener(this);
        btn_map_mode_normal.setOnClickListener(this);
        btn_map_mode_following.setOnClickListener(this);
        btn_map_mode_compass.setOnClickListener(this);
        iv_map_traffic.setOnClickListener(this);
        iv_myLocation.setOnClickListener(this);
    }

    private void ininView() {

        //地图
        mMapView = (MapView) getActivity().findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);  //地图比例初始化为500M
        mBaiduMap.setMapStatus(msu);
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                iv_myLocation.setImageResource(R.mipmap.location);
            }
        });
        
        btn_map_menu = (Button) getActivity().findViewById(R.id.btn_map_menu);
        btn_map_normal = (Button) getActivity().findViewById(R.id.btn_map_normal);
        btn_map_site = (Button) getActivity().findViewById(R.id.btn_map_site);
        btn_map_mode_normal = (Button) getActivity().findViewById(R.id.btn_map_mode_normal);
        btn_map_mode_following = (Button) getActivity().findViewById(R.id.btn_map_mode_following);
        btn_map_mode_compass = (Button) getActivity().findViewById(R.id.btn_map_mode_compass);
        iv_map_traffic = (ImageView) getActivity().findViewById(R.id.iv_map_traffic);
        iv_myLocation = (ImageView) getActivity().findViewById(R.id.iv_loc);
    }

    //定位初始化
    private void initLocation() {
        mLocationClient = new LocationClient(getActivity());
        myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        LocationClientOption option = new LocationClientOption();

        // 返回国测局经纬度坐标系：gcj02 返回百度墨卡托坐标系 ：bd09
        // 返回百度经纬度坐标系 ：bd09ll
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true); // 设置是否需要地址信息，默认为无地址
        option.setOpenGps(true);
        option.setScanSpan(1000);// 设置扫描间隔，单位毫秒，当<1000(1s)时，定时定位无效
        mLocationClient.setLocOption(option);//将上面option中的设置加载

        //初始化方向指示图标
//        mbitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map_my_location_icon);  //自定义方向图标
        mMyOrientationListener = new MyOrientationListener(getActivity());
        mMyOrientationListener.setmOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });

        //默认地图模式
        mLocationMode = LocationMode.NORMAL;
    }
    
    //软件开启时判断地图是否打开，没有打开，则打开
    @Override
    public void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        //开启方向传感器
        mMyOrientationListener.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    //退出程序时关闭地图
    @Override
    public void onStop() {
        super.onStop();
//        qlthread.interrupt();
        //停止地图定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();

        //停止方向传感器
        mMyOrientationListener.stop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(shareLocationMessage.isShareconnect()&&shareLocationMessage.isFirstconnect()){
            shareLocationMessage.setFirstconnect(false);
            querylocationthread.start();
        }
    }

    
    //按钮动画点击监听事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_map_menu:
                bt_animation();
                break;
            case R.id.btn_map_normal:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                bt_animation();
                break;
            case R.id.btn_map_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                bt_animation();
                break;
            case R.id.iv_map_traffic:
                if (mBaiduMap.isTrafficEnabled()) {
                    mBaiduMap.setTrafficEnabled(false);
                    iv_map_traffic.setImageResource(R.drawable.main_icon_roadcondition_off);
                } else {
                    mBaiduMap.setTrafficEnabled(true);
                    iv_map_traffic.setImageResource(R.drawable.main_icon_roadcondition_on);
                }
                bt_animation();
                break;
            case R.id.btn_map_mode_normal:
                mLocationMode = LocationMode.NORMAL;
                bt_animation();
                break;
            case R.id.btn_map_mode_following:
                mLocationMode = LocationMode.FOLLOWING;
                bt_animation();
                break;
            case R.id.btn_map_mode_compass:
                mLocationMode = LocationMode.COMPASS;
                bt_animation();
                break;

            case R.id.iv_loc:
                LatLng latLng = new LatLng(mLatitude, mLongitude);

                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(msu);
                bt_animation();
                iv_myLocation.setImageResource(R.mipmap.location_center);
                break;
        }
    }

    //按钮伸缩动画判断
    private void bt_animation() {
        if (flag) { 
            startAnim();
        } else {
            closeAnmi();
        }
    }

    //菜单的回收
    public void closeAnmi() {
        for (int i = 1; i < res.length; i++) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(ButtonList.get(i), "translationY", i * 100, 0F);
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(ButtonList.get(i), "rotation", 0, 360F);
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(animator, animator2);
            animator.setDuration(300);
            animator.setStartDelay(i * 100);
            animator.start();
            animator.start();
            animator2.setDuration(300);
            animator2.setStartDelay(i * 100);
            animator2.start();
            flag = true;
        }
    }

    private void startAnim() {
        for (int i = 1; i < res.length; i++) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(ButtonList.get(i), "translationY", 0F, i * 100);
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(animator);
            animator.setDuration(300);
            animator.setStartDelay(i * 100);
            animator.start();
            flag = false;
        }
    }
    //菜单的弹出

    //实现动画接口的抽象方法
    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Nullable
    @Override
    public ActionBarDrawerToggle.Delegate getDrawerToggleDelegate() {
        return null;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        return null;
    }

    //地图定位加载是耗时的，因此采用异步加载
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            MyLocationData data = new MyLocationData.Builder()//
                    .direction(mCurrentX)//
                    .accuracy(location.getRadius())//
                    .latitude(location.getLatitude())//
                    .longitude(location.getLongitude())//
                    .build();

            mBaiduMap.setMyLocationData(data);
            MyLocationConfiguration config = new MyLocationConfiguration(mLocationMode, true, mbitmapDescriptor);
            mBaiduMap.setMyLocationConfigeration(config);
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();

            if (isFirstIn) {
                LatLng latLng = new LatLng(mLatitude, mLongitude);
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(msu);
                isFirstIn = false;
            }
        }
    }
   
}