package com.github.interbus;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 *   created by android on 2019/4/8
 */
public class InterBus {
    private static InterBus bus;
    /*<postKey,<setKey>>*/
    private Map<Integer, Map<Integer, BusCallback>> mapEvent;
    private Map<Integer, Map<Integer, BusCallback>> mapStickyEvent;
    private Map<Integer, Set<InterBean>> interBean;


    private SparseArrayCompat<BusCallback> sparseSingleEvent;

    private SparseArrayCompat  stickyBean;

    private SparseArrayCompat<Set<InterBean>> interBeanSetSparse;

    private InterBus() {
        mapEvent = new ConcurrentHashMap<>();
        mapStickyEvent = new ConcurrentHashMap();

        interBean = new ConcurrentHashMap();

        sparseSingleEvent = new SparseArrayCompat();
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

    //region   普通事件  -----------------------------------------

    public <T>void setEvent(Object object,Class<T> clazz, BusCallback<T> busCallback) {
        if(object==null){
            throw new IllegalStateException("setEvent(object), object can not null");
        }
        String className = clazz.getName();
        int setKey = (className + System.currentTimeMillis()).hashCode();
        int postKey = className.hashCode();
        Map<Integer, BusCallback> sparseArray = callbackSparse.get(postKey);
        if (sparseArray == null) {
            Map<Integer, BusCallback> postSpare = new ConcurrentHashMap<>();
            postSpare.put(setKey, busCallback);
            callbackSparse.put(postKey, postSpare);
        } else {
            sparseArray.put(setKey, busCallback);
        }
        InterBean interBean = new InterBean(setKey, postKey, false);
        getSet(fragment).add(interBean);
    }
    public <T> void setEvent(Activity activity,Class<T> clazz, BusCallback<T> busCallback) {
        if(activity==null){
            throw new IllegalStateException("setEvent(activity), activity can not null");
        }
        InterBean interBean = setEvent(clazz,busCallback);
        addSubscribe(activity,interBean);
    }
    @Deprecated
    public <T> InterBean setEvent(Class<T> clazz, BusCallback<T> busCallback) {
        return setTheEvent(clazz, mapEvent, busCallback);
    }
    //endregion


    //region   黏性事件  -----------------------------------------

    public <T>void setEventSticky(Fragment fragment,Class<T> clazz, BusCallback<T> busCallback) {
        if(fragment==null){
            throw new IllegalStateException("setEventSticky(fragment), fragment can not null");
        }
        InterBean interBean = setEventSticky(clazz, busCallback);
        addSubscribe(fragment,interBean);
    }
    public <T>void setEventSticky(Activity activity,Class<T> clazz, BusCallback<T> busCallback) {
        if(activity==null){
            throw new IllegalStateException("setEventSticky(activity), activity can not null");
        }
        InterBean interBean = setEventSticky(clazz, busCallback);
        addSubscribe(activity,interBean);
    }
    @Deprecated
    public <T> InterBean setEventSticky(Class<T> clazz, BusCallback<T> busCallback) {
        int postKey = clazz.getName().hashCode();
        if (stickyBean != null && stickyBean.size() > 0 && stickyBean.get(postKey) != null) {
            T obj = (T) stickyBean.get(postKey);
            if (busCallback != null) {
                busCallback.accept(obj);
            }
        }
        InterBean interBean = setTheEvent(clazz, mapStickyEvent, busCallback);
        interBean.isStickyEvent = true;
        return interBean;
    }
    //endregion
    private <T> InterBean setTheEvent(Class<T> clazz, Map<Integer, Map<Integer, BusCallback>> callbackSparse, BusCallback<T> busCallback) {
        String className = clazz.getName();
        int setKey = (className + System.currentTimeMillis()).hashCode();
        int postKey = className.hashCode();
        Map<Integer, BusCallback> sparseArray = callbackSparse.get(postKey);
        if (sparseArray == null) {
            Map<Integer, BusCallback> postSpare = new ConcurrentHashMap<>();
            postSpare.put(setKey, busCallback);
            callbackSparse.put(postKey, postSpare);
        } else {
            sparseArray.put(setKey, busCallback);
        }
        return new InterBean(setKey, postKey, false);
    }


    //region   单一事件  -----------------------------------------

    public <T>void setEventSingle(Fragment fragment,Class<T> clazz, BusCallback<T> busCallback) {
        if(fragment==null){
            throw new IllegalStateException("setEvent(fragment), fragment can not null");
        }
        InterBean interBean = setEventSingle(clazz,busCallback);
        addSubscribe(fragment,interBean);
    }
    public <T> void setEventSingle(Activity activity,Class<T> clazz, BusCallback<T> busCallback) {
        if(activity==null){
            throw new IllegalStateException("setEvent(activity), activity can not null");
        }
        InterBean interBean = setEventSingle(clazz,busCallback);
        addSubscribe(activity,interBean);
    }
    public  <T> InterBean setEventSingle(Class<T> clazz, BusCallback<T> busCallback) {
        String className = clazz.getName();
        int setKey =className.hashCode();
        int postKey = setKey;
        sparseSingleEvent.put(setKey, busCallback);
        return new InterBean(setKey, postKey, false);
    }
    //endregion
    public void post(Object event) {
        if(event==null){
            return;
        }
        /*单一事件*/
        postEventSingle(event,sparseSingleEvent);

        /*普通事件*/
        postEvent(event, mapEvent);
    }

    public void postSticky(Object event) {
        if(event==null){
            return;
        }
        int postKey = event.getClass().getName().hashCode();
        if (stickyBean == null) {
            stickyBean = new SparseArrayCompat<>();
        }
        stickyBean.put(postKey, event);
        postEvent(event, mapStickyEvent);
    }

    private void postEventSingle(Object event, SparseArrayCompat<BusCallback> eventSparse) {
        if (eventSparse == null || eventSparse.size() == 0) {
            return;
        }
        int postKey = event.getClass().getName().hashCode();
        BusCallback callback = eventSparse.get(postKey);
        if(callback!=null){
            callback.accept(event);
        }
    }
    private void postEvent(Object event, Map<Integer, Map<Integer, BusCallback>> eventSparse) {
        if (eventSparse == null || eventSparse.size() == 0) {
            return;
        }

        int postKey = event.getClass().getName().hashCode();
        Map<Integer, BusCallback> busCallbackSparseArray = eventSparse.get(postKey);
        if (busCallbackSparseArray == null) {
            return;
        }
        for (int i = 0, size = busCallbackSparseArray.size(); i < size; i++) {
            BusCallback callback = busCallbackSparseArray.valueAt(i);
            if (callback != null) {
                callback.accept(event);
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
        SparseArrayCompat<BusCallback> busCallbackSparseArray = mapEvent.get(interBean.postKey);
        if (busCallbackSparseArray == null || busCallbackSparseArray.size() == 0) {
            return;
        }
        busCallbackSparseArray.remove(interBean.setKey);
    }

    private void removeStickyEvent(InterBean interBean) {
        if (mapStickyEvent == null || mapStickyEvent.size() == 0) {
            return;
        }
        SparseArrayCompat<BusCallback> busCallbackSparseArray = mapStickyEvent.get(interBean.postKey);
        if (busCallbackSparseArray == null || busCallbackSparseArray.size() == 0) {
            return;
        }
        busCallbackSparseArray.remove(interBean.setKey);
    }

    public void addSubscribe(Fragment fragment, InterBean bean) {
        getSet(fragment).add(bean);
    }
    public void addSubscribe(Fragment fragment, Set<InterBean> bean) {
        getSet(fragment).addAll(bean);
    }
    public void addSubscribe(Activity activity, InterBean bean) {
        getSet(activity).add(bean);
    }
    public void addSubscribe(Activity activity, Set<InterBean> bean) {
        getSet(activity).addAll(bean);
    }


    private Set getSet(Fragment fragment) {
        if (fragment == null) {
            new IllegalStateException("getSet(fragment) fragment is null");
        }
        int hashCode = fragment.getClass().getName().hashCode();
        return getSetForHashCode(hashCode);
    }
    private Set getSet(Activity activity) {
        if (activity == null) {
            new IllegalStateException("getSet(activity) activity is null");
        }
        int hashCode = activity.getClass().getName().hashCode();
        return getSetForHashCode(hashCode);
    }

    private Set getSetForHashCode(int hashCode) {
        if (interBeanSetSparse == null) {
            interBeanSetSparse = new SparseArrayCompat<>();
        }
        Set<InterBean> interBeanSet = interBeanSetSparse.get(hashCode);
        if (interBeanSet == null) {
            interBeanSet = new HashSet<>();
            interBeanSetSparse.put(hashCode, interBeanSet);
        }
        return interBeanSet;
    }

    private Set<InterBean> unSubscribeForHashCode(int hashCode) {
        if (interBeanSetSparse == null) {
            return null;
        }
        Set<InterBean> interBeanSet = interBeanSetSparse.get(hashCode);
        return interBeanSet;
    }

    public void unSubscribe(Activity activity) {
        if (activity == null) {
            new IllegalStateException("unSubscribe(activity) activity is null");
        }
        int hashCode = activity.getClass().getName().hashCode();
        Set<InterBean> interBeans = unSubscribeForHashCode(hashCode);
        if (interBeans != null) {
            remove(interBeans);
        }
    }

    public void unSubscribe(Fragment fragment) {
        if (fragment == null) {
            new IllegalStateException("unSubscribe(fragment) fragment is null");
        }
        int hashCode = fragment.getClass().getName().hashCode();
        Set<InterBean> interBeans = unSubscribeForHashCode(hashCode);
        if (interBeans != null) {
            remove(interBeans);
        }
    }


}
