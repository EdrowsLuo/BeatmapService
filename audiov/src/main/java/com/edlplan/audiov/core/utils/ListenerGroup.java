package com.edlplan.audiov.core.utils;

import java.util.LinkedList;
import java.util.List;

public abstract class ListenerGroup<T> {

    private List<T> listeners = new LinkedList<>();

    /**
     * @param consumer 通过这个函数来代替apply时做的事
     */
    public static <T> ListenerGroup<T> create(Consumer<T> consumer) {
        return new ListenerGroup<T>() {
            @Override
            protected void apply(T t) {
                consumer.consume(t);
            }
        };
    }

    public void handle() {
        for (T t : listeners) {
            apply(t);
        }
    }

    public void register(T t) {
        if (!listeners.contains(t)) {
            listeners.add(t);
        }
    }

    public void unregiser(T t) {
        if (listeners.contains(t)) {
            listeners.remove(t);
        }
    }

    /**
     * 触发一个监听器
     */
    protected abstract void apply(T t);
}
