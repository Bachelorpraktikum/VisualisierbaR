package com.github.bachelorpraktikum.dbvisualization;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class CompositeList<E> extends AbstractList<E> {

    private final List<? extends E> list1;
    private final List<? extends E> list2;

    public CompositeList() {
        this(Collections.emptyList());
    }

    public CompositeList(List<? extends E> list) {
        this(list, Collections.emptyList());
    }

    public CompositeList(@Nonnull List<? extends E> list1, @Nonnull List<? extends E> list2) {
        this.list1 = list1;
        this.list2 = list2;
    }

    public CompositeList<E> union(List<? extends E> other) {
        return new CompositeList<>(this, other);
    }

    @Override
    public E get(int index) {
        if (index < list1.size()) {
            return list1.get(index);
        } else {
            return list2.get(index - list1.size());
        }
    }

    @Override
    public int size() {
        return list1.size() + list2.size();
    }
}
