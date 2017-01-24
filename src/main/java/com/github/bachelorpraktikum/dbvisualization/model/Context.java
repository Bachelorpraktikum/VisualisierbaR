package com.github.bachelorpraktikum.dbvisualization.model;

import com.github.bachelorpraktikum.dbvisualization.CompositeList;
import com.github.bachelorpraktikum.dbvisualization.CompositeObservableList;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.ObservableList;
import javax.annotation.concurrent.Immutable;

/**
 * Provides a context for factories of classes in this package.<br>Classes in this package always
 * ensure the uniqueness of names for an instance of Context.<br>If an instance of this class is no
 * longer referenced in any client code, all associated data will be garbage collected.
 */
@Immutable
public final class Context {

    private final List<Object> objects;

    public Context() {
        this.objects = new LinkedList<>();
    }

    public List<Event> getEvents() {
        CompositeList<Event> elementEvents = new CompositeList<>(Element.in(this).getEvents());
        return elementEvents.union(Train.in(this).getAll().stream()
                .map(Train::getEvents)
                .reduce(new CompositeList<>(), CompositeList::union, CompositeList::union));
    }

    public ObservableList<Event> getObservableEvents() {
        CompositeObservableList<Event> elementEvents = new CompositeObservableList<>(
                Element.in(this).getEvents());
        return elementEvents.union(Train.in(this).getAll().stream()
                .map(Train::getEvents)
                .reduce(new CompositeObservableList<>(),
                        CompositeObservableList::union,
                        CompositeObservableList::union));
    }

    public void addObject(Object object) {
        objects.add(object);
    }
}
