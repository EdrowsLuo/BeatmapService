package com.edlplan.beatmapservice.util;

import com.edlplan.beatmapservice.Util;

public abstract class Collector<T, V> implements Updatable<T> {

    protected V value;

    public Collector(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public static <T> Collector<T, Integer> min(int iniValue, Function<T, Integer> getter) {
        return new Collector<T, Integer>(iniValue) {
            @Override
            public void update(T t) {
                int v = getter.fun(t);
                if (v < value) {
                    value = v;
                }
            }
        };
    }

    public static <T> Collector<T, Integer> max(int iniValue, Function<T, Integer> getter) {
        return new Collector<T, Integer>(iniValue) {
            @Override
            public void update(T t) {
                int v = getter.fun(t);
                if (v > value) {
                    value = v;
                }
            }
        };
    }

    public static <T> Collector<T, Double> min(double iniValue, Function<T, Double> getter) {
        return new Collector<T, Double>(iniValue) {
            @Override
            public void update(T t) {
                double v = getter.fun(t);
                if (v < value) {
                    value = v;
                }
            }
        };
    }

    public static <T> Collector<T, Double> max(double iniValue, Function<T, Double> getter) {
        return new Collector<T, Double>(iniValue) {
            @Override
            public void update(T t) {
                double v = getter.fun(t);
                if (v > value) {
                    value = v;
                }
            }
        };
    }

    public static <T, K extends Number> Collector<T, Double> avg(Function<T, K> getter) {
        return new Collector<T, Double>(0.0) {

            int count = 0;

            @Override
            public void update(T t) {
                value += Util.toDouble(getter.fun(t));
                count++;
            }

            @Override
            public Double getValue() {
                return count == 0 ? 0 : value / count;
            }
        };
    }

    public static <T> Updatable<T> bind(Collector<T, ?>... collectors) {
        return t -> {
            for (Collector<T, ?> c : collectors) {
                c.update(t);
            }
        };
    }

}
