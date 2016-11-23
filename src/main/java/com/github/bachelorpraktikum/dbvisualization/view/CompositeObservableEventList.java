package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Event;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

class CompositeObservableEventList extends ObservableListBase<Event> {
    private final List<ObservableList<? extends Event>> lists;

    CompositeObservableEventList(List<ObservableList<? extends Event>> eventLists) {
        this.lists = new ArrayList<>(eventLists);
        registerListeners();
    }

    private void registerListeners() {
        for (ObservableList<? extends Event> list : lists) {
            list.addListener(this::fireChange);
        }
    }

    @Override
    public Event get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index is negative");
        }

        for (ObservableList<? extends Event> list : lists) {
            if (list.size() <= index) {
                index -= list.size();
            } else {
                return list.get(index);
            }
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return lists.stream().mapToInt(List::size).sum();
    }
}
