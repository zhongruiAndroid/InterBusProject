package com.github.interbus;

/***
 *   created by android on 2019/4/8
 */
public interface BusCallback<T> {
    void accept(T event);
}
