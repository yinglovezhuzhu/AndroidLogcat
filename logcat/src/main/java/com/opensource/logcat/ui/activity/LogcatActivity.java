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

package com.opensource.logcat.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.opensource.logcat.Logcat;
import com.opensource.logcat.R;
import com.opensource.logcat.adapter.LogAdapter;
import com.opensource.logcat.observer.LogObserver;

/**
 * 日志页面
 *
 * Created by yinglovezhuzhu@gmail.com on 2015/12/28.
 */
public class LogcatActivity extends Activity {

    private ListView mLvLogs;
    private LogAdapter mAdapter;

    private ToggleButton mTBtnAuto;
    private Button mBtnClear;

    private LocalReceiver mReceiver = new LocalReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_logcat);

        initView();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Logcat.ACTION_CLOSE_LOGCAT);

        registerReceiver(mReceiver, intentFilter);
        Intent intent = new Intent(Logcat.ACTION_LOGCAT_OPENED);
        sendBroadcast(intent);

        Logcat.getInstance().registerLogObserver(mLogObserver);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Logcat.getInstance().unregisterAllLogObserver();
//        Logcat.getInstance().unregisterLogObserver(mLogObserver);
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    public void exit() {
        finish();
        Intent intent = new Intent(Logcat.ACTION_LOGCAT_CLOSED);
        sendBroadcast(intent);
    }

    private void initView() {
        mLvLogs = (ListView) findViewById(R.id.lv_log);
        mAdapter = new LogAdapter(this);
        mLvLogs.setAdapter(mAdapter);

        mTBtnAuto = (ToggleButton) findViewById(R.id.tbtn_log_auto_scroll);

        mBtnClear = (Button) findViewById(R.id.btn_log_clear);

        mTBtnAuto.setChecked(Logcat.getInstance().isLogAutoScroll());
        if(Logcat.getInstance().isLogAutoScroll()) {
            mLvLogs.setSelection(mAdapter.getCount());
        }
        mLvLogs.setDividerHeight(1);

        mTBtnAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Logcat.getInstance().setLogAutoScroll(isChecked);
                if (isChecked) {
                    mLvLogs.setSelection(mAdapter.getCount());
                }
            }
        });

        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logcat.getInstance().clearLog();
                if (null != mAdapter) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    private LogObserver mLogObserver = new LogObserver() {
        @Override
        public void onLogChanged(CharSequence log) {
            mAdapter.notifyDataSetChanged();
            if(Logcat.getInstance().isLogAutoScroll()) {
                mLvLogs.setSelection(mAdapter.getCount());
            }
        }
    };

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(null == intent) {
                return;
            }
            String action = intent.getAction();
            if(Logcat.ACTION_CLOSE_LOGCAT.equals(action)) {
                exit();
            }
        }
    }
}
