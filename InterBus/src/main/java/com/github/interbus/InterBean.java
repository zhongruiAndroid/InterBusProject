package com.github.interbus;

class InterBean {
    public String postKey;
    public String registerCode;
    public boolean isStickyEvent;
    public BusCallback busCallback;
    public Object stickEventObj;

    public InterBean(String postKey, String registerCode, boolean isStickyEvent, BusCallback callback) {
        this.postKey = postKey;
        this.registerCode = registerCode;
        this.isStickyEvent = isStickyEvent;
        this.busCallback = callback;
    }


}