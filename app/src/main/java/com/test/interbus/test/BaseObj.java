package com.test.interbus.test;

import android.util.Log;

import com.github.interbus.BusCallback;
import com.github.interbus.InterBus;

import java.util.ArrayList;
import java.util.List;

public class BaseObj<T> {
    public static void test(Object obj) {
        //event.getClass().getName().hashCode()
        //clazz.getName().hashCode();
        InterBus.get().post(1);
        InterBus.get().post("2");
        InterBus.get().post('3');
        InterBus.get().post(4.4);
        InterBus.get().post(true);
        InterBus.get().postSticky(1);
        InterBus.get().postSticky(null);

    }
    public static void testSet(){

    }
    public static void testPost(){

    }
    public static void main(String[] args) {
        InterBus.get().setEvent("1", List.class, new BusCallback<List>() {
            @Override
            public void accept(List event) {
                Log.i("=====","=====accept");
            }
        });
        List<String>list=new ArrayList<>();
        List<Integer>list2=new ArrayList<>();
        InterBus.get().post(list);
        InterBus.get().post(list2);
    }
}
