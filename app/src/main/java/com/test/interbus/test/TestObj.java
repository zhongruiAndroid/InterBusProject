package com.test.interbus.test;

import com.github.interbus.BusCallback;
import com.github.interbus.BusResult;

public class TestObj extends BaseObj<Integer> implements BusCallback {
    @Override
    public void accept(Object event, BusResult busResult) {

    }
}
