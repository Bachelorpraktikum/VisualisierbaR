package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents a train.<br> There will always be exactly one instance of this class per name per
 * {@link Context}. <p>Once {@link EventFactory#init(Edge) initialized}, a train has exactly one
 * {@link #getState(int) state} at any given point of time (represented by a positive integer).</p>
 * <p>Only the state at a point of time after or at the time of the last registered event can
 * change.</p> <p>{@link EventFactory#terminate(int, int) Terminated} trains are immutable.</p>
 */
@ParametersAreNonnullByDefault
public class Train {
    private static final Logger log = Logger.getLogger(Train.class.getName());

    @Nonnull
    private final String name;
    @Nonnull
    private final String readableName;
    private final int length;

    @Nonnull
    private final ObservableList<TrainEvent> events;

    private Train(String name, String readableName, int length) {
        this.name = Objects.requireNonNull(name);
        this.readableName = Objects.requireNonNull(readableName);
        if (length <= 0) {
            throw new IllegalArgumentException("length must be greater than 0");
        }
        this.length = length;

        events = FXCollections.observableArrayList();
    }

    /**
     * Manages instances of {@link Train}.<br>
     * Ensures that there will always be only one instance of Train per name per context.
     */
    @ParametersAreNonnullByDefault
    public static final class Factory {
        private static final int INITIAL_TRAINS_CAPACITY = 16;
        private static final Map<Context, Factory> instances = new WeakHashMap<>();

        @Nonnull
        private final Map<String, Train> trains;

        @Nonnull
        private static Factory getInstance(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }
            return instances.computeIfAbsent(context, g -> new Factory());
        }

        private Factory() {
            this.trains = new HashMap<>(INITIAL_TRAINS_CAPACITY);
        }

        /**
         * Potentially creates a new train instance.
         *
         * @param name         unique name of the train
         * @param readableName human readable name of the train
         * @param length       length of the train in meters
         * @return an instance of {@link Train}
         * @throws IllegalArgumentException if another instance with the same name but <b>with a
         *                                  different readableName or length.</b> exists
         * @throws IllegalArgumentException if length is negative or zero
         * @throws NullPointerException     if name or readableName is null
         */
        @Nonnull
        public Train create(String name, String readableName, int length) {
            Train result = trains.get(Objects.requireNonNull(name));
            if (result == null) {
                result = new Train(name, readableName, length);
                trains.put(name, result);
            } else if (result.getLength() != length
                    || !result.getReadableName().equals(readableName)) {
                throw new IllegalArgumentException("train already exists, but differently");
            }

            return result;
        }

        /**
         * Gets the only {@link Train} instance for the given name.
         *
         * @param name the train's name
         * @return the train instance for the given name
         * @throws NullPointerException     if name is null
         * @throws IllegalArgumentException if there is no train with the given name in this
         *                                  context
         */
        @Nonnull
        public Train get(String name) {
            Train train = trains.get(Objects.requireNonNull(name));
            if (train == null) {
                throw new IllegalArgumentException("unknown train: " + name);
            }
            return train;
        }

        /**
         * Gets all trains in this {@link Context}.
         *
         * @return an unmodifiable collection of {@link Train} instances
         */
        @Nonnull
        public Collection<Train> getAll() {
            return Collections.unmodifiableCollection(trains.values());
        }
    }

    /**
     * Gets the {@link Factory} instance for the given context.
     *
     * @param context the context
     * @return the only Factory instance for this context
     * @throws NullPointerException if context is null
     */
    @Nonnull
    public static Factory in(Context context) {
        return Factory.getInstance(context);
    }

    /**
     * Gets the unique name of this {@link Train}.
     *
     * @return the name
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Gets the human readable name of this {@link Train}.
     *
     * @return the human readable name
     */
    @Nonnull
    public String getReadableName() {
        return readableName;
    }

    /**
     * Gets the length of this {@link Train} in meters.
     *
     * @return the length in meters
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets an unmodifiable list of all known events of this train.
     * The list will get updated if a new event is appended to the end.
     *
     * @return the list of events
     */
    public ObservableList<? extends Event> getEvents() {
        return FXCollections.unmodifiableObservableList(events);
    }

    List<TrainEvent> getTrainEvents() {
        return FXCollections.unmodifiableObservableList(events);
    }

    /**
     * Gets the state of this {@link Train} at the given time.
     * {@link State} objects are immutable.
     *
     * @param time the time in milliseconds since the start of the simulation
     * @return the state of the train at the given point in time.
     * @throws IllegalArgumentException if time is negative
     * @throws IllegalStateException    if this train has not been {@link EventFactory#init(Edge)
     *                                  initialized}
     */
    @Nonnull
    public State getState(int time) {
        return getState(time, 0);
    }

    /**
     * Gets the state of this train at the given time, similar to {@link #getState(int)}.<br>In
     * contrast to the aforementioned method, this method can use the given state as a starting
     * point for searching the wanted state, which is potentially more efficient. If the time of the
     * given starting state is after the wanted time, this method won't improve performance.
     *
     * @param time   the time in milliseconds since the start of the simulation
     * @param before the state to use as a jumping off point
     * @return the state of the train at the given point in time.
     * @throws IllegalArgumentException if time is negative
     * @throws IllegalArgumentException if before is a state of a different train
     * @throws NullPointerException     if before is null
     * @throws IllegalStateException    if this train has not been {@link EventFactory#init(Edge)
     *                                  initialized}
     */
    @Nonnull
    public State getState(int time, State before) {
        if (before == null) {
            throw new NullPointerException("before state is null");
        }

        if (!before.getTrain().equals(this)) {
            throw new IllegalArgumentException("passed state of different train");
        }

        if (before.getTime() > time) {
            return getState(time);
        }

        if (before instanceof InterpolatableState) {
            return getState(time, ((InterpolatableState) before).getIndex());
        }

        return getState(time);
    }

    @Nonnull
    private State getState(int time, int startingIndex) {
        if (time < 0) {
            throw new IllegalArgumentException("time is negative");
        }

        if (events.isEmpty()) {
            throw new IllegalStateException("not initialized");
        }

        Iterator<TrainEvent> iterator = events.listIterator(startingIndex);
        TrainEvent result = iterator.next();

        while (iterator.hasNext()) {
            TrainEvent event = iterator.next();
            if (event.getTime() > time) {
                return result.getState().interpolate(time, event.getState());
            }
            result = event;
        }

        return result.getState();
    }

    /**
     * Gets the {@link EventFactory} for this train.
     *
     * @return the EventFactory
     */
    @Nonnull
    public EventFactory eventFactory() {
        return new EventFactory();
    }

    @Override
    public String toString() {
        return "Train{"
                + "name='" + name + '\''
                + ", readableName='" + readableName + '\''
                + ", length=" + length
                + '}';
    }

    @FunctionalInterface
    private interface EventCreator {
        @Nonnull
        TrainEvent create(TrainEvent before);
    }

    /**
     * Creates events for an instance of {@link Train}.
     */
    @ParametersAreNonnullByDefault
    public final class EventFactory {
        private EventFactory() {
        }

        /**
         * Initializes the {@link Train} instance corresponding to this factory.
         * The back of the train will be at totalDistance 0 from the start of the edge.
         *
         * @param edge the edge on which this train should be initialized.
         * @throws IllegalStateException if this method is called twice for the same train.
         */
        public void init(Edge edge) {
            if (!events.isEmpty()) {
                throw new IllegalStateException("already initialized");
            }
            events.add(new TrainEvent.Init(Train.this, edge));
        }

        private void addState(int time, EventCreator creator) {
            if (events.isEmpty()) {
                throw new IllegalStateException("not initialized");
            }
            TrainEvent before = events.get(events.size() - 1);
            if (before instanceof TrainEvent.Terminate) {
                throw new IllegalStateException("already terminated");
            }
            if (before.getTime() > time) {
                throw new IllegalStateException("tried to insert event before last element");
            }
            events.add(creator.create(before));
        }

        /**
         * Registers a speed event.
         *
         * @param time       the time of the event
         * @param distance   the totalDistance travelled since the last event
         * @param speedAfter the speed at this point of time
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void speed(int time, int distance, int speedAfter) {
            addState(time, (before) -> new TrainEvent.Speed(before, time, distance, speedAfter));
        }

        /**
         * Registers a reachStart event.<br> After this event the front of the train will be at
         * totalDistance 0 from the start of the given {@link Edge}.
         *
         * @param time     the time of the event
         * @param edge     the edge the train reached
         * @param distance the totalDistance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void reach(int time, Edge edge, int distance) {
            addState(time, (before) -> new TrainEvent.Reach(before, time, distance, edge));
        }

        /**
         * Registers a leave event.<br> After this event the back of the train will be at
         * totalDistance 0 from the start of the given {@link Edge}.
         *
         * @param time     the time of the event
         * @param edge     the edge the train reached
         * @param distance the totalDistance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void leave(int time, Edge edge, int distance) {
            addState(time, (before) -> new TrainEvent.Leave(before, time, distance, edge));
        }

        /**
         * Terminates this train. Can only be called once.
         *
         * @param time     the time of the event
         * @param distance the totalDistance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has already been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void terminate(int time, int distance) {
            addState(time, (before) -> new TrainEvent.Terminate(before, time, distance));
        }
    }

    /**
     * Represents the state of a {@link Train} at a specific point of time.
     */
    @Immutable
    @ParametersAreNonnullByDefault
    public interface State extends Comparable<State> {
        @Nonnull
        Train getTrain();

        int getTime();

        boolean isTerminated();

        int getSpeed();

        @Nonnull
        Position getPosition();

        /**
         * Gets the total totalDistance the train has travelled at this point.
         *
         * @return the totalDistance in meters
         */
        int getTotalDistance();

        @Override
        default int compareTo(State other) {
            return Integer.compare(getTime(), other.getTime());
        }
    }

    /**
     * Represents the position of a {@link Train}.
     */
    @Immutable
    @ParametersAreNonnullByDefault
    public interface Position {
        /**
         * Gets the {@link Train} corresponding to this {@link Position}.
         *
         * @return the train
         */
        @Nonnull
        Train getTrain();

        /**
         * Gets the {@link Edge} the front of the {@link Train} is on.
         *
         * @return the edge
         */
        @Nonnull
        Edge getFrontEdge();

        /**
         * Gets the totalDistance the front of the train has travelled on the {@link #getFrontEdge()
         * front edge}.
         *
         * @return the totalDistance in meters
         */
        int getFrontDistance();

        /**
         * Gets the {@link Edge} the back of the {@link Train} is on.
         *
         * @return the edge
         */
        @Nonnull
        Edge getBackEdge();

        /**
         * Gets the totalDistance the back of the train is away from the end of the {@link
         * #getBackEdge() back edge}.
         *
         * @return the totalDistance in meters
         */
        int getBackDistance();

        @Nonnull
        List<Edge> getEdges();
    }
}
