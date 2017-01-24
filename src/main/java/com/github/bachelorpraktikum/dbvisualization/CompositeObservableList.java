package com.github.bachelorpraktikum.dbvisualization;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.WeakListChangeListener;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link ObservableList} implementation that is composed of multiple observable lists.
 * Does not implement optional operations like {@link #add(Object)}.
 */
@ParametersAreNonnullByDefault
public final class CompositeObservableList<T> extends ObservableListBase<T> {

    private final ObservableList<? extends T> list1;
    private final ObservableList<? extends T> list2;
    private final ListChangeListener<T> listener;


    public CompositeObservableList() {
        this(FXCollections.emptyObservableList());
    }

    public CompositeObservableList(ObservableList<? extends T> list) {
        this(list, FXCollections.emptyObservableList());
    }

    public CompositeObservableList(ObservableList<? extends T> list1,
            ObservableList<? extends T> list2) {
        this.list1 = list1;
        this.list2 = list2;
        listener = this::fireChange;
        registerListener(this.list1);
        registerListener(this.list2);
    }

    public CompositeObservableList<T> union(ObservableList<? extends T> other) {
        return new CompositeObservableList<>(this, other);
    }

    private void registerListener(ObservableList<? extends T> list) {
        list.addListener(new WeakListChangeListener<>(listener));
    }

    @Override
    public T get(int index) {
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
