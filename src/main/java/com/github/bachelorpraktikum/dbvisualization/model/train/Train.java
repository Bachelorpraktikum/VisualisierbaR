package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.GraphObject;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.Shapeable;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a train.<br> There will always be exactly one instance of this class per name per
 * {@link Context}. <p>Once {@link EventFactory#init(int, Edge) initialized}, a train has exactly
 * one {@link #getState(int) state} at any given point of time (represented by a positive
 * integer).</p> <p>Only the state at a point of time after or at the time of the last registered
 * event can change.</p> <p>{@link EventFactory#terminate(int, int) Terminated} trains are
 * immutable.</p>
 */
@ParametersAreNonnullByDefault
public class Train implements GraphObject<Shape> {

    private static final Logger log = Logger.getLogger(Train.class.getName());

    @Nonnull
    private final String name;
    @Nonnull
    private final String readableName;
    private final int length;
    private final Property<VisibleState> stateProperty;

    @Nonnull
    private final ObservableList<TrainEvent> events;

    /**
     * Creates a new train.
     *
     * @param name the unique name of the train
     * @param readableName the human readable name of the train
     * @param length the length of the train
     * @throws IllegalArgumentException if length <= 0
     */
    private Train(String name, String readableName, int length) {
        this.name = Objects.requireNonNull(name);
        this.readableName = Objects.requireNonNull(readableName);
        if (length <= 0) {
            throw new IllegalArgumentException("length must be greater than 0");
        }
        this.length = length;

        events = FXCollections.observableArrayList();
        events.add(new TrainEvent.Start(this));
        stateProperty = new SimpleObjectProperty<>(VisibleState.AUTO);
    }

    @Override
    public Path createShape() {
        return new Path();
    }

    @Override
    public Shape createIconShape() {
        URL url = Train.class.getResource("../symbols/train.fxml");
        Shape shape = Shapeable.createShape(url);
        shape.setRotate(180);
        return shape;
    }

    @Override
    public Property<VisibleState> visibleStateProperty() {
        return stateProperty;
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
         * @param name unique name of the train
         * @param readableName human readable name of the train
         * @param length length of the train in meters
         * @return an instance of {@link Train}
         * @throws IllegalArgumentException if length is negative or zero
         * @throws IllegalArgumentException if a train with the same name but a different readable
         * name or length already exists
         * @throws NullPointerException if name or readableName is null
         */
        @Nonnull
        public Train create(String name, String readableName, int length) {
            Train result = trains.computeIfAbsent(Objects.requireNonNull(name), n ->
                new Train(n, readableName, length)
            );

            if (result.getLength() != length
                || !result.getReadableName().equals(readableName)) {
                String trainFormat = "(readableName: %s, length: %d)";
                String message = "Train with name: %s already exists:\n"
                    + trainFormat + ", tried to recreate with following arguments:\n"
                    + trainFormat;
                message = String.format(message, name, readableName, length,
                    result.getReadableName(), result.getLength());
                throw new IllegalArgumentException(message);
            }

            return result;
        }

        /**
         * Gets the only {@link Train} instance for the given name.
         *
         * @param name the train's name
         * @return the train instance for the given name
         * @throws NullPointerException if name is null
         * @throws IllegalArgumentException if there is no train with the given name in this
         * context
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

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
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
     * @throws IllegalArgumentException if time is less than {@link Context#INIT_STATE_TIME}
     * @throws IllegalStateException if this train has not been {@link EventFactory#init(int, Edge)
     * initialized}
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
     * @param time the time in milliseconds since the start of the simulation
     * @param before the state to use as a jumping off point
     * @return the state of the train at the given point in time.
     * @throws IllegalArgumentException if time is less than {@link Context#INIT_STATE_TIME}
     * @throws IllegalArgumentException if before is a state of a different train
     * @throws NullPointerException if before is null
     * @throws IllegalStateException if this train has not been {@link EventFactory#init(int, Edge)
     * initialized}
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
        if (time < Context.INIT_STATE_TIME) {
            throw new IllegalArgumentException("time is too small");
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
        TrainEvent create(int eventTime, TrainEvent before);
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
         * @param time the time of the event
         * @param edge the edge on which this train should be initialized.
         * @throws IllegalStateException if this method is called twice for the same train.
         */
        public void init(int time, Edge edge) {
            if (events.size() != 1) {
                throw new IllegalStateException("already initialized. Possibly two init events?");
            }
            List<String> warnings = new LinkedList<>();
            if (time < 0) {
                warnings.add("Tried to add with negative time: " + time);
                time = 0;
            }
            TrainEvent event = new TrainEvent.Init(time, Train.this, edge);
            warnings.forEach(event::addWarning);
            events.add(event);
        }

        private void addState(int time, EventCreator creator) {
            if (events.size() == 1) {
                throw new IllegalStateException("not initialized");
            }
            List<String> warnings = new LinkedList<>();
            TrainEvent before = events.get(events.size() - 1);
            if (before instanceof TrainEvent.Terminate) {
                warnings.add("Event after termination!");
            }
            if (before.getTime() > time) {
                warnings.add("tried to insert before previous event at time: " + time);
                time = before.getTime();
            }
            TrainEvent event = creator.create(time, before);
            warnings.forEach(event::addWarning);
            events.add(event);
        }

        /**
         * Registers a speed event.
         *
         * @param time the time of the event
         * @param distance the distance travelled since the last event
         * @param speedAfter the speed at this point of time
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         * event
         */
        public void speed(int time, int distance, int speedAfter) {
            addState(time, (eventTime, before) -> new TrainEvent.Speed(before, eventTime, distance,
                speedAfter));
        }

        /**
         * Registers a speed event without a new speed.
         * This is a workaround for speed log entries without a speed.
         *
         * @param time the time of the event
         * @param distance the distance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         * event
         */
        public void move(int time, int distance) {
            addState(time, (eventTime, before) -> new TrainEvent.Move(before, eventTime, distance));
        }

        /**
         * Registers a reachStart event.<br> After this event the front of the train will be at
         * distance 0 from the start of the given {@link Edge}.
         *
         * @param time the time of the event
         * @param edge the edge the train reached
         * @param distance the distance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         * event
         */
        public void reach(int time, Edge edge, int distance) {
            addState(time,
                (eventTime, before) -> new TrainEvent.Reach(before, eventTime, distance, edge));
        }

        /**
         * Registers a leave event.<br> After this event the back of the train will be at
         * distance 0 from the start of the given {@link Edge}.
         *
         * @param time the time of the event
         * @param edge the edge the train reached
         * @param distance the distance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         * event
         */
        public void leave(int time, Edge edge, int distance) {
            addState(time,
                (eventTime, before) -> new TrainEvent.Leave(before, eventTime, distance, edge));
        }

        /**
         * Terminates this train. Can only be called once.
         *
         * @param time the time of the event
         * @param distance the distance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has already been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         * event
         */
        public void terminate(int time, int distance) {
            addState(time,
                (eventTime, before) -> new TrainEvent.Terminate(before, eventTime, distance));
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

        boolean isInitialized();

        int getSpeed();

        /**
         * Gets the position of the train.
         *
         * @return the train position
         * @throws IllegalStateException if {@link #isInitialized()} is false
         */
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
         * This is a convenience method for {@link #getEdges() getEdges().get(0)}.
         *
         * @return the edge
         */
        @Nonnull
        Edge getFrontEdge();

        /**
         * Gets the distance the front of the train has travelled on the {@link #getFrontEdge()
         * front edge}.
         *
         * @return the distance in meters
         */
        int getFrontDistance();

        /**
         * Gets the real coordinates of the train's front.
         *
         * @param adapter the adapter to translate the Coordinates of the nearest Nodes to real
         * coordinates.
         * @return the real position of the front
         */
        @Nonnull
        Point2D getFrontPosition(Function<Node, Point2D> adapter);

        /**
         * Gets the position of the train's front in the virtual coordinate system of the
         * simulation. The coordinates returned by this function can not confidently translated to
         * real coordinates, because the actual layout might differ from the virtual one. If you
         * want real coordinates of the front, use {@link #getFrontPosition(Function)}.
         *
         * @return the virtual coordinates of the front
         */
        @Nonnull
        Point2D getFrontCoordinates();

        /**
         * Gets the {@link Edge} the back of the {@link Train} is on. This is a convenience method
         * for {@link #getEdges() getEdges().get(getEdges().size() - 1)}.
         *
         * @return the edge
         */
        @Nonnull
        Edge getBackEdge();

        /**
         * Gets the distance the back of the train is away from the end of the {@link
         * #getBackEdge() back edge}.
         * In other words, this is the distance the back end of the train still has to travel until
         * it reaches the end of the edge.
         *
         * @return the distance in meters
         */
        int getBackDistance();

        /**
         * Gets the real coordinates of the train's back.
         *
         * @param adapter the adapter to translate the Coordinates of the nearest Nodes to real
         * coordinates.
         * @return the real position of the back
         */
        @Nonnull
        Point2D getBackPosition(Function<Node, Point2D> adapter);

        /**
         * Gets the position of the train's back in the virtual coordinate system of the
         * simulation. The coordinates returned by this function can not confidently translated to
         * real coordinates, because the actual layout might differ from the virtual one. If you
         * want real coordinates of the back, use {@link #getBackPosition(Function)}.
         *
         * @return the virtual coordinates of the back
         */
        @Nonnull
        Point2D getBackCoordinates();

        /**
         * Gets all significant points this train is on, from front to start.
         * This includes all nodes and the exact positions of the front and back.
         *
         * @param adapter the adapter to translate Node Coordinates to real positions.
         * @return a list of real positions
         */
        @Nonnull
        List<Point2D> getPositions(Function<Node, Point2D> adapter);

        /**
         * Gets all edges this train is on. The list starts with the front edge and ends with the
         * back edge.
         *
         * @return a list of all edges this train is on
         */
        @Nonnull
        List<Edge> getEdges();
    }
}
