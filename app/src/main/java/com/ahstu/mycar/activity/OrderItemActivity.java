package com.ahstu.mycar.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahstu.mycar.R;
import com.ahstu.mycar.sql.DatabaseHelper;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import c.b.BP;
import c.b.PListener;

/**
 * Created by xuning on 2016/5/23.
 * 功能：订单子项目详情
 */
public class OrderItemActivity extends Activity {
    TextView info_station;
    TextView info_username;
    TextView info_carnumber;
    TextView info_ctype;
    TextView info_gasprice;
    TextView info_gascount;
    TextView info_countprice;
    TextView info_time;
    ImageView erweima;
    ImageView meorder_back;
    Bundle bundle;
    private Button paid_btn;
    private String station_name, station_style;
    private Double total_price;
    private int PLUGINVERSION = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myorder_item);
        BP.init(this, "ccd46e34cec57d61dbcedaa08f722296");

        inview();
        set();
    }

    void inview() {
        info_station = (TextView) findViewById(R.id.info_station);
        info_carnumber = (TextView) findViewById(R.id.info_carnumber);
        info_username = (TextView) findViewById(R.id.info_username);
        info_ctype = (TextView) findViewById(R.id.info_ctype);
        info_gasprice = (TextView) findViewById(R.id.info_gasprice);
        info_gascount = (TextView) findViewById(R.id.info_gascount);
        info_countprice = (TextView) findViewById(R.id.info_countprice);
        info_time = (TextView) findViewById(R.id.info_time);
        erweima = (ImageView) findViewById(R.id.info_erweima);
        meorder_back = (ImageView) findViewById(R.id.meorderback);
        paid_btn = (Button) findViewById(R.id.not_paid_btn);
    }

    void set() {
        DatabaseHelper helper = new DatabaseHelper(OrderItemActivity.this, "node.db", null, 1);
        SQLiteDatabase data = helper.getReadableDatabase();
        bundle = getIntent().getExtras();
        int id = bundle.getInt("id");
        Log.e("OrderItemActivity", "aaaaaaaaaaaaaaaaaaaa" + id);
        Cursor cursor = data.query("gasorder", new String[]{"username", "carnumber", "stationname", "ctype", "gascount", "gasprice", "countprice", "time"}, "id=?", new String[]{id + ""}, null, null, null);
        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndex("username"));

            String carnumber = cursor.getString(cursor.getColumnIndex("carnumber")).toString();
            String staion = cursor.getString(cursor.getColumnIndex("stationname")).toString();
            String ctype = cursor.getString(cursor.getColumnIndex("ctype")).toString();
            Double gascount = cursor.getDouble(cursor.getColumnIndex("gascount"));
            Double gasprice = cursor.getDouble(cursor.getColumnIndex("gasprice"));
            String countprice = cursor.getString(cursor.getColumnIndex("countprice"));
            String time = cursor.getString(cursor.getColumnIndex("time")).toString();

            String str = countprice.replace(",", "");
            total_price = Double.parseDouble(str.substring(1, str.length()));
            station_name = staion;
            station_style = ctype;

            info_station.setText(staion);
            info_carnumber.setText(carnumber);
            info_username.setText(username);
            info_ctype.setText(ctype);
            info_gascount.setText(String.valueOf(gascount) + "升");
            info_gasprice.setText(String.valueOf(gasprice) + "元/升");
            info_countprice.setText(countprice);
            info_time.setText(time);
            JSONObject json = new JSONObject();
            try {
                json.put("加油站", staion);
                json.put("用户姓名", username);
                json.put("车牌号码", carnumber);
                json.put("加油类型", ctype);
                json.put("加油单价", gasprice);
                json.put("加油数量", gascount);
                json.put("加油总价", countprice);
                json.put("加油时间", time);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String conent = json.toString();
            Bitmap bitmap = EncodingUtils.createQRCode(conent, 400, 400, null);
            erweima.setImageBitmap(bitmap);
        }
        meorder_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        paid_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pluginVersion = BP.getPluginVersion();
                if (pluginVersion < PLUGINVERSION) {// 为0说明未安装支付插件, 否则就是支付插件的版本低于官方最新版
                    Toast.makeText(
                            OrderItemActivity.this,
                            pluginVersion == 0 ? "监测到本机尚未安装支付插件,无法进行支付,请先安装插件(无流量消耗)"
                                    : "监测到本机的支付插件不是最新版,最好进行更新,请先更新插件(无流量消耗)", 0).show();
                    installBmobPayPlugin("bp.db");
                }
                //订单支付
                BP.pay("加油站订单支付", station_name + " " + station_style, total_price, true
                        , new PListener() {

                            @Override
                            public void unknow() {
                                // 因为网络等问题,不能确认是否支付成功,请稍后手动查询(小概率事件)
                                Toast.makeText(OrderItemActivity.this, "因为网络等问题,不能确认是否支付成功,请稍后手动查询", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void succeed() {
                                // TODO Auto-generated method stub
                                Toast.makeText(OrderItemActivity.this, "订单支付成功", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void orderId(String orderid) {
                                // TODO Auto-generated method stub
                                Toast.makeText(OrderItemActivity.this, "订单号:" + orderid, Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void fail(int arg0, String reason) {
                                // TODO Auto-generated method stub
                                Toast.makeText(OrderItemActivity.this, "交易失败:" + reason, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });

    }


    //安装支付安全插件
    void installBmobPayPlugin(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + fileName + ".apk");
            if (file.exists())
                file.delete();
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse("file://" + file),
                    "application/vnd.android.package-archive");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
