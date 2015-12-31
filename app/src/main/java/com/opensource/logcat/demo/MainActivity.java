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
