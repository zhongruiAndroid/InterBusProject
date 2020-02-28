package com.github.interbus;

import android.os.SystemClock;

import org.junit.Test;

import java.util.HashSet;
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