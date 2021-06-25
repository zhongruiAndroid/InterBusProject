package com.github.interbus;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 *   created by android on 2019/4/8
 */
public class InterBus {
    private final String REMOVE_ALL_FLAG = "-100";
    private static InterBus bus;
    private Object[] objects = new Object[0];
    private Object[] objectSticky = new Object[0];
    /*
     * 第一个key为postCode(方便根据postCode取event,发消息),第二个key为registerCode
     * 因为可能存在不同act注册相同obj消息，所以第二个map以registercode为key,也方便后续的取消某个act里面的消息订阅
     * */
    /*普通消息event容器*/
    private Map<String, Map<String, List<InterBean>>> mapEvent;
    /*粘性消息event容器*/
    private Map<String, Map<String, List<InterBean>>> mapStickyEvent;
    /*单一的消息，只保存注册的最后一个(或者最开始的一个)事件消息*/
    private Map<String, Map<String, InterBean>> singleEvent;
    /*先保存发送的粘性事件,key为postCode*/
    private Map<String, InterBean> stickyPostEvent;
    /*
     *  key为registerCode，将event保存到list里面，方便后续取消注册时根据registerCode和postCode移除
     * */
    private Map<String, List<InterBean>> needRemoveEvent;


    private InterBus() {
        mapEvent = new ConcurrentHashMap<>();
        mapStickyEvent = new ConcurrentHashMap();
        singleEvent = new ConcurrentHashMap();

        stickyPostEvent = new ConcurrentHashMap();

        needRemoveEvent = new ConcurrentHashMap();
    }

    public static InterBus get() {
        if (bus == null) {
            synchronized (InterBus.class) {
                if (bus == null) {
                    bus = new InterBus();
                }
            }
        }
        return bus;
    }

    /**
     * 普通事件
     */
    public <T> void setEvent(Object object, Class<T> clazz, BusCallback<T> busCallback) {
        if (object == null || clazz == null) {
            throw new IllegalStateException("setEvent(object,class,busCallback), object or class can not null");
        }
        String postCode = clazz.getName().hashCode() + "";
        saveEvent(object, postCode, busCallback, false);
    }

    public <T> void setEvent(Object object, String postKey, BusCallback<T> busCallback) {
        saveEvent(object, postKey, busCallback, false);
    }

    /**
     * 粘性事件
     */
    public <T> void setStickyEvent(Object object, String postKey, BusCallback<T> busCallback) {
        saveEvent(object, postKey, busCallback, true);
    }

    public <T> void setStickyEvent(Object object, Class<T> clazz, BusCallback<T> busCallback) {
        if (object == null || clazz == null) {
            throw new IllegalStateException("setStickyEvent(object,class,busCallback), object or class can not null");
        }
        String postCode = clazz.getName().hashCode() + "";
        saveEvent(object, postCode, busCallback, true);
    }

    private <T> void saveEvent(Object object, String postCode, BusCallback<T> busCallback, boolean isSticky) {
        if (busCallback == null) {
            return;
        }
        String registerCode = object.hashCode() + "";

        InterBean interBean = new InterBean(postCode, registerCode, isSticky, busCallback);

        if (isSticky) {
            /*检查是否已经发送过粘性事件*/
            InterBean hasEvent = checkHasEvent(postCode);
            if (hasEvent != null) {
                busCallback.accept((T) hasEvent.stickEventObj, hasEvent.busResult);
            }
            saveEventToMap(mapStickyEvent, postCode, registerCode, interBean);
        } else {
            saveEventToMap(mapEvent, postCode, registerCode, interBean);
        }

        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        addEventToRegisterGroup(registerCode, interBean);
    }

    private void addEventToRegisterGroup(String registerCode, InterBean interBean) {
        synchronized (objectSticky) {
            /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
            List<InterBean> interBeans = needRemoveEvent.get(registerCode);
            if (interBeans == null) {
                interBeans = new ArrayList<>();
                needRemoveEvent.put(registerCode, interBeans);
            }
            interBeans.add(interBean);
        }
    }

    private InterBean checkHasEvent(String postCode) {
        if (stickyPostEvent == null || stickyPostEvent.isEmpty()) {
            return null;
        }
        InterBean stickyEvent = stickyPostEvent.get(postCode);
        /*不等于null就存在发送的粘性事件*/
        return stickyEvent;
    }

    private void saveEventToMap(Map<String, Map<String, List<InterBean>>> mapEvent, String postCode, String registerCode, InterBean interBean) {
        synchronized (objects) {
            Map<String, List<InterBean>> integerListMap = mapEvent.get(postCode);
            if (integerListMap == null) {
                integerListMap = new LinkedHashMap<>();
                List<InterBean> interBeans = new ArrayList<>();
                interBeans.add(interBean);
                integerListMap.put(registerCode, interBeans);

                mapEvent.put(postCode, integerListMap);
            } else {
                List<InterBean> interBeans = integerListMap.get(registerCode);
                if (interBeans == null) {
                    interBeans = new ArrayList<>();
                    integerListMap.put(registerCode, interBeans);
                }
                interBeans.add(interBean);
            }
        }
    }

    /****************************************************************************************/
    public void post(String postKey) {
        post(postKey, null);
    }

    public void post(String postKey, BusResult busResult) {
        post(postKey, postKey, busResult);
    }

    public void post(Object event) {
        post(event, null);
    }

    public void post(Object event, BusResult busResult) {
        if (event == null) {
            return;
        }
        String postKey = event.getClass().getName().hashCode() + "";
        post(postKey, event, busResult);
    }

    public void post(String postKey, Object event) {
        post(postKey, event, null);
    }

    public void post(String postKey, Object event, BusResult busResult) {
        if (TextUtils.isEmpty(postKey)) {
            return;
        }
        /*发送单一事件*/
        getSingleEventAndPost(postKey, event, busResult);
        /*发送普通事件*/
        getEventAndPost(postKey, event, mapEvent, busResult);
    }

    /****************************************************************************************/

    private void getSingleEventAndPost(String postKey, Object event, BusResult busResult) {
        if (singleEvent == null || singleEvent.isEmpty()) {
            return;
        }
        Map<String, InterBean> integerInterBeanMap = singleEvent.get(postKey);
        if (integerInterBeanMap == null || integerInterBeanMap.isEmpty()) {
            return;
        }
        for (InterBean bean : integerInterBeanMap.values()) {
            if (bean == null || bean.busCallback == null) {
                continue;
            }
            if (busResult == null) {
                busResult = new BusResult<Object>() {
                    @Override
                    public void result(Object obj) {

                    }
                };
            }
            bean.busCallback.accept(event, busResult);
        }
    }

    /****************************************************************************************/
    public void postSticky(String postKey) {
        postSticky(postKey, new BusResult<Object>() {
            @Override
            public void result(Object obj) {
            }
        });
    }

    public void postSticky(String postKey, BusResult busResult) {
        postSticky(postKey, postKey, busResult);
    }

    public void postSticky(Object event) {
        postSticky(event, null);
    }

    public void postSticky(Object event, BusResult busResult) {
        if (event == null) {
            return;
        }
        String postKey = event.getClass().getName().hashCode() + "";
        postSticky(postKey, event, busResult);
    }

    public void postSticky(String postKey, Object event) {
        postSticky(postKey, event, null);
    }

    public void postSticky(String postKey, Object event, BusResult busResult) {
        if (TextUtils.isEmpty(postKey)) {
            return;
        }
        InterBean interBean = new InterBean(postKey, "0", true, null);
        interBean.stickEventObj = event;
        interBean.busResult = busResult;
        stickyPostEvent.put(postKey, interBean);

        /*发送单一事件*/
        getSingleEventAndPost(postKey, event, busResult);
        /*发送粘性事件*/
        getEventAndPost(postKey, event, mapStickyEvent, busResult);
    }

    /****************************************************************************************/
    /**
     * 单一普通事件
     */
    public <T> void setSingleEvent(Object object, String postKey, BusCallback<T> busCallback) {
        setSingleEvent(object, postKey, true, busCallback);
    }

    public <T> void setSingleEvent(Object object, Class<T> clazz, BusCallback<T> busCallback) {
        setSingleEvent(object, clazz, true, busCallback);
    }

    public <T> void setSingleEvent(Object object, String postKey, boolean useLastEvent, BusCallback<T> busCallback) {
        saveSingleEvent(object, postKey, busCallback, useLastEvent);
    }

    public <T> void setSingleEvent(Object object, Class<T> clazz, boolean useLastEvent, BusCallback<T> busCallback) {
        if (object == null || clazz == null) {
            throw new IllegalStateException("setSingleEvent(object,class,busCallback), object or class can not null");
        }
        String postCode = clazz.getName().hashCode() + "";
        saveSingleEvent(object, postCode, busCallback, useLastEvent);
    }

    private <T> void saveSingleEvent(Object object, String postKey, BusCallback<T> busCallback, boolean useLastEvent) {
        if (busCallback == null) {
            return;
        }
        //如果有多次相同的object注册，只用最后注册的event，需要覆盖
        String registerCode = object.hashCode() + "";

        InterBean interBean = new InterBean(postKey, registerCode, false, busCallback);

        Map<String, InterBean> integerInterBeanMap = singleEvent.get(postKey);
        if (!useLastEvent && integerInterBeanMap != null && !integerInterBeanMap.isEmpty()) {
            //如果有多次相同的object注册，只用最开始注册的event，则不覆盖添加
            return;
        }
        if (integerInterBeanMap == null) {
            integerInterBeanMap = new ConcurrentHashMap<>(1);
            singleEvent.put(postKey, integerInterBeanMap);
        }
        integerInterBeanMap.put(registerCode, interBean);

        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        addEventToRegisterGroup(registerCode, interBean);
    }


    public void removeStickyEvent(Object event) {
        if (event == null) {
            return;
        }
        String postCode = event.getClass().getName().hashCode() + "";
        removeSticky(postCode);
    }

    public void removeStickyEvent(Class clazz) {
        if (clazz == null) {
            return;
        }
        String postCode = clazz.getName().hashCode() + "";
        removeSticky(postCode);
    }

    private void removeSticky(String postCode) {
        if (stickyPostEvent == null || stickyPostEvent.isEmpty()) {
            return;
        }
        if (TextUtils.isEmpty(postCode)) {
            return;
        }
        stickyPostEvent.remove(postCode);
    }

    public void removeAllStickyEvent() {
        if (stickyPostEvent == null || stickyPostEvent.isEmpty()) {
            return;
        }
        stickyPostEvent.clear();
    }

    private void getEventAndPost(String postKey, Object event, Map<String, Map<String, List<InterBean>>> mapEvent, BusResult busResult) {
        if (event == null || mapEvent == null || mapEvent.size() == 0) {
            return;
        }
        for (String integer : mapEvent.keySet()) {
            if (!TextUtils.equals(integer, postKey)) {
                continue;
            }
            Map<String, List<InterBean>> integerListMap = mapEvent.get(integer);
            if (integerListMap == null) {
                continue;
            }
            for (List<InterBean> value : integerListMap.values()) {
                if (value == null || value.isEmpty()) {
                    continue;
                }
                for (InterBean bean : value) {
                    if (bean == null || bean.busCallback == null) {
                        continue;
                    }
                    if (busResult == null) {
                        busResult = new BusResult() {
                            @Override
                            public void result(Object obj) {

                            }
                        };
                    }
                    bean.busCallback.accept(event, busResult);
                }
            }
        }
    }

    /*取消某个对象下的事件*/
    public void unSubscribe(Object object) {
        if (object == null) {
            return;
        }
        if (needRemoveEvent == null || needRemoveEvent.size() == 0) {
            return;
        }
        String registerCode = object.hashCode() + "";
        List<InterBean> interBeans = needRemoveEvent.remove(registerCode);
        if (interBeans == null || interBeans.isEmpty()) {
            return;
        }
        /*获取注册到某个object下的event*/
        for (InterBean bean : interBeans) {
            /*移除单一事件*/
            removeSingleEvent(registerCode, bean.postKey);
            /*移除其他事件*/
            removeEvent(registerCode, bean);
            /*移除临时保存的粘性事件对象*/
            removeSticky(bean.postKey);
        }
        interBeans.clear();
    }

    /*取消所有订阅的事件+移除所有粘性事件*/
    public void unSubscribeAll() {
        if (needRemoveEvent == null || needRemoveEvent.size() == 0) {
            return;
        }
        for (List<InterBean> item : needRemoveEvent.values()) {
            if (item == null || item.isEmpty()) {
                continue;
            }
            for (InterBean bean : item) {
                /*移除单一事件*/
                removeSingleEvent(REMOVE_ALL_FLAG, bean.postKey);
                /*移除其他事件*/
                removeEvent(REMOVE_ALL_FLAG, bean);
                /*移除临时保存的粘性事件对象*/
                removeAllStickyEvent();
            }
        }
        needRemoveEvent.clear();
    }

    /*移除普通事件和粘性事件*/
    private void removeEvent(String unSubscribeCode, InterBean bean) {
        if (bean == null) {
            return;
        }
        if (TextUtils.isEmpty(unSubscribeCode)) {
            return;
        }
        boolean isSticky = bean.isStickyEvent;
        Map<String, List<InterBean>> integerListMap;
        if (isSticky) {
            integerListMap = mapStickyEvent.get(bean.postKey);
        } else {
            integerListMap = mapEvent.get(bean.postKey);
        }
        if (integerListMap == null || integerListMap.isEmpty()) {
            return;
        }
        if (unSubscribeCode == REMOVE_ALL_FLAG) {
            for (List<InterBean> lastItem : integerListMap.values()) {
                if (lastItem == null || lastItem.isEmpty()) {
                    continue;
                }
                lastItem.clear();
            }
            integerListMap.clear();
            return;
        }
        List<InterBean> remove = integerListMap.remove(unSubscribeCode);
        if (remove != null) {
            remove.clear();
        }
    }


    /*移除单一事件*/
    private void removeSingleEvent(String registerCode, String postCode) {
        if (TextUtils.isEmpty(registerCode)) {
            return;
        }
        if (singleEvent == null || singleEvent.isEmpty()) {
            return;
        }
        Map<String, InterBean> integerListMap = singleEvent.get(postCode);
        if (integerListMap != null && !integerListMap.isEmpty()) {
            integerListMap.remove(registerCode);
        }
        if (REMOVE_ALL_FLAG.equals(registerCode)) {
            singleEvent.clear();
        }
    }
}
