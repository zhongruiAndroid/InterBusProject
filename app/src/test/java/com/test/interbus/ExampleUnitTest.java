package com.test.interbus;

import android.util.Log;

import com.github.interbus.BusCallback;
import com.github.interbus.InterBus;

import org.junit.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void sd() {
        main(List.class);
    }
    public void main(Class clazz) {
        System.out.println(clazz.getName().hashCode());

        InterBus.get().setEvent("1", List.class, new BusCallback<List>() {
            @Override
            public void accept(List event) {
                Log.i("=====","=====accept");
            }
        });
        List<String>list=new ArrayList<>();
        List<Integer>list2=new ArrayList<>();
        int postKey = list.getClass().getName().hashCode();
        int postKey2 = list2.getClass().getName().hashCode();
        System.out.println(postKey);
        System.out.println(postKey2);
        InterBus.get().post(list);
        InterBus.get().post(list2);
    }
    @Test
    public void sdd() {
        String s=new String();
        System.out.println(s.getClass().getName());
        List<String> list=new ArrayList<String>();
        System.out.println(list.getClass().getName());

        testSet(new BusCallback<String>() {
            @Override
            public void accept(String event) {
            }
        });
        testSet(new BusCallback<List<String>>() {
            @Override
            public void accept(List<String> event) {
            }
        });
        testSet(new BusCallback<List>() {
            @Override
            public void accept(List event) {
            }
        });
    }
    public void testSet(BusCallback busCallback){
        System.out.println("============================================");
        Type[] genericSuperclass = busCallback.getClass().getGenericInterfaces();
        for (Type type:genericSuperclass){
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) type;
                Type actualTypeArgument = parameterized.getActualTypeArguments()[0];
                if(actualTypeArgument instanceof GenericArrayType){
                    GenericArrayType newType = (GenericArrayType) actualTypeArgument;
                    String name = ((Class) newType.getGenericComponentType()).getName();
                    System.out.println(int[].class.getName()+"=====testObj1===="+name);
                }else{
                    Type actualTypeArgument1 = parameterized.getActualTypeArguments()[0];
                    System.out.println(int[].class.getName()+"=====testObj2===="+actualTypeArgument1);
                }
            }
        }
        System.out.println("============================================");
    }
}