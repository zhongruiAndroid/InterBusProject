package com.github.interbus;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 *   created by android on 2019/4/8
 */
public class InterBus {
    private static InterBus bus;
    /*<postKey,<setKey>>*/
    private SparseArray<SparseArray<BusCallback>> sparseEvent;
    private SparseArray<SparseArray<BusCallback>> sparseStickyEvent;

    private SparseArray<List> stickyBean;

    private InterBus() {
        sparseEvent=new SparseArray();
        sparseStickyEvent=new SparseArray();
    }
    public static InterBus get(){
        if (bus == null) {
            synchronized (InterBus.class){
                if (bus == null) {
                    bus=new InterBus();
                }
            }
        }
        return bus;
    }

    public <T>InterBean setEvent(Class<T> clazz, BusCallback<T> busCallback){
        return setTheEvent(clazz, sparseEvent, busCallback);
    }
    public <T>InterBean setEventSticky(Class<T> clazz, BusCallback<T> busCallback){
        return setEventSticky(clazz,false,busCallback);
    }
    public <T>InterBean setEventStickyLast(Class<T> clazz, BusCallback<T> busCallback){
        return setEventSticky(clazz,true,busCallback);
    }
    public <T>InterBean setEventSticky(Class<T> clazz,boolean sameStickyLastEvent, BusCallback<T> busCallback){
        int postKey = clazz.getName().hashCode();
        if(stickyBean!=null&&stickyBean.size()>0&&stickyBean.get(postKey)!=null){
            if(sameStickyLastEvent){
                List<T> list = stickyBean.get(postKey);
                T obj = list.get(list.size() - 1);
                if(busCallback!=null){
                    busCallback.accept(obj);
                }
            }else{
                List<T> list = stickyBean.get(postKey);
                int size=list.size();
                for (int i = 0; i <size; i++) {
                    if(busCallback!=null){
                        busCallback.accept(list.get(i));
                    }
                }
            }
        }
        InterBean interBean = setTheEvent(clazz, sparseStickyEvent, busCallback);
        interBean.isStickyEvent=true;
        return interBean;
    }
    private <T>InterBean setTheEvent(Class<T> clazz,SparseArray<SparseArray<BusCallback>> callbackSparse, BusCallback<T> busCallback){
        String className=clazz.getName();
        int setKey = (className+System.currentTimeMillis()).hashCode();
        int postKey = className.hashCode();

        SparseArray<BusCallback> sparseArray = callbackSparse.get(postKey);
        if(sparseArray==null){
            SparseArray<BusCallback> postSpare=new SparseArray<>();
            postSpare.put(setKey,busCallback);
            callbackSparse.put(postKey,postSpare);
        }else{
            sparseArray.put(setKey,busCallback);
        }
        return new InterBean(setKey,postKey,false);
    }


    public void post(Object event){
        postEvent(event,sparseEvent);
    }
    public void postSticky(Object event){
        int postKey=event.getClass().getName().hashCode();
        if(stickyBean==null){
            stickyBean=new SparseArray<>();
        }
        if(stickyBean.get(postKey)==null){
            List<Object> eventList = new ArrayList<>();
            eventList.add(event);
            stickyBean.put(postKey,eventList);
        }else{
            stickyBean.get(postKey).add(event);
        }
        postEvent(event,sparseStickyEvent);
    }
    private void postEvent(Object event,SparseArray<SparseArray<BusCallback>> eventSparse) {
        int postKey=event.getClass().getName().hashCode();
        if(eventSparse==null||eventSparse.size()==0){
            return;
        }
        SparseArray<BusCallback> busCallbackSparseArray = eventSparse.get(postKey);
        if(busCallbackSparseArray==null){
            return;
        }
        for (int i = 0,size=busCallbackSparseArray.size(); i <size ; i++) {
            BusCallback callback = busCallbackSparseArray.valueAt(i);
            if(callback!=null){
                callback.accept(event);
            }
        }
    }

    public void remove(Set<InterBean> interBeanSet){
        if(interBeanSet==null||interBeanSet.size()==0){
            return;
        }
        for (InterBean bean:interBeanSet) {
            remove(bean);
        }
    }
    public void remove(InterBean interBean){
        if(interBean==null){
            return;
        }
        if(interBean.isStickyEvent){
            removeStickyEvent(interBean);
        }else{
            removeEvent(interBean);
        }
    }
    private void removeEvent(InterBean interBean){
        if(sparseEvent==null||sparseEvent.size()==0){
            return;
        }
        SparseArray<BusCallback> busCallbackSparseArray = sparseEvent.get(interBean.postKey);
        if(busCallbackSparseArray==null||busCallbackSparseArray.size()==0){
            return;
        }
        busCallbackSparseArray.remove(interBean.setKey);
    }
    private void removeStickyEvent(InterBean interBean){
        if(sparseStickyEvent==null||sparseStickyEvent.size()==0){
            return;
        }
        SparseArray<BusCallback> busCallbackSparseArray = sparseStickyEvent.get(interBean.postKey);
        if(busCallbackSparseArray==null||busCallbackSparseArray.size()==0){
            return;
        }
        busCallbackSparseArray.remove(interBean.setKey);
    }

}
