package com.github.interbus;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.SparseArrayCompat;
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
    private SparseArrayCompat<SparseArrayCompat<BusCallback>> sparseEvent;
    private SparseArrayCompat<SparseArrayCompat<BusCallback>> sparseStickyEvent;

    private SparseArrayCompat  stickyBean;

    private SparseArrayCompat<Set<InterBean>> interBeanSetSparse;

    private InterBus() {
        sparseEvent = new SparseArrayCompat();
        sparseStickyEvent = new SparseArrayCompat();
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

    public <T> InterBean setEvent(Class<T> clazz, BusCallback<T> busCallback) {
        return setTheEvent(clazz, sparseEvent, busCallback);
    }

    public <T> InterBean setEventSticky(Class<T> clazz, BusCallback<T> busCallback) {
        int postKey = clazz.getName().hashCode();
        if (stickyBean != null && stickyBean.size() > 0 && stickyBean.get(postKey) != null) {
            T obj = (T) stickyBean.get(postKey);
            if (busCallback != null) {
                busCallback.accept(obj);
            }
        }
        InterBean interBean = setTheEvent(clazz, sparseStickyEvent, busCallback);
        interBean.isStickyEvent = true;
        return interBean;
    }

    private <T> InterBean setTheEvent(Class<T> clazz, SparseArrayCompat<SparseArrayCompat<BusCallback>> callbackSparse, BusCallback<T> busCallback) {
        String className = clazz.getName();
        int setKey = (className + System.currentTimeMillis()).hashCode();
        int postKey = className.hashCode();

        SparseArrayCompat<BusCallback> sparseArray = callbackSparse.get(postKey);
        if (sparseArray == null) {
            SparseArrayCompat<BusCallback> postSpare = new SparseArrayCompat<>();
            postSpare.put(setKey, busCallback);
            callbackSparse.put(postKey, postSpare);
        } else {
            sparseArray.put(setKey, busCallback);
        }
        return new InterBean(setKey, postKey, false);
    }


    public void post(Object event) {
        postEvent(event, sparseEvent);
    }

    public void postSticky(Object event) {
        int postKey = event.getClass().getName().hashCode();
        if (stickyBean == null) {
            stickyBean = new SparseArrayCompat<>();
        }
        stickyBean.put(postKey, event);
        postEvent(event, sparseStickyEvent);
    }

    private void postEvent(Object event, SparseArrayCompat<SparseArrayCompat<BusCallback>> eventSparse) {
        int postKey = event.getClass().getName().hashCode();
        if (eventSparse == null || eventSparse.size() == 0) {
            return;
        }
        SparseArrayCompat<BusCallback> busCallbackSparseArray = eventSparse.get(postKey);
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
        if (sparseEvent == null || sparseEvent.size() == 0) {
            return;
        }
        SparseArrayCompat<BusCallback> busCallbackSparseArray = sparseEvent.get(interBean.postKey);
        if (busCallbackSparseArray == null || busCallbackSparseArray.size() == 0) {
            return;
        }
        busCallbackSparseArray.remove(interBean.setKey);
    }

    private void removeStickyEvent(InterBean interBean) {
        if (sparseStickyEvent == null || sparseStickyEvent.size() == 0) {
            return;
        }
        SparseArrayCompat<BusCallback> busCallbackSparseArray = sparseStickyEvent.get(interBean.postKey);
        if (busCallbackSparseArray == null || busCallbackSparseArray.size() == 0) {
            return;
        }
        busCallbackSparseArray.remove(interBean.setKey);
    }


    public void addSubscribe(Activity activity, InterBean bean) {
        getSet(activity).add(bean);
    }
    public void addSubscribe(Activity activity, Set<InterBean> bean) {
        getSet(activity).addAll(bean);
    }

    public void addSubscribe(Fragment fragment, Set<InterBean> bean) {
        if (fragment == null) {
            new IllegalStateException("addSubscribe(fragment) fragment is null");
        }
        addSubscribe(fragment.getActivity(), bean);
    }
    public void addSubscribe(Fragment fragment, InterBean bean) {
        if (fragment == null) {
            new IllegalStateException("addSubscribe(fragment) fragment is null");
        }
        addSubscribe(fragment.getActivity(), bean);
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
        unSubscribe(fragment.getActivity());
    }


}
