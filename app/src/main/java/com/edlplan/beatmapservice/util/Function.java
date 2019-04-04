package com.edlplan.beatmapservice.util;

@FunctionalInterface
public interface Function<T, K> {
    K fun(T t);
}
