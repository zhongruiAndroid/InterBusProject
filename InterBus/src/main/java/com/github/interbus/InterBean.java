package com.github.interbus;

public class InterBean {
    public int postKey;
    public boolean isStickyEvent;
    public BusCallback busCallback;

    public InterBean(int postKey, boolean isStickyEvent, BusCallback callback) {
        this.postKey = postKey;
        this.isStickyEvent = isStickyEvent;
        this.busCallback = callback;
    }

}