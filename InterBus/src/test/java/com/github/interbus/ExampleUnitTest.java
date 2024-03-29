package com.github.interbus;

import android.os.SystemClock;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public void addffd() {
        for (int i = 0; i < 8; i++) {
            System.out.println(new Object[0].hashCode()+":===");
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

    class TestObj extends Base<ExampleUnitTest>{

    }
    class Base<T>{

    }

    @Test
    public void sdddafd() {
        System.out.println("".length());
        System.out.println(" ".length());
        System.out.println(" ".replace(" ","").length());
        System.out.println("   ".replace(" ","").length());
        System.out.println(TestObj.class.getSuperclass());
        System.out.println(TestObj.class.getGenericSuperclass());
        System.out.println(TestObj.class.getGenericInterfaces());
    }
    @Test
    public void asdf() {
        CopyOnWriteArrayList<Object> a=new CopyOnWriteArrayList<Object>();
        a.add("1");
        a.add("a");
        a.add("2");
        a.add("b");
        a.add("3");
        a.add("c");
        a.add("1");
        a.add("11");
        Iterator<Object> iterator = a.iterator();
        while (iterator.hasNext()){
            Object next = iterator.next();
            System.out.println(next+"=======");
        }
    }
}