package com.test.interbus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.rxbus.MyConsumer;
import com.github.rxbus.MyDisposable;
import com.github.rxbus.RxBus;

public class TestActivity extends AppCompatActivity {

    private MyDisposable eventReplay;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        tv = findViewById(R.id.tv);
        eventReplay = RxBus.getInstance().getEventReplay(Test.class, new MyConsumer<Test>() {
            @Override
            public void onAccept(Test event) {
                tv.setText("来消息啦！");
                Log.i("==========","==========");
            }
        });
        eventReplay = RxBus.getInstance().getEventReplay(Test.class, new MyConsumer<Test>() {
            @Override
            public void onAccept(Test event) {
                Log.i("==========","=====222=====");
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().dispose(eventReplay);
    }
}
