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

package com.opensource.logcat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.opensource.logcat.observer.LogObservable;
import com.opensource.logcat.observer.LogObserver;
import com.opensource.logcat.ui.activity.LogcatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 滚屏日志
 * Created by yinglovezhuzhu@gmail.com on 2015/8/24.
 */
public class Logcat {

    public static final String SP_LOGCAT_CONFIG = "logcat_config";

    /** 关闭logcat **/
    public static final String ACTION_CLOSE_LOGCAT = "com.ghw.sdk.ACTION_CLOSE_LOGCAT";
    /** logcat关闭成功 **/
    public static final String ACTION_LOGCAT_CLOSED = "com.ghw.sdk.ACTION_LOGCAT_CLOSED";
    /** logcat打开 **/
    public static final String ACTION_LOGCAT_OPENED = "com.ghw.sdk.ACTION_LOGCAT_OPENED";

    private static final String SP_KEY_FLOW_BUTTON_X = "debug_flow_button_x";
    private static final String SP_KEY_FLOW_BUTTON_Y = "debug_flow_button_y";
    /** Boolean **/
    private static final String SP_KEY_LOG_AUTO_SCROLL = "log_auto_scroll";

    private static final int DEFAULT_LOG_MAX_LIENS = 10000;

    private Context mAppContext;
    private WindowManager mWindowManager;
    private Button mBtnLog;
    private WindowManager.LayoutParams mBtnLayoutParams;

    private final LogcatHandler mHandler = new LogcatHandler(Looper.getMainLooper());
    private LogObservable mLogObservable = new LogObservable();

    private SharedPrefHelper mSharePrefHelper;
    private int mMaxLines = DEFAULT_LOG_MAX_LIENS;

    private final List<String> mLogs = new ArrayList<>();

    private boolean mInitialized = false;
    private boolean mLogcatOpened = false;
    private boolean mLogAutoScroll = false;

    private ReadLogThread mReadLogThread;

    private static Logcat mInstance = null;

    private Logcat() {
    }

    /**
     * 启用Logcat，启用后将会有一个悬浮按钮入口，强烈建议在Activity的onResume中调用
     * @param activity
     */
    public static void enableLogcat(Activity activity) {
        getInstance().enableLogcatWindow(activity);
    }

    /**
     * 禁用Logcat，禁用后将关闭悬浮按钮入口，强烈建议在Activity的onStop中调用
     */
    public static void disableLogcat() {
        getInstance().disableLogcatWindow();
    }

    /**
     * 设置保存最大的日志数
     * @param maxLines
     */
    public static void setMaxLogLines(int maxLines) {
        getInstance().setMaxLines(maxLines);
    }

    public static Logcat getInstance() {
        synchronized (Logcat.class) {
            if(null == mInstance) {
                mInstance = new Logcat();
            }
            return mInstance;
        }
    }

    void initialize(Context context) {
        if(mInitialized) {
            return;
        }
        mAppContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mAppContext.getSystemService(Context.WINDOW_SERVICE);

        mSharePrefHelper = SharedPrefHelper.newInstance(context.getApplicationContext(), SP_LOGCAT_CONFIG);
        mLogAutoScroll = mSharePrefHelper.getBoolean(SP_KEY_LOG_AUTO_SCROLL, false);

        initLayoutParams();

        initLogButton(mAppContext);

        LocalReceiver receiver = new LocalReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_LOGCAT_CLOSED);
        mAppContext.registerReceiver(receiver, intentFilter);

        mInitialized = true;
    }

    void enableLogcatWindow(Activity activity) {

        initialize(activity);

        try {
            mWindowManager.addView(mBtnLog, mBtnLayoutParams);
        } catch (Exception e) {
            // do nothing
        }

        mReadLogThread = new ReadLogThread();
        mReadLogThread.start();
    }

    void disableLogcatWindow() {
        if(null != mWindowManager) {
            if(null != mBtnLog) {
                try {
                    mWindowManager.removeView(mBtnLog);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
        if(null != mReadLogThread) {
            mReadLogThread.interrupt();
            mReadLogThread = null;
        }
    }

    void setMaxLines(int maxLines) {
        this.mMaxLines = maxLines;
    }


    /**
     * 注册一个日志观察者
     * @param observer
     */
    public void registerLogObserver(LogObserver observer) {
        mLogObservable.registerObserver(observer);
    }

    /**
     * 反注册一个日志观察者
     * @param observer
     */
    public void unregisterLogObserver(LogObserver observer) {
        mLogObservable.unregisterObserver(observer);
    }

    /**
     * 反注册所有的日志观察者
     */
    public void unregisterAllLogObserver() {
        mLogObservable.unregisterAll();
    }

    //------- Log ---------

    /**
     * 日志数据的长度
     */
    public int getLogSize() {
        synchronized (mLogs) {
            return mLogs.size();
        }
    }

    public String getLog(int position) {
        synchronized (mLogs) {
            return mLogs.get(position);
        }
    }

    public void clearLog() {
        synchronized (mLogs) {
            mLogs.clear();
        }
        new ClearLogThread().start();
    }

    public boolean isLogAutoScroll() {
        return mLogAutoScroll;
    }

    public void setLogAutoScroll(boolean autoScroll) {
        this.mLogAutoScroll = autoScroll;
        if(null != mSharePrefHelper) {
            mSharePrefHelper.saveBoolean(SP_KEY_LOG_AUTO_SCROLL, autoScroll);
        }
    }

    /**
     * 记录log
     * @param log
     */
    public void addLog(String log) {
        mHandler.sendMessage(mHandler.obtainMessage(LogcatHandler.MSG_LOG, log));
    }

    //------- Log ---------

    /**
     * 显示日志窗口
     */
    private void showLog() {
        if(mLogcatOpened) {
            Intent intent = new Intent(Logcat.ACTION_CLOSE_LOGCAT);
            mAppContext.sendBroadcast(intent);
            mLogcatOpened = false;
            return;
        }
        Intent intent = new Intent(mAppContext, LogcatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mAppContext.startActivity(intent);
        mLogcatOpened = true;
    }

    /**
     * 初始化入口悬浮按钮
     * @param context
     */
    private void initLogButton(Context context) {
        mBtnLog = new Button(context);
        mBtnLog.setText("LOG");
        mBtnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog();
            }
        });
        mBtnLog.setOnTouchListener(new View.OnTouchListener() {

            float startX = 0f;
            float startY = 0f;

            float downX = 0f;
            float downY = 0f;
            int statusBarHeight = 0;
            boolean start = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getRawX();
                            startY = event.getRawY();
                            downX = event.getX();
                            downY = event.getY();
                            Rect rect = new Rect();
                            mBtnLog.getWindowVisibleDisplayFrame(rect);
                            statusBarHeight = rect.top;

                            start = true;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (start && Math.abs(event.getRawX() - startX) < 5 && Math.abs(event.getRawY() - startY) < 5) {
                                start = false;
                                return true;
                            }
                            mBtnLayoutParams.x = (int) (event.getRawX() - downX);
                            mBtnLayoutParams.y = (int) (event.getRawY() - downY - statusBarHeight);
                            mWindowManager.updateViewLayout(mBtnLog, mBtnLayoutParams);
                            mSharePrefHelper.saveInt(SP_KEY_FLOW_BUTTON_X, mBtnLayoutParams.x);
                            mSharePrefHelper.saveInt(SP_KEY_FLOW_BUTTON_Y, mBtnLayoutParams.y);
                            return true;
                        case MotionEvent.ACTION_UP:
                            return Math.abs(event.getRawX() - startX) > 5 || Math.abs(event.getRawY() - startY) > 5;
                        default:
                            break;

                    }
                } catch (Exception e) {
                    // do nothing
                }
                return false;
            }
        });

    }

    private void initLayoutParams() {
        mBtnLayoutParams = new WindowManager.LayoutParams();
        mBtnLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mBtnLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mBtnLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mBtnLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mBtnLayoutParams.format = PixelFormat.RGBA_8888;
        mBtnLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

        if(null != mSharePrefHelper) {
            mBtnLayoutParams.x = mSharePrefHelper.getInt(SP_KEY_FLOW_BUTTON_X, 0);
            mBtnLayoutParams.y = mSharePrefHelper.getInt(SP_KEY_FLOW_BUTTON_Y, 0);
        }

    }

    /**
     * 读取日志的线程类
     */
    private class ReadLogThread extends Thread {
        Process process = null;
        @Override
        public void run() {
            try {
//                    process = Runtime.getRuntime().exec("logcat -v time");
                process = new ProcessBuilder()
                        .command("logcat", "-v", "time", "|", "grep", String.valueOf(android.os.Process.myPid()))
//                        .command("logcat", "-v", "time")
                        .redirectErrorStream(true)
                        .start();
                final InputStream is = process.getInputStream();
                final InputStream es = process.getErrorStream();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String line;
                        try {
                            while((line = br.readLine()) != null) {
                                addLog(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        BufferedReader br = new BufferedReader(new InputStreamReader(es));
                        String line;
                        try {
                            while((line = br.readLine()) != null) {
                                addLog(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                process.waitFor();

            } catch (IOException|InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(null != process) {
                    process.destroy();
                }
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            if(null != process) {
                process.destroy();
            }
        }
    }

    /**
     * 清除log
     */
    private class ClearLogThread extends Thread {
        @Override
        public void run() {
            Process process = null;
            try {
                process = new ProcessBuilder()
                        .command("logcat", "-c")
                        .redirectErrorStream(true)
                        .start();
                int exitValue = process.waitFor();
                Log.d("AndroidLogcat", "Clear log-->>>>" + exitValue);
            } catch (IOException|InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(null != process) {
                    process.destroy();
                }
            }
        }
    }

    private class LogcatHandler extends Handler {

        public static final int MSG_LOG = 0x01;

        public LogcatHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LOG:
                    synchronized (mLogs) {
                        String log = (String) msg.obj;
                        mLogs.add(log);
                        if(mLogs.size() > mMaxLines) {
                            mLogs.remove(0);
                        }
                        mLogObservable.notifyLogChanged(log);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(null == intent) {
                return;
            }
            String action = intent.getAction();
            if(Logcat.ACTION_LOGCAT_CLOSED.equals(action)) {
                mLogcatOpened = false;
            } else if(Logcat.ACTION_LOGCAT_OPENED.equals(action)) {
                mLogcatOpened = true;
            }
        }
    }

}
