package com.github.interbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 *   created by android on 2019/4/8
 */
public class InterBus {
    private final int REMOVE_ALL_CODE = -100;
    private static InterBus bus;

    /*
     * 第一个key为postCode(方便根据postCode取event,发消息),第二个key为registerCode
     * 因为可能存在不同act注册相同obj消息，所以第二个map以registercode为key,也方便后续的取消某个act里面的消息订阅
     * */
    /*普通消息event容器*/
    private Map<Integer, Map<Integer, List<InterBean>>> mapEvent;
    /*粘性消息event容器*/
    private Map<Integer, Map<Integer, List<InterBean>>> mapStickyEvent;
    /*单一的消息，只保存注册的最后一个(或者最开始的一个)事件消息*/
    private Map<Integer, InterBean> singleEvent;
    /*先保存发送的粘性事件,key为postCode*/
    private Map<Integer, InterBean> stickyPostEvent;
    /*
     *  key为registerCode，将event保存到list里面，方便后续取消注册时根据registerCode和postCode移除
     * */
    private Map<Integer, List<InterBean>> needRemoveEvent;


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
        saveEvent(object, clazz, busCallback, false);
    }

    /**
     * 粘性事件
     */
    public <T> void setStickyEvent(Object object, Class<T> clazz, BusCallback<T> busCallback) {
        if (object == null || clazz == null) {
            throw new IllegalStateException("setStickyEvent(object,class,busCallback), object or class can not null");
        }
        saveEvent(object, clazz, busCallback, true);
    }

    private <T> void saveEvent(Object object, Class<T> clazz, BusCallback<T> busCallback, boolean isSticky) {
        if (busCallback == null) {
            return;
        }
        int postCode = clazz.getName().hashCode();
        int registerCode = object.hashCode();

        InterBean interBean = new InterBean(postCode, registerCode, isSticky, busCallback);

        if (isSticky) {
            /*检查是否已经发送过粘性事件*/
            InterBean hasEvent = checkHasEvent(postCode);
            if (hasEvent != null) {
                busCallback.accept((T) hasEvent.stickEventObj);
            }
            saveEventToMap(mapStickyEvent, postCode, registerCode, interBean);
        } else {
            saveEventToMap(mapEvent, postCode, registerCode, interBean);
        }

        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        addEventToRegisterGroup(registerCode,interBean);
    }

    private void addEventToRegisterGroup(int registerCode,InterBean interBean) {
        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        List<InterBean> interBeans = needRemoveEvent.get(registerCode);
        if (interBeans == null) {
            interBeans = new ArrayList<>();
            needRemoveEvent.put(registerCode, interBeans);
        }
        interBeans.add(interBean);
    }

    private InterBean checkHasEvent(int postCode) {
        if (stickyPostEvent == null || stickyPostEvent.isEmpty()) {
            return null;
        }
        InterBean stickyEvent = stickyPostEvent.get(postCode);
        /*不等于null就存在发送的粘性事件*/
        return stickyEvent;
    }

    private void saveEventToMap(Map<Integer, Map<Integer, List<InterBean>>> mapEvent, int postCode, int registerCode, InterBean interBean) {
        Map<Integer, List<InterBean>> integerListMap = mapEvent.get(postCode);
        if (integerListMap == null) {
            integerListMap = new ConcurrentHashMap<>();
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

    public void post(Object event) {
        /*发送单一事件*/
        getSingleEventAndPost(event);
        /*发送普通事件*/
        getEventAndPost(event, mapEvent);
    }

    private void getSingleEventAndPost(Object event) {
        if(singleEvent==null||singleEvent.isEmpty()){
            return;
        }
        int postKey = event.getClass().getName().hashCode();
        InterBean bean = singleEvent.get(postKey);
        if (bean == null || bean.busCallback == null) {
            return;
        }
        bean.busCallback.accept(event);
    }

    public void postSticky(Object event) {
        int postCode = event.getClass().getName().hashCode();
        InterBean interBean = new InterBean(postCode, 0, true, null);
        interBean.stickEventObj = event;
        stickyPostEvent.put(postCode, interBean);

        /*发送单一事件*/
        getSingleEventAndPost(event);
        /*发送粘性事件*/
        getEventAndPost(event, mapStickyEvent);
    }

    /**
     * 单一普通事件
     */
    public <T> void setSingleEvent(Object object, Class<T> clazz, BusCallback<T> busCallback) {
        setSingleEvent(object, clazz, true, busCallback);
    }

    public <T> void setSingleEvent(Object object, Class<T> clazz, boolean useLastEvent, BusCallback<T> busCallback) {
        if (object == null || clazz == null) {
            throw new IllegalStateException("setSingleEvent(object,class,busCallback), object or class can not null");
        }
        saveSingleEvent(object, clazz, busCallback, useLastEvent, false);
    }

    /**
     * 单一粘性事件
     */
    public <T> void setSingleStickyEvent(Object object, Class<T> clazz, BusCallback<T> busCallback) {
        setSingleStickyEvent(object, clazz, true, busCallback);
    }

    public <T> void setSingleStickyEvent(Object object, Class<T> clazz, boolean useLastEvent, BusCallback<T> busCallback) {
        if (object == null || clazz == null) {
            throw new IllegalStateException("setSingleStickyEvent(object,class,busCallback), object or class can not null");
        }
        saveSingleEvent(object, clazz, busCallback, useLastEvent, true);
    }

    private <T> void saveSingleEvent(Object object, Class<T> clazz, BusCallback<T> busCallback, boolean useLastEvent, boolean isSticky) {
        if (busCallback == null) {
            return;
        }
        int postCode = clazz.getName().hashCode();
        if (!useLastEvent&&singleEvent.get(postCode)!=null) {
            //如果有多次相同的object注册，只用最开始注册的event，则不覆盖添加
            return;
        }

        //如果有多次相同的object注册，只用最后注册的event，需要覆盖
        int registerCode = object.hashCode();

        InterBean interBean = new InterBean(postCode, registerCode, isSticky, busCallback);

        if (isSticky) {
            /*检查是否已经发送过粘性事件*/
            InterBean hasEvent = checkHasEvent(postCode);
            if (hasEvent != null) {
                busCallback.accept((T) hasEvent.stickEventObj);
            }
        }
        singleEvent.put(postCode,interBean);


        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        addEventToRegisterGroup(registerCode,interBean);
    }

    public void removeSingleEvent(Object event) {
        if (event == null) {
            return;
        }
        int postCode = event.getClass().getName().hashCode();
        removeSingleEvent(postCode);
    }

    public void removeSingleEvent(Class clazz) {
        if (clazz == null) {
            return;
        }
        int postCode = clazz.getName().hashCode();
        removeSingleEvent(postCode);
    }


    public void removeStickyEvent(Object event) {
        if (event == null) {
            return;
        }
        int postCode = event.getClass().getName().hashCode();
        removeSticky(postCode);
    }

    public void removeStickyEvent(Class clazz) {
        if (clazz == null) {
            return;
        }
        int postCode = clazz.getName().hashCode();
        removeSticky(postCode);
    }

    private void removeSticky(int postCode) {
        if (stickyPostEvent == null || stickyPostEvent.isEmpty()) {
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

    private void getEventAndPost(Object event, Map<Integer, Map<Integer, List<InterBean>>> mapEvent) {
        if (event == null || mapEvent == null || mapEvent.size() == 0) {
            return;
        }
        int postKey = event.getClass().getName().hashCode();
        for (Integer integer : mapEvent.keySet()) {
            if (integer != postKey) {
                continue;
            }
            Map<Integer, List<InterBean>> integerListMap = mapEvent.get(integer);
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
                    bean.busCallback.accept(event);
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
        int registerCode = object.hashCode();
        List<InterBean> interBeans = needRemoveEvent.remove(registerCode);
        if (interBeans == null || interBeans.isEmpty()) {
            return;
        }
        /*获取注册到某个object下的event*/
        for (InterBean bean : interBeans) {
            /*移除单一事件*/
            removeSingleEvent(bean.postKey);
            /*移除其他事件*/
            removeEvent(registerCode, bean);
            /*移除临时保存的粘性事件对象*/
            removeSticky(bean.postKey);
        }
        interBeans.clear();
    }

    /*取消所有订阅事件+移除所有粘性事件*/
    public void unSubscribeAllAndSticky() {
        removeAllStickyEvent();
        unSubscribeAll();
    }

    /*取消所有订阅的事件*/
    public void unSubscribeAll() {
        if (needRemoveEvent == null || needRemoveEvent.size() == 0) {
            return;
        }
        for (List<InterBean> item : needRemoveEvent.values()) {
            if (item == null || item.isEmpty()) {
                continue;
            }
            for (InterBean bean : item) {
                removeSingleEvent(REMOVE_ALL_CODE);
                removeEvent(REMOVE_ALL_CODE, bean);
            }
        }
        needRemoveEvent.clear();
    }

    /*移除普通事件和粘性事件*/
    private void removeEvent(int unSubscribeCode, InterBean bean) {
        if (bean == null) {
            return;
        }
        boolean isSticky = bean.isStickyEvent;
        Map<Integer, List<InterBean>> integerListMap;
        if (isSticky) {
            integerListMap = mapStickyEvent.get(bean.postKey);
        } else {
            integerListMap = mapEvent.get(bean.postKey);
        }
        if (integerListMap == null || integerListMap.isEmpty()) {
            return;
        }
        if (unSubscribeCode == REMOVE_ALL_CODE) {
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
    private void removeSingleEvent(int postCode) {
        if (singleEvent == null || singleEvent.isEmpty()) {
            return;
        }
        if (postCode == REMOVE_ALL_CODE) {
            singleEvent.clear();
        } else {
            singleEvent.remove(postCode);
        }
    }
}
