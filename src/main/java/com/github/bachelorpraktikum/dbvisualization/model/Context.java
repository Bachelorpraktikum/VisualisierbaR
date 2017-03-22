package com.github.bachelorpraktikum.dbvisualization.model;

import com.github.bachelorpraktikum.dbvisualization.CompositeObservableList;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javafx.collections.ObservableList;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Provides a context for factories of classes in this package.<br>Classes in this package always
 * ensure the uniqueness of names for an instance of Context.<br>If an instance of this class is no
 * longer referenced in any client code, all associated data will be garbage collected.
 */
@Immutable
public final class Context {

    public static final int INIT_STATE_TIME = -1;

    private final List<Object> objects;

    public Context() {
        this.objects = new LinkedList<>();
    }

    /**
     * <p>Gets a list of all events associated with this context.</p>
     *
     * <p>If a train is created after calling this method, the list previously returned will not
     * contain any of the new train's events.</p>
     *
     * @return an immutable observable list of events
     */
    @Nonnull
    public ObservableList<Event> getObservableEvents() {
        CompositeObservableList<Event> elementEvents = new CompositeObservableList<>(
            Element.in(this).getEvents()
        );

        CompositeObservableList<Event> events = elementEvents.union(
            Messages.in(this).getEvents()
        );

        return events.union(
            Train.in(this).getAll().stream()
                .map(Train::getEvents)
                .reduce(new CompositeObservableList<>(),
                    CompositeObservableList::union,
                    CompositeObservableList::union)
        );
    }

    /**
     * <p>Ensures the given object will not be garbage collected until this context is.</p>
     *
     * @param object any object
     * @throws NullPointerException if the object is null
     */
    public void addObject(@Nonnull Object object) {
        objects.add(Objects.requireNonNull(object));
    }
}
