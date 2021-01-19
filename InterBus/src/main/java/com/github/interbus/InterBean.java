package com.github.interbus;

class InterBean {
    public int postKey;
    public int registerCode;
    public boolean isStickyEvent;
    public BusCallback busCallback;
    public Object stickEventObj;

    public InterBean(int postKey, int registerCode, boolean isStickyEvent, BusCallback callback) {
        this.postKey = postKey;
        this.registerCode = registerCode;
        this.isStickyEvent = isStickyEvent;
        this.busCallback = callback;
    }


}