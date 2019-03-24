package com.edlplan.audiov.core.utils;

@FunctionalInterface
public interface Consumer<T> {
    void consume(T t);
}
