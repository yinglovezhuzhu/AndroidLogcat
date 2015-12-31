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

package com.opensource.logcat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opensource.logcat.Logcat;

/**
 * 日志数据列表适配器
 * Created by yinglovezhzuhu@gmail.com on 2015/12/29.
 */
public class LogAdapter extends BaseAdapter {

    private Context mContext;

    public LogAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return Logcat.getInstance().getLogSize();
    }

    @Override
    public CharSequence getItem(int position) {
        return Logcat.getInstance().getLog(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if(null == convertView) {
            textView = new TextView(mContext);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(5, 5, 5, 5);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            convertView = textView;
        } else {
            textView = (TextView) convertView;
        }
        textView.setText(getItem(position));
        return convertView;
    }
}
