package com.github.interbus;

import android.text.TextUtils;
import android.view.View;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 *   created by android on 2019/4/8
 */
public class InterBus {
    private final String REMOVE_ALL_FLAG = "-100";
    private static InterBus bus;
    /*
     * 第一个key为postCode(方便根据postCode取event,发消息)
     * */
    /*普通消息event容器*/
    private Map<String, CopyOnWriteArrayList<InterBean>> mapEvent;
    /*粘性消息event容器*/
//    private Map<String, CopyOnWriteArrayList<InterBean>> mapStickyEvent;
    /*单一的消息，key为postCode只保存注册的最后一个(或者最开始的一个)事件消息*/
    private Map<String, InterBean> singleEvent;
    /*先保存发送的粘性事件,key为postCode*/
    private Map<String, InterBean> stickyPostEvent;
    /*
     *  key为registerCode，将event保存到list里面，方便后续取消注册时根据registerCode和postCode移除
     * */
    private Map<String, CopyOnWriteArrayList<InterBean>> needRemoveEvent;


    private InterBus() {
        mapEvent = new ConcurrentHashMap<>();
//        mapStickyEvent = new ConcurrentHashMap();
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
/*
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
*/

    private <T> void saveEvent(Object object, String postCode, BusCallback<T> busCallback, boolean isSticky) {
        if (busCallback == null) {
            return;
        }
        String registerCode = object.hashCode() + "";

        InterBean interBean = new InterBean(postCode, registerCode, isSticky, busCallback);

        /*检查是否已经发送过粘性事件*/
        InterBean hasEvent = checkHasEvent(postCode);
        if (hasEvent != null) {
            busCallback.accept((T) hasEvent.stickEventObj, hasEvent.busResult);
        }

        /*if (isSticky) {
            *//*检查是否已经发送过粘性事件*//*
            InterBean hasEvent = checkHasEvent(postCode);
            if (hasEvent != null) {
                busCallback.accept((T) hasEvent.stickEventObj, hasEvent.busResult);
            }
            saveEventToMap(mapStickyEvent, postCode, registerCode, interBean);
        } else {
            saveEventToMap(mapEvent, postCode, registerCode, interBean);
        }*/
        saveEventToMap(mapEvent, postCode, registerCode, interBean);

        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        addEventToRegisterGroup(registerCode, interBean);
    }

    private void addEventToRegisterGroup(String registerCode, InterBean interBean) {
        if (TextUtils.isEmpty(registerCode) || interBean == null) {
            return;
        }
        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        CopyOnWriteArrayList<InterBean> interBeans = needRemoveEvent.get(registerCode);
        if (interBeans == null) {
            interBeans = new CopyOnWriteArrayList<>();
            needRemoveEvent.put(registerCode, interBeans);
        }
        interBeans.add(interBean);
    }

    private InterBean checkHasEvent(String postCode) {
        if (stickyPostEvent == null || stickyPostEvent.isEmpty()) {
            return null;
        }
        InterBean stickyEvent = stickyPostEvent.get(postCode);
        /*不等于null就存在发送的粘性事件*/
        return stickyEvent;
    }

    private void saveEventToMap(Map<String, CopyOnWriteArrayList<InterBean>> mapEvent, String postCode, String registerCode, InterBean interBean) {
        CopyOnWriteArrayList<InterBean> onWriteArrayList = mapEvent.get(postCode);
        if (onWriteArrayList == null) {
            onWriteArrayList = new CopyOnWriteArrayList<>();
            mapEvent.put(postCode, onWriteArrayList);
        }
        onWriteArrayList.add(interBean);
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
        InterBean interBean = singleEvent.get(postKey);
        if (interBean == null || interBean.busCallback == null) {
            return;
        }
        if (busResult == null) {
            busResult = new BusResult<Object>() {
                @Override
                public void result(Object obj) {

                }
            };
        }
        interBean.busCallback.accept(event, busResult);
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
//        getEventAndPost(postKey, event, mapStickyEvent, busResult);
        getEventAndPost(postKey, event, mapEvent, busResult);
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

        InterBean integerInterBeanMap = singleEvent.get(postKey);
        if (!useLastEvent && integerInterBeanMap != null) {
            //如果有多次相同的object注册，只用最开始注册的event，则不覆盖添加
            return;
        }
        InterBean interBean = new InterBean(postKey, registerCode, false, busCallback);
        singleEvent.put(postKey, interBean);

        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        addEventToRegisterGroup(registerCode, interBean);
    }


    public void removeStickyEvent(Object event) {
        if (event == null) {
            return;
        }
        String postCode = event.getClass().getName().hashCode() + "";
        removeStickyEvent(postCode);
    }

    public void removeStickyEvent(Class clazz) {
        if (clazz == null) {
            return;
        }
        String postCode = clazz.getName().hashCode() + "";
        removeStickyEvent(postCode);
    }

    private void removeStickyEvent(String postCode) {
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

    private void getEventAndPost(String postKey, Object event, Map<String, CopyOnWriteArrayList<InterBean>> mapEvent, BusResult busResult) {
        if (event == null || mapEvent == null || mapEvent.size() == 0) {
            return;
        }
        Set<String> strings = mapEvent.keySet();
        if (strings == null) {
            return;
        }
        Iterator<String> iterator = strings.iterator();
        if (iterator == null) {
            return;
        }
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!TextUtils.equals(key, postKey)) {
                continue;
            }
            CopyOnWriteArrayList<InterBean> interBeans = mapEvent.get(key);
            if (interBeans == null || interBeans.isEmpty()) {
                continue;
            }
            Iterator<InterBean> interBeanIterator = interBeans.iterator();
            while (interBeanIterator.hasNext()) {
                InterBean bean = interBeanIterator.next();
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

    /*取消某个对象下的事件*/
    public void unSubscribe(Object object) {
        if (object == null) {
            return;
        }
        if (needRemoveEvent == null || needRemoveEvent.size() == 0) {
            return;
        }
        String registerCode = object.hashCode() + "";
        CopyOnWriteArrayList<InterBean> interBeans = needRemoveEvent.remove(registerCode);
        if (interBeans == null || interBeans.isEmpty()) {
            return;
        }
        /*获取注册到某个object下的event*/
        Iterator<InterBean> iterator = interBeans.iterator();
        while (iterator.hasNext()) {
            InterBean bean = iterator.next();
            if(bean==null){
                continue;
            }
            /*移除单一事件*/
            removeSingleEvent(registerCode, bean.postKey);
            /*移除其他事件*/
            removeEvent(registerCode, bean);
            /*移除临时保存的粘性事件对象*/
//            removeStickyEvent(bean.postKey);
        }

        interBeans.clear();
    }

    /*取消所有订阅的事件+移除所有粘性事件*/
    public void unSubscribeAll() {
        if (needRemoveEvent == null || needRemoveEvent.size() == 0) {
            return;
        }
        Iterator<CopyOnWriteArrayList<InterBean>> iterator = needRemoveEvent.values().iterator();
        while (iterator.hasNext()) {
            CopyOnWriteArrayList<InterBean> item = iterator.next();
            if (item == null || item.isEmpty()) {
                continue;
            }
            Iterator<InterBean> interBeanIterator = item.iterator();
            while (interBeanIterator.hasNext()) {
                InterBean bean = interBeanIterator.next();
                if(bean==null){
                    continue;
                }
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
    private void removeEvent(String registerCode, InterBean bean) {
        if (bean == null) {
            return;
        }
        if (TextUtils.isEmpty(registerCode)) {
            return;
        }
        boolean isSticky = bean.isStickyEvent;
        CopyOnWriteArrayList<InterBean> integerListMap;
        if (isSticky) {
//            integerListMap = mapStickyEvent.get(bean.postKey);
        } else {
//            integerListMap = mapEvent.get(bean.postKey);
        }
        integerListMap = mapEvent.get(bean.postKey);
        if (integerListMap == null || integerListMap.isEmpty()) {
            return;
        }
        if (registerCode.equals(REMOVE_ALL_FLAG)) {
            integerListMap.clear();
            return;
        }
        Iterator<InterBean> iterator = integerListMap.iterator();
        while (iterator.hasNext()) {
            InterBean next = iterator.next();
            if (next == null) {
                continue;
            }
            if (TextUtils.equals(next.registerCode, registerCode)) {
                integerListMap.remove(next);
            }
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
        if (REMOVE_ALL_FLAG.equals(registerCode)) {
            singleEvent.clear();
            return;
        }
        singleEvent.remove(postCode);
    }
}
