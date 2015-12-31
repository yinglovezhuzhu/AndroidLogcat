/*
 * Copyright (C) 2015. The Android Open Source Project.
 *
 *         yinglovezhuzhu@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.opensource.logcat.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.opensource.logcat.Logcat;
import com.opensource.logcat.SharedPrefHelper;

/**
 * 主页面
 * Created by yinglovezhuzhu@gmail.com on 2015/12/31.
 */
public class MainActivity extends Activity {

    private ToggleButton mTBtnAddLog;
    private SharedPrefHelper mSharedPrefHelper;
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

        mSharedPrefHelper = SharedPrefHelper.newInstance(this, Logcat.SP_LOGCAT_CONFIG);

        mTBtnAddLog = (ToggleButton) findViewById(R.id.tbtn_log_toggle);
        mTBtnAddLog.setOnCheckedChangeListener(mOnCheckedChangeListener);

        ToggleButton tBtnLogcatToggle = (ToggleButton) findViewById(R.id.tbtn_logcat_toggle);
        tBtnLogcatToggle.setChecked(mSharedPrefHelper.getBoolean("logcat_enabled", false));
        tBtnLogcatToggle.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tbtn_logcat_toggle:
                    if(isChecked) {
                        Logcat.enableLogcat(MainActivity.this);
                    } else {
                        Logcat.disableLogcat();
                    }
                    break;
                case R.id.tbtn_log_toggle:
                    if(isChecked) {
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    } else {
                        mHandler.removeMessages(0);
                    }
                    mSharedPrefHelper.saveBoolean("logcat_enabled", isChecked);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(mSharedPrefHelper.getBoolean("logcat_enabled", false)) {
            Logcat.enableLogcat(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logcat.disableLogcat();
    }
}
