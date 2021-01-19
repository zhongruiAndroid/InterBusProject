package com.test.interbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.interbus.BusCallback;
import com.github.interbus.InterBus;
import com.test.interbus.test.BaseObj;
import com.test.interbus.test.TestObj;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    private List<String> listStr=new ArrayList<>();
    private List<String> listStrSticky=new ArrayList<>();
    private TextView tvTips;
    private TextView tvTipsSticky;

    /*这里需要在同一个act中测试两种事件，所以用了两个object，平时使用，直接传this即可(在内部类中不能直接传this,需要传类名.this,比如 TestActivity.this)*/
    private Object simpleEvent=1;
    private Object stickyEvent=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testObj(new BusCallback<int[]>() {
            @Override
            public void accept(int[] event) {

            }
        });
        setContentView(R.layout.activity_test);

        Button btRegister = findViewById(R.id.btRegister);
        btRegister.setOnClickListener(this);

        Button btPost = findViewById(R.id.btPost);
        btPost.setOnClickListener(this);

        Button btUnRegister = findViewById(R.id.btUnRegister);
        btUnRegister.setOnClickListener(this);

        tvTips = findViewById(R.id.tvTips);

        Button btRegisterSticky = findViewById(R.id.btRegisterSticky);
        btRegisterSticky.setOnClickListener(this);

        Button btPostSticky = findViewById(R.id.btPostSticky);
        btPostSticky.setOnClickListener(this);

        Button btUnRegisterSticky = findViewById(R.id.btUnRegisterSticky);
        btUnRegisterSticky.setOnClickListener(this);

        tvTipsSticky = findViewById(R.id.tvTipsSticky);


    }

    private void testObj(BusCallback<int[]> busCallback) {
        Type[] genericSuperclass = busCallback.getClass().getGenericInterfaces();
        for (Type type:genericSuperclass){
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) type;
                Type actualTypeArgument = parameterized.getActualTypeArguments()[0];
                if(actualTypeArgument instanceof GenericArrayType){
                    GenericArrayType newType = (GenericArrayType) actualTypeArgument;
                    String name = ((Class) newType.getGenericComponentType()).getName();
                    Log.i("=====",int[].class.getName()+"=====testObj1===="+name);
                }else{
                    String name = ((Class) parameterized.getActualTypeArguments()[0]).getName();
                    Log.i("=====",int[].class.getName()+"=====testObj2===="+name);
                }
            }
        }
        if(true){
            return;
        }
        Log.i("=====","=============================================");
        Log.i("=====","====="+busCallback.getClass().getSuperclass());
        Log.i("=====","====="+busCallback.getClass().getGenericSuperclass());
        Type[] genericInterfaces = busCallback.getClass().getGenericInterfaces();
        Type[] genericInterfaces1 = busCallback.getClass().getInterfaces();
        for (Type type:genericInterfaces){
            Log.i("=====",type.toString()+"=Type===="+type.getClass().getGenericSuperclass());
        }
        Log.i("=====","=============================================");

//            ParameterizedType superClass = (ParameterizedType) busCallback.getClass().getGenericSuperclass();
//        Type actualTypeArgument = superClass.getActualTypeArguments()[0];
//        Log.i("=====","====="+actualTypeArgument.toString());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        InterBus.get().unSubscribe(this);
    }
    int a=0;
    int b=0;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btRegister:
                addText(listStr,"订阅普通事件");
                setText(listStr,tvTips);
                InterBus.get().setEvent(simpleEvent, EventBean.class, new BusCallback<EventBean>() {
                    @Override
                    public void accept(EventBean event) {
                        b+=1;
                        Log.i("=====",b+"=====普通事件:"+event.content);
                        addText(listStr,event.content);
                        setText(listStr,tvTips);
                    }
                });
                break;
            case R.id.btPost:
                b=0;
                addText(listStr,"发送普通事件");
                setText(listStr,tvTips);
                InterBus.get().post(new EventBean("普通消息来了"));
                break;
            case R.id.btUnRegister:
                InterBus.get().unSubscribe(simpleEvent);
                listStr.clear();
                tvTips.setText("取消普通事件订阅");
                break;
            case R.id.btRegisterSticky:
                addText(listStrSticky,"订阅粘性事件");
                setText(listStrSticky,tvTipsSticky);
       /*         InterBus.get().setEvent(stickyEvent, EventBean.class, new BusCallback<EventBean>() {
                    @Override
                    public void accept(EventBean event) {
                        a+=1;
                        Log.i("=====",a+"=====粘性事件:"+event.content);
                        addText(listStrSticky,event.content);
                        setText(listStrSticky,tvTipsSticky);
                    }
                });*/
                InterBus.get().setStickyEvent(stickyEvent, EventStickyBean.class, new BusCallback<EventStickyBean>() {
                    @Override
                    public void accept(EventStickyBean event) {
                        a+=1;
                        Log.i("=====",a+"=====粘性事件:"+event.content);
                        addText(listStrSticky,event.content);
                        setText(listStrSticky,tvTipsSticky);
                    }
                });
                break;
            case R.id.btPostSticky:
                a=0;
                addText(listStrSticky,"发送粘性事件");
                setText(listStrSticky,tvTipsSticky);
//                InterBus.get().post(new EventBean("粘性消息来了"));
                InterBus.get().postSticky(new EventStickyBean("粘性消息来了"));
                break;
            case R.id.btUnRegisterSticky:
                InterBus.get().unSubscribe(stickyEvent);
                InterBus.get().removeStickyEvent(EventStickyBean.class);
                listStrSticky.clear();
                tvTipsSticky.setText("取消粘性事件订阅");
                break;
        }
    }
    private void addText(List<String>list,String str){
        if(list==null||list.contains(str)){
            return;
        }
        list.add(str);
    }
    private void setText(List<String>list,TextView textView){
        if(textView==null||list==null||list.isEmpty()){
            return;
        }
        StringBuilder stringBuilder=new StringBuilder();
        for (String item:list){
            stringBuilder.append("—>");
            stringBuilder.append(item);
        }
        textView.setText(stringBuilder.toString());
    }
}
