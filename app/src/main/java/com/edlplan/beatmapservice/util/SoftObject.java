package com.edlplan.beatmapservice.util;

import java.lang.ref.SoftReference;

public abstract class SoftObject<T> {

    SoftReference<T> ref;

    public SoftObject() {
        get();
    }

    public T get() {
        if (ref == null || ref.get() == null) {
            ref = new SoftReference<>(load());
            return ref.get();
        } else {
            return ref.get();
        }
    }

    protected abstract T load();

    public static <T> SoftObject<T> create(Loader<T> l) {
        return new SoftObject<T>() {
            @Override
            protected T load() {
                return l.load();
            }
        };
    }

    @FunctionalInterface
    public interface Loader<T> {
        T load();
    }

}
