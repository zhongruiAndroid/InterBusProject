package com.github.interbus;

public class InterBean {
    public int setKey;
    public int postKey;
    public boolean isStickyEvent;

    public InterBean(int setKey, int postKey, boolean isStickyEvent) {
        this.setKey = setKey;
        this.postKey = postKey;
        this.isStickyEvent = isStickyEvent;
    }
}