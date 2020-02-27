package com.test.interbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.interbus.BusCallback;
import com.github.interbus.InterBean;
import com.github.interbus.InterBus;


public class TestActivity extends AppCompatActivity {

    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        tv = findViewById(R.id.tv);

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
        InterBus.get().unSubscribe(this);
    }
}
