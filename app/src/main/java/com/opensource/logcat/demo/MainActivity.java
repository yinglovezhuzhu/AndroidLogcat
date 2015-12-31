package com.opensource.logcat.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.opensource.logcat.Logcat;

/**
 * 主页面
 * Created by yinglovezhuzhu@gmail.com on 2015/12/31.
 */
public class MainActivity extends Activity {

    private ToggleButton mTBtnAddLog;
    private android.os.Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.d("AndroidLogcat", "This is test log : " + System.currentTimeMillis());
                    if(null != mTBtnAddLog && mTBtnAddLog.isChecked()) {
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTBtnAddLog = (ToggleButton) findViewById(R.id.tbtn_log_toggle);
        mTBtnAddLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                } else {
                    mHandler.removeMessages(0);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logcat.enableLogcat(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logcat.disableLogcat();
    }
}
