package com.test.interbus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.interbus.BusCallback;
import com.github.interbus.InterBus;

import java.util.ArrayList;
import java.util.List;

public class SingleEventActivity extends AppCompatActivity implements View.OnClickListener {
    private CheckBox cbSingleType;


    private List<String> listStr = new ArrayList<>();
    private List<String> listStrSticky = new ArrayList<>();
    private TextView tvTips;
    private TextView tvTipsSticky;

    /*这里需要在同一个act中测试两种事件，所以用了两个object，平时使用，直接传this即可(在内部类中不能直接传this,需要传类名.this,比如 TestActivity.this)*/
    private Object simpleEvent=11;
    private Object stickyEvent=22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_event);

        cbSingleType = findViewById(R.id.cbSingleType);


        Button btRegister = findViewById(R.id.btRegister);
        btRegister.setOnClickListener(this);

        Button btPost = findViewById(R.id.btPost);
        btPost.setOnClickListener(this);

        Button btUnRegister = findViewById(R.id.btUnRegister);
        btUnRegister.setOnClickListener(this);

        tvTips = findViewById(R.id.tvTips);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InterBus.get().unSubscribe(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btRegister:
                addText(listStr, "订阅普通事件");
                setText(listStr, tvTips);
                InterBus.get().setSingleEvent(simpleEvent, EventBean.class, cbSingleType.isChecked(), new BusCallback<EventBean>() {
                    @Override
                    public void accept(EventBean event) {
                        Log.i("=====", "===1=====<单一>普通事件:" + event.content);
                        addText(listStr, event.content);
                        setText(listStr, tvTips);
                    }
                });
                InterBus.get().setSingleEvent(simpleEvent, EventBean.class, cbSingleType.isChecked(), new BusCallback<EventBean>() {
                    @Override
                    public void accept(EventBean event) {
                        Log.i("=====", "===2=====<单一>普通事件:" + event.content);
                        addText(listStr, event.content);
                        setText(listStr, tvTips);
                    }
                });
                break;
            case R.id.btPost:
                addText(listStr, "发送普通事件");
                setText(listStr, tvTips);
                InterBus.get().post(new EventBean("普通消息来了"));
                break;
            case R.id.btUnRegister:
                InterBus.get().unSubscribe(simpleEvent);
                listStr.clear();
                tvTips.setText("取消普通事件订阅");
                break;
        }
    }

    private void addText(List<String> list, String str) {
        if (list == null || list.contains(str)) {
            return;
        }
        list.add(str);
    }

    private void setText(List<String> list, TextView textView) {
        if (textView == null || list == null || list.isEmpty()) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String item : list) {
            stringBuilder.append("—>");
            stringBuilder.append(item);
        }
        textView.setText(stringBuilder.toString());
    }
}

//12.3.4567
