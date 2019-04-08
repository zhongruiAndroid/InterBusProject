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
        String className=clazz.getName();
        int setKey = (className+System.currentTimeMillis()).hashCode();
        int postKey = className.hashCode();

        SparseArray<BusCallback> sparseArray = sparseEvent.get(postKey);
        if(sparseArray==null){
            SparseArray<BusCallback> postSpare=new SparseArray<>();
            postSpare.put(setKey,busCallback);
            sparseEvent.put(postKey,postSpare);
        }else{
            sparseArray.put(setKey,busCallback);
        }
        return new InterBean(setKey,postKey,false);
    }

    public void setStickyEvent(){

    }

    public void post(Object event){
        int postKey=event.getClass().getName().hashCode();
        if(sparseEvent==null||sparseEvent.size()==0){
            return;
        }
        SparseArray<BusCallback> busCallbackSparseArray = sparseEvent.get(postKey);
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
    public void postSticky(Class clazz){

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
