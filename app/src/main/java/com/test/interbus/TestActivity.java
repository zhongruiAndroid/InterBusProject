package com.test.interbus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.interbus.BusCallback;
import com.github.interbus.InterBean;
import com.github.interbus.InterBus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TestActivity extends AppCompatActivity {

    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_test);

        tv = findViewById(R.id.tv);


        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EventBus.getDefault().postSticky(new MainActivity.TestEvent("cv"));
            }
        });

        test();

    }

    private void test() {
        InterBean interBean1 = InterBus.get().setEvent(Test.class, new BusCallback<Test>() {
            @Override
            public void accept(Test event) {
                Log.i("===========", "accept===========setEvent");
            }
        });
        InterBean interBean = InterBus.get().setEventSticky(Test.class, new BusCallback<Test>() {
            @Override
            public void accept(Test event) {
                Log.i("===========", "accept===========setEventSticky");
            }
        });
        InterBus.get().post(new Test());
        InterBus.get().postSticky(new Test());
        InterBus.get().addSubscribe(this,interBean1);
        InterBus.get().addSubscribe(this,interBean);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        InterBus.get().unSubscribe(this);
    }
    @Subscribe( sticky=true)
    public void onMessageEvent(MainActivity.TestEvent event) {
        Log.i("===========", "onMessageEvent===========onMessageEvent"+event.str);
    };
    @Subscribe( sticky=true)
    public void onMessageEventasdf(MainActivity.TestEvent event) {
        Log.i("===========", "onMessageEvent====222=======onMessageEvent"+event.str);
    };
}
