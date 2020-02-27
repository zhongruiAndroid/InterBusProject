package com.github.interbus;

public class InterBean {
    /*从map移除需要的key*/
    public int setKey;
    /*发送事件需要的key*/
    public int postKey;
    /*是否是粘性事件*/
    public boolean isStickyEvent;

    public InterBean(int setKey, int postKey, boolean isStickyEvent) {
        this.setKey = setKey;
        this.postKey = postKey;
        this.isStickyEvent = isStickyEvent;
    }
}