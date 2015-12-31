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
