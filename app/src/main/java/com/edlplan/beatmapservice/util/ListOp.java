package com.edlplan.beatmapservice.util;

import com.edlplan.beatmapservice.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ListOp<T> {

    private List<T> list;

    public ListOp(List<T> l) {
        this.list = l;
    }

    public ListOp<T> forEach(Util.RunnableWithParam<T> r) {
        for (T t : list) {
            r.run(t);
        }
        return this;
    }

    public ListOp<T> sort(Comparator<T> comparator) {
        Collections.sort(list, comparator);
        return this;
    }

    public <K> ListOp<K> reflect(Function<T, K> f) {
        List<K> l = new ArrayList<>(list.size());
        for (T t : list) {
            l.add(f.fun(t));
        }
        return new ListOp<>(l);
    }

    public List<T> getList() {
        return list;
    }

    public T[] asArray(T[] a) {
        return getList().toArray(a);
    }

    public int[] asIntArray() {
        int[] ary = new int[getList().size()];
        Iterator<T> iterator = getList().iterator();
        for (int i = 0; i < ary.length; i++) {
            ary[i] = (Integer) iterator.next();
        }
        return ary;
    }

    public ListOp<T> op(Util.RunnableWithParam<ListOp<T>> o) {
        o.run(this);
        return this;
    }

    public static <T> ListOp<T> copyOf(List<T> list) {
        return new ListOp<>(new ArrayList<>(list));
    }

    public static ListOp<Integer> copyOf(int[] a) {
        List<Integer> list = new ArrayList<>(a.length);
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
        return new ListOp<>(list);
    }
}
