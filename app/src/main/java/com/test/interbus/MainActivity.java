package com.test.interbus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.github.interbus.BusCallback;
import com.github.interbus.InterBean;
import com.github.interbus.InterBus;

public class MainActivity extends Test implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    Button btPost;
    Button btPost2;
    CheckBox cbSetEvent1;
    CheckBox cbSetEvent2;
    CheckBox cbSetEvent3;

    Button btPostSticky;
    Button btPostSticky2;
    CheckBox cbSetEventSticky1;
    CheckBox cbSetEventSticky2;
    CheckBox cbSetEventSticky3;

    private InterBean interBean1;
    private InterBean interBean2;
    private InterBean interBean3;


    private InterBean interBeanSticky1;
    private InterBean interBeanSticky2;
    private InterBean interBeanSticky3;

    Button btOther;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("=====","===111=="+this.getClass().hashCode());
        setContentView(R.layout.activity_main);

        btOther = findViewById(R.id.btOther);
        btOther.setOnClickListener(this);

        btPost = findViewById(R.id.btPost);
        btPost.setOnClickListener(this);

        btPost2 = findViewById(R.id.btPost2);
        btPost2.setOnClickListener(this);

        cbSetEvent1 = findViewById(R.id.cbSetEvent1);
        cbSetEvent1.setOnCheckedChangeListener(this);

        cbSetEvent2 = findViewById(R.id.cbSetEvent2);
        cbSetEvent2.setOnCheckedChangeListener(this);

        cbSetEvent3 = findViewById(R.id.cbSetEvent3);
        cbSetEvent3.setOnCheckedChangeListener(this);


        btPostSticky = findViewById(R.id.btPostSticky);
        btPostSticky.setOnClickListener(this);

        btPostSticky2 = findViewById(R.id.btPostSticky2);
        btPostSticky2.setOnClickListener(this);

        cbSetEventSticky1 = findViewById(R.id.cbSetEventSticky1);
        cbSetEventSticky1.setOnCheckedChangeListener(this);

        cbSetEventSticky2 = findViewById(R.id.cbSetEventSticky2);
        cbSetEventSticky2.setOnCheckedChangeListener(this);

        cbSetEventSticky3 = findViewById(R.id.cbSetEventSticky3);
        cbSetEventSticky3.setOnCheckedChangeListener(this);


        test();


    }

    private void test() {

        InterBus.get().setEvent(TestEvent.class, null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cbSetEvent1:
                    if(isChecked){
                        interBean1 = InterBus.get().setEvent(TestEvent.class, new BusCallback<TestEvent>() {
                            @Override
                            public void accept(TestEvent event) {
                                cbSetEvent1.setText(cbSetEvent1.getTag()+"--收到消息:"+event.str);
                                Log.i("====","====interBean1=="+event.str);
                            }
                        });
                        cbSetEvent1.setTag("取消订阅1");
                        cbSetEvent1.setText("取消订阅1");
                    }else{
                        cbSetEvent1.setText("订阅1");
                        cbSetEvent1.setTag("订阅1");
                        InterBus.get().remove(interBean1);
                    }
                break;
            case R.id.cbSetEvent2:
                if(isChecked){
                    interBean2 = InterBus.get().setEvent(TestEvent.class, new BusCallback<TestEvent>() {
                        @Override
                        public void accept(TestEvent event) {
                            cbSetEvent2.setText(cbSetEvent2.getTag()+"--收到消息:"+event.str);
                            Log.i("====","====interBean2=="+event.str);
                        }
                    });
                    cbSetEvent2.setTag("取消订阅1");
                    cbSetEvent2.setText("取消订阅1");
                }else{
                    cbSetEvent2.setText("订阅1");
                    cbSetEvent2.setTag("订阅1");
                    InterBus.get().remove(interBean2);
                }
                break;
            case R.id.cbSetEvent3:
                if(isChecked){
                    interBean3 = InterBus.get().setEvent(TestEvent2.class, new BusCallback<TestEvent2>() {
                        @Override
                        public void accept(TestEvent2 event) {
                            cbSetEvent3.setText(cbSetEvent3.getTag()+"--收到消息:"+event.str);
                            Log.i("====","====interBean3=="+event.str);
                        }
                    });
                    cbSetEvent3.setText("取消订阅2");
                    cbSetEvent3.setTag("取消订阅2");
                }else{
                    cbSetEvent3.setText("订阅2");
                    cbSetEvent3.setTag("订阅1");
                    InterBus.get().remove(interBean3);
                }
                break;
            case R.id.cbSetEventSticky1:
                if(isChecked){
                    cbSetEventSticky1.setTag("取消订阅Sticky1");
                    cbSetEventSticky1.setText("取消订阅Sticky1");
                    interBeanSticky1 = InterBus.get().setEventSticky(TestEvent.class, new BusCallback<TestEvent>() {
                        @Override
                        public void accept(TestEvent event) {
                            cbSetEventSticky1.setText(cbSetEventSticky1.getTag()+"--收到消息:"+event.str);
                            Log.i("====","====interBean1=="+event.str);
                        }
                    });
                }else{
                    cbSetEventSticky1.setText("订阅Sticky1");
                    cbSetEventSticky1.setTag("订阅Sticky1");
                    InterBus.get().remove(interBeanSticky1);
                }
                break;
            case R.id.cbSetEventSticky2:
                if(isChecked){
                    cbSetEventSticky2.setTag("取消订阅Sticky1");
                    cbSetEventSticky2.setText("取消订阅Sticky1");
                    interBeanSticky2 = InterBus.get().setEventSticky(TestEvent.class, new BusCallback<TestEvent>() {
                        @Override
                        public void accept(TestEvent event) {
                            cbSetEventSticky2.setText(cbSetEventSticky2.getTag()+"--收到消息:"+event.str);
                            Log.i("====","====interBean1=="+event.str);
                        }
                    });
                }else{
                    cbSetEventSticky2.setText("订阅Sticky1");
                    cbSetEventSticky2.setTag("订阅Sticky1");
                    InterBus.get().remove(interBeanSticky2);
                }
                break;
            case R.id.cbSetEventSticky3:
                if(isChecked){
                    cbSetEventSticky3.setTag("取消订阅Sticky1");
                    cbSetEventSticky3.setText("取消订阅Sticky1");
                    interBeanSticky3 = InterBus.get().setEventSticky(TestEvent2.class, new BusCallback<TestEvent2>() {
                        @Override
                        public void accept(TestEvent2 event) {
                            cbSetEventSticky3.setText(cbSetEventSticky3.getTag()+"--收到消息:"+event.str);
                            Log.i("====","====interBean1=="+event.str);
                        }
                    });
                }else{
                    cbSetEventSticky3.setText("订阅Sticky1");
                    cbSetEventSticky3.setTag("订阅Sticky1");
                    InterBus.get().remove(interBeanSticky3);
                }
                break;
        }
    }

    public static class TestEvent {
        public String str;

        public TestEvent(String str) {
            this.str = str;
        }
    }
    public static class TestEvent2 {
        public String str;

        public TestEvent2(String str) {
            this.str = str;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btPost:
                InterBus.get().post(new TestEvent("android"));
                break;
            case R.id.btPost2:
                InterBus.get().post(new TestEvent2("IOS"));
                break;
            case R.id.btPostSticky:
                InterBus.get().postSticky(new TestEvent("StickyAndroid"));
                break;
            case R.id.btPostSticky2:
                InterBus.get().postSticky(new TestEvent2("StickyIOS"));
                break;
            case R.id.btOther:
                startActivity(new Intent(this,TestActivity.class));
                break;
        }
    }

}
