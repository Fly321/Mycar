package com.ahstu.mycar.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahstu.mycar.R;
import com.ahstu.mycar.bean.User;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;

/**
 * @author 吴天洛 2016/4/25
 *         功能:手机号码注册
 */
public class RegisterPhoneActivity extends Activity implements View.OnClickListener {

    private ImageView register_title_back;
    private EditText et_phoneNum;
    private EditText et_register_code;
    private Button btn_register_code;
    private TextView tv_webview;
    private Button btn_next;
    private Context context;
    private TimeCount time;
    private CheckBox mCheckBox;
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (et_phoneNum.getText().toString().isEmpty() || et_register_code.getText().toString().isEmpty() || (!mCheckBox.isChecked())) {
                btn_next.setEnabled(false);
                btn_next.setBackgroundResource(R.drawable.login_button_unchecked);
            } else {
                btn_next.setEnabled(true);
                btn_next.setBackgroundResource(R.drawable.login_button);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (et_phoneNum.getText().toString().isEmpty() || et_register_code.getText().toString().isEmpty() || (!mCheckBox.isChecked())) {
                btn_next.setEnabled(false);
                btn_next.setBackgroundResource(R.drawable.login_button_unchecked);
            } else {
                btn_next.setEnabled(true);
                btn_next.setBackgroundResource(R.drawable.login_button);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (et_phoneNum.getText().toString().isEmpty() || et_register_code.getText().toString().isEmpty() || (!mCheckBox.isChecked())) {
                btn_next.setEnabled(false);
                btn_next.setBackgroundResource(R.drawable.login_button_unchecked);
            } else {
                btn_next.setEnabled(true);
                btn_next.setBackgroundResource(R.drawable.login_button);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);
        initView();
        initClick();
        this.context = this;
        if (mCheckBox.isChecked()) {
            btn_next.setEnabled(true);
        } else {
            btn_next.setEnabled(false);
        }
    }

    private void initClick() {
        register_title_back.setOnClickListener(this);
        btn_register_code.setOnClickListener(this);
        mCheckBox.setOnClickListener(this);
        tv_webview.setOnClickListener(this);
        btn_next.setOnClickListener(this);
    }

    private void initView() {
        register_title_back = (ImageView) findViewById(R.id.register_title_back);
        et_phoneNum = (EditText) findViewById(R.id.et_phoneNum);
        et_phoneNum.addTextChangedListener(mTextWatcher);
        et_register_code = (EditText) findViewById(R.id.et_register_code);
        et_register_code.addTextChangedListener(mTextWatcher);
        btn_register_code = (Button) findViewById(R.id.btn_register_code);
        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        tv_webview = (TextView) findViewById(R.id.tv_webview);
        btn_next = (Button) findViewById(R.id.btn_next);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.register_title_back:
                finish();
                break;

            case R.id.btn_register_code:
                if (et_phoneNum.getText().toString().isEmpty()) {
                    Toast.makeText(context, "请输入手机号", Toast.LENGTH_SHORT).show();
                } else if (checkPhoneNum(et_phoneNum.getText().toString())) {
                    //判断手机号是否注册
                    BmobQuery<User> query = new BmobQuery<User>();
                    query.addWhereEqualTo("mobilePhoneNumber", et_phoneNum.getText().toString());
                    query.findObjects(context, new FindListener<User>() {
                        @Override
                        public void onSuccess(List<User> object) {
                            //手机号没注册则发送验证码
                            if (object.isEmpty()) {
                                //请求发送验证码
                                BmobSMS.requestSMSCode(context, et_phoneNum.getText().toString(), "requestSMS", new RequestSMSCodeListener() {
                                    @Override
                                    public void done(Integer integer, BmobException e) {
                                        if (e == null) {
                                            Toast.makeText(context, "验证码已发送", Toast.LENGTH_SHORT).show();

                                            //更改获取验证码的button
                                            time = new TimeCount(60000, 1000);
                                            time.start();

                                        } else {
                                            Toast.makeText(context, "验证码发送失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(RegisterPhoneActivity.this, "手机号已注册", Toast.LENGTH_SHORT).show();
                                et_phoneNum.setText("");
                            }
                        }

                        @Override
                        public void onError(int code, String msg) {
                            Toast.makeText(context, "请检查您的网络", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "手机号格式不正确", Toast.LENGTH_SHORT).show();
                    et_phoneNum.setText("");
                }
                break;
            case R.id.checkbox:
                if (et_phoneNum.getText().toString().isEmpty() || et_register_code.getText().toString().isEmpty() || (!mCheckBox.isChecked())) {
                    btn_next.setEnabled(false);
                    btn_next.setBackgroundResource(R.drawable.login_button_unchecked);
                } else {
                    btn_next.setEnabled(true);
                    btn_next.setBackgroundResource(R.drawable.login_button);
                }
                break;
            case R.id.tv_webview:
                startActivity(new Intent(this, RegisterLawsActivity.class));
                break;
                
            case R.id.btn_next:
                if (et_phoneNum.getText().toString().isEmpty()) {
                    Toast.makeText(context, "请输入手机号", Toast.LENGTH_SHORT).show();
                } else if (!checkPhoneNum(et_phoneNum.getText().toString())) {
                    Toast.makeText(context, "手机号格式不正确", Toast.LENGTH_SHORT).show();
                    et_phoneNum.setText("");
                } else if (et_register_code.getText().toString().isEmpty()) {
                    Toast.makeText(context, "请输入验证码", Toast.LENGTH_SHORT).show();
                } else {
                    //验证验证码
                    BmobSMS.verifySmsCode(context, et_phoneNum.getText().toString(), et_register_code.getText().toString(), new VerifySMSCodeListener() {
                        @Override
                        public void done(BmobException e) {

                            if (e == null) {
                                Intent i = new Intent(RegisterPhoneActivity.this, RegisterUserMsgActivity.class);
                                i.putExtra("phoneNum", et_phoneNum.getText().toString());
                                startActivity(i);

                            } else {
                                Toast.makeText(context, "验证失败,请重新获取验证码", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
        }

    }

    /**
     * 手机号检查
     */
    private Boolean checkPhoneNum(String s) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btn_register_code.setClickable(false);
            btn_register_code.setText("  重新发送(" + millisUntilFinished / 1000 + "s)  ");
            btn_register_code.setTextColor(R.color.dark_grey);
            btn_register_code.setBackgroundResource(R.drawable.regitser_code_again);
        }

        @Override
        public void onFinish() {
            btn_register_code.setText("  重新发送  ");
            btn_register_code.setTextColor(R.color.white);
            btn_register_code.setBackgroundResource(R.drawable.login_button);
            btn_register_code.setClickable(true);

        }
    }
}
