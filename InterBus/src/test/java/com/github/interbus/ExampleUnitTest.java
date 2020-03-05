package com.github.interbus;

import android.os.SystemClock;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void adfd() {
        List<String> list=new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("31");
        list.add("32");
        for(String str:list){
            System.out.println(str);
        }
    }
    @Test
    public void addfd() {
        Map<Integer,String> map=new LinkedHashMap<>(1,0.3f,true);
        map.put(1,"1");
        map.put(2,"2");
        map.put(3,"3");
        map.get(1);
        for (Map.Entry<Integer, String> item:map.entrySet()){
            System.out.println(item.getKey()+"===="+item.getValue());
        }
    }
    @Test
    public void afd() {
        String a="asdf";
        int i = this.getClass().getName().hashCode();

//        System.out.println(TestA.class.getName());
//        System.out.println(TestB.class.getName());


        String className=getClass().getName();
        int setKey = (className+System.currentTimeMillis()).hashCode();
        int postKey = className.hashCode();
        System.out.println(setKey);
        System.out.println(postKey);
    }

    @Test
    public void sdafd() {
        Set<TestA> set=new HashSet<>();
        TestA a=new TestA();
        TestA b=new TestA();
        set.add(a);
        set.add(a);
        System.out.println(set.size()+"=============");
    }
    public static class TestA{

    }
    public static class TestB{

    }
}