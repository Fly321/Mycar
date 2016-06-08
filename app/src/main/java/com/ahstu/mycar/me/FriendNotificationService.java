package com.ahstu.mycar.me;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.ahstu.mycar.R;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;

/**
 * Created by Administrator on 2016/6/6.
 */
public class FriendNotificationService extends Service{
    
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;//notification构造器
    private Notification share_notification;
    private String ACTION_FRIEND="com.ahstu.mycar.sharelocation.action";
    
    private final static String SHARELOCATION="Sharelocation";
    private final static int BUTTON_ACCEPT=1;
    private final static int BUTTON_DENY=2;

    private ButtonBroadcastReceiver btReceiver;
    
    private RemoteViews friendremoteviews;
    
    private String name,mobileid;

    private BmobPushManager bmobPush;
    private BmobQuery<BmobInstallation> bmobInstallationBmobQueryid;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initButtonReceiver();
    }

    public void initButtonReceiver(){
        btReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FRIEND);
        registerReceiver(btReceiver, intentFilter);
    }

    
    //请求位置共享通知栏广播
    public class ButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_FRIEND)){
                int sharebutton = intent.getIntExtra(SHARELOCATION, 0);
                switch (sharebutton){
                    case BUTTON_ACCEPT:
                        acceptShare();
                        break;
                    case BUTTON_DENY:
                        denyShare();
                        break;
                }
            }
        }
    }
    //接受响应事件
    public void acceptShare(){
        notificationManager.cancel(1);
        bmobPush.pushMessage(name+"车友接受了位置共享");
//        Intent i=new Intent(FriendNotificationService.this,MusicMainActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);
//        BmobQuery<User> friendquery = new BmobQuery<User>();
    }
    //拒绝响应事件
    public void denyShare(){
        notificationManager.cancel(1);
        bmobPush.pushMessage(name+"车友拒绝了位置共享");
        
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //发送方姓名和设备id
        name=intent.getStringExtra("FRIEND_NAME");
        mobileid=intent.getStringExtra("MOBILE_ID");

        //通知栏自定义按钮点击事件
        notificationManager= (NotificationManager) super.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        RemoteViews friendremoteviews=new RemoteViews(getPackageName(), R.layout.friendshare_notification);
        friendremoteviews.setTextViewText(R.id.friend_request,name+"好友位置请求");

        //接受请求
        Intent friend_notification=new Intent(ACTION_FRIEND);
        friend_notification.putExtra(SHARELOCATION,BUTTON_ACCEPT);
        PendingIntent intent_accept=PendingIntent.getBroadcast(this,1,friend_notification,PendingIntent.FLAG_UPDATE_CURRENT);
        friendremoteviews.setOnClickPendingIntent(R.id.bt_accept_share,intent_accept);

        //拒绝请求
        friend_notification.putExtra(SHARELOCATION,BUTTON_DENY);
        PendingIntent intent_deny=PendingIntent.getBroadcast(this,2,friend_notification,PendingIntent.FLAG_UPDATE_CURRENT);
        friendremoteviews.setOnClickPendingIntent(R.id.bt_deny_share,intent_deny);

        //设置通知栏
        mBuilder.setContent(friendremoteviews).setWhen(System.currentTimeMillis()).setTicker(name+"车友请求位置共享").setSmallIcon(R.drawable.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL);
        share_notification=mBuilder.build();
        share_notification.flags=Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1,share_notification);
        //推送查询
        bmobPush = new BmobPushManager(this);
        bmobInstallationBmobQueryid = BmobInstallation.getQuery();//查询设备表
        bmobInstallationBmobQueryid.addWhereEqualTo("installationId", mobileid);
        bmobPush.setQuery(bmobInstallationBmobQueryid);
        return super.onStartCommand(intent, flags, startId);
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
