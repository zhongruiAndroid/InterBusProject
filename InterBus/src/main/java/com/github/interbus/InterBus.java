package com.github.interbus;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 *   created by android on 2019/4/8
 */
public class InterBus {
    private static InterBus bus;

    /*
     * postkey---act---list<interbean>
     * 以post为键，将event保存到对应register的map下面,方便post发消息
     * */
    private Map<Integer, Map<Integer, List<InterBean>>> mapEvent;

    private Map<Integer, Map<Integer, List<InterBean>>> mapStickyEvent;
    /*
     * act---list<interbean>
     *  以register为键，将event保存到list里面，方便后续根据register移除
     * */
    private Map<Integer, List<InterBean>> needRemoveEvent;


    private InterBus() {
        mapEvent = new ConcurrentHashMap<>();
        mapStickyEvent = new ConcurrentHashMap();

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

        InterBean interBean = new InterBean(postCode, isSticky, busCallback);

        if (isSticky) {
            saveEventToMap(mapStickyEvent, postCode, registerCode, interBean);
        } else {
            saveEventToMap(mapEvent, postCode, registerCode, interBean);
        }

        /*将interbean也添加到这个容器中，方便unRegister时根据registerCode和postCode去移除*/
        List<InterBean> interBeans = needRemoveEvent.get(registerCode);
        if (interBeans == null) {
            interBeans = new ArrayList<>();
            needRemoveEvent.put(registerCode,interBeans);
        }
        interBeans.add(interBean);
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
            integerListMap.get(registerCode).add(interBean);
        }
    }

    public void post(Object event) {
        /*发送普通事件*/
        getEventAndPost(event, mapEvent);
    }

    public void postSticky(Object event) {
        /*发送粘性事件*/
        getEventAndPost(event, mapStickyEvent);
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

    public void remove(Set<InterBean> interBeanSet) {
        if (interBeanSet == null || interBeanSet.size() == 0) {
            return;
        }
        for (InterBean bean : interBeanSet) {
            remove(bean);
        }
    }

    public void remove(InterBean interBean) {
        if (interBean == null) {
            return;
        }
        if (interBean.isStickyEvent) {
            removeStickyEvent(interBean);
        } else {
            removeEvent(interBean);
        }
    }

    private void removeEvent(InterBean interBean) {
        if (mapEvent == null || mapEvent.size() == 0) {
            return;
        }

    }

    private void removeStickyEvent(InterBean interBean) {
        if (mapStickyEvent == null || mapStickyEvent.size() == 0) {
            return;
        }

    }


    public void unSubscribe(Object object) {
        if (object == null) {
            return;
        }
    }
}
