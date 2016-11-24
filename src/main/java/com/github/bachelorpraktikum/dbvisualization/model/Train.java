package com.github.bachelorpraktikum.dbvisualization.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import javafx.collections.transformation.SortedList;

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
    private final ObservableList<InterpolatableState> unorderedStates;
    @Nonnull
    private final SortedList<InterpolatableState> states;

    private Train(String name, String readableName, int length) {
        this.name = Objects.requireNonNull(name);
        this.readableName = Objects.requireNonNull(readableName);
        if (length <= 0) {
            throw new IllegalArgumentException("length must be greater than 0");
        }
        this.length = length;

        unorderedStates = FXCollections.observableArrayList();
        states = new SortedList<>(unorderedStates);
    }

    /**
     * Manages instances of {@link Train}.<br>
     * Ensures that there will always be only one instance of Train per name per context.
     */
    @ParametersAreNonnullByDefault
    public static final class Factory {
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
            this.trains = new HashMap<>();
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
     * Gets an unmodifiable list of all known exact states of this train.
     * The list will get updated if a new state is appended to the end.<br>
     * If you want an interpolated state, use {@link #getState(int)}.
     *
     * @return the list of states
     */
    public ObservableList<? extends State> getStates() {
        return FXCollections.unmodifiableObservableList(states);
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

        if (states.isEmpty()) {
            throw new IllegalStateException("not initialized");
        }

        Iterator<InterpolatableState> iterator = states.listIterator(startingIndex);
        InterpolatableState result = iterator.next();

        while (iterator.hasNext()) {
            InterpolatableState state = iterator.next();
            if (state.getTime() > time) {
                return result.interpolate(time, state);
            }
            result = state;
        }

        return result;
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
    private interface StateMutator {
        @Nonnull
        InterpolatableState mutate(int index, InterpolatableState before);
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
         * The back of the train will be at distance 0 from the start of the edge.
         *
         * @param edge the edge on which this train should be initialized.
         * @throws IllegalStateException if this method is called twice for the same train.
         */
        public void init(Edge edge) {
            if (!states.isEmpty()) {
                throw new IllegalStateException("already initialized");
            }
            TrainPosition initPosition = TrainPosition.init(Train.this, edge);
            InterpolatableState initState = new InitState(Train.this, initPosition);
            unorderedStates.add(initState);
        }

        private void addState(int time, StateMutator mutator) {
            if (states.isEmpty()) {
                throw new IllegalStateException("not initialized");
            }
            InterpolatableState before = states.get(states.size() - 1);
            if (before.isTerminated()) {
                throw new IllegalStateException("already terminated");
            }
            if (before.getTime() > time) {
                throw new IllegalStateException("tried to insert state before last element");
            }
            int index = states.size();
            if (before.getTime() == time) {
                index--;
                unorderedStates.remove(index);
            }
            unorderedStates.add(mutator.mutate(index, before));
        }

        /**
         * Registers a speed event.
         *
         * @param time       the time of the event
         * @param distance   the distance travelled since the last event
         * @param speedAfter the speed at this point of time
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void speed(int time, int distance, int speedAfter) {
            addState(time, (index, before) -> before.speed(time, index, distance, speedAfter));
        }

        /**
         * Registers a reachStart event.<br> After this event the front of the train will be at
         * distance 0 from the start of the given {@link Edge}.
         *
         * @param time     the time of the event
         * @param edge     the edge the train reached
         * @param distance the distance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void reach(int time, Edge edge, int distance) {
            addState(time, (index, before) -> before.reach(time, index, edge, distance));
        }

        /**
         * Registers a leave event.<br> After this event the back of the train will be at
         * distance 0 from the start of the given {@link Edge}.
         *
         * @param time     the time of the event
         * @param edge     the edge the train reached
         * @param distance the distance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void leave(int time, Edge edge, int distance) {
            addState(time, (index, before) -> before.leave(time, index, edge, distance));
        }

        /**
         * Terminates this train. Can only be called once.
         *
         * @param time     the time of the event
         * @param distance the distance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has already been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void terminate(int time, int distance) {
            addState(time, (index, before) -> before.terminate(time, index, distance));
        }
    }

    /**
     * Represents the state of a {@link Train} at a specific point of time.
     */
    @Immutable
    @ParametersAreNonnullByDefault
    public interface State extends Event {
        @Nonnull
        Train getTrain();

        boolean isTerminated();

        int getSpeed();

        @Nonnull
        Position getPosition();

        /**
         * Gets the total distance the train has travelled at this point.
         *
         * @return the distance in meters
         */
        int getTotalDistance();
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
         * Gets the distance the front of the train has travelled on the {@link #getFrontEdge()
         * front edge}.
         *
         * @return the distance in meters
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
         * Gets the distance the back of the train is away from the end of the {@link #getBackEdge()
         * back edge}.
         *
         * @return the distance in meters
         */
        int getBackDistance();

        @Nonnull
        List<Edge> getEdges();
    }

    @Immutable
    @ParametersAreNonnullByDefault
    private abstract static class InterpolatableState implements State {
        @Nonnull
        private final Train train;
        private final int index;
        private final int time;
        private final int distance;
        @Nonnull
        private final List<String> events;

        InterpolatableState(Train train, int index, int time, int distance, List<String> events) {
            this.train = train;
            this.index = index;
            this.time = time;
            this.distance = distance;
            this.events = new ArrayList<>(events);
        }

        private List<String> createEvents(int newTime, String newEvent) {
            if (getTime() == newTime) {
                List<String> newEvents = new ArrayList<>(getEvents());
                newEvents.add(newEvent);
                return newEvents;
            } else {
                return Collections.singletonList(newEvent);
            }
        }

        final InterpolatableState speed(int time, int index, int distance, int speed) {
            String event = "Speed{"
                    + "time=" + time
                    + ", distance=" + distance
                    + ", speed=" + speed
                    + "}";
            List<String> newEvents = createEvents(time, event);

            int newDistance = getTotalDistance() + distance;
            TrainPosition newPosition = getPosition().move(distance);
            return new NormalState(getTrain(), index, time, newDistance, speed, newPosition, newEvents);
        }

        final InterpolatableState reach(int time, int index, Edge reached, int movedDistance) {
            String event = "Reach{"
                    + "time=" + time
                    + ", distance=" + movedDistance
                    + ", reached=" + reached
                    + "}";
            List<String> newEvents = createEvents(time, event);
            int newDistance = getTotalDistance() + movedDistance;
            TrainPosition newPosition = getPosition().reachFront(reached, movedDistance);
            return new NormalState(getTrain(), index, time, newDistance, getSpeed(), newPosition, newEvents);
        }

        final InterpolatableState leave(int time, int index, Edge left, int movedDistance) {
            String event = "Leave{"
                    + "time=" + time
                    + ", distance=" + movedDistance
                    + ", left=" + left
                    + "}";
            List<String> newEvents = createEvents(time, event);
            int newDistance = getTotalDistance() + movedDistance;
            TrainPosition newPosition = getPosition().leaveBack(left, movedDistance);
            return new NormalState(getTrain(), index, time, newDistance, getSpeed(), newPosition, newEvents);
        }

        final InterpolatableState terminate(int time, int index, int distance) {
            String event = "Terminate{"
                    + "time=" + time
                    + ", distance=" + distance
                    + "}";
            List<String> newEvents = createEvents(time, event);
            TrainPosition newPosition = getPosition().move(distance);
            int newDistance = getTotalDistance() + distance;
            return new TerminatedState(getTrain(), index, newPosition, time, newDistance, newEvents);
        }

        @Nonnull
        final InterpolatableState interpolate(int targetTime, InterpolatableState other) {
            if (compareTo(other) > 0) {
                return other.interpolate(targetTime, this);
            }

            if (getTime() > targetTime) {
                throw new IllegalArgumentException("time not between states");
            }

            int relativeTargetTime = targetTime - getTime();
            int relativeOtherTime = other.getTime() - getTime();

            int interpolatedSpeed = getSpeed();
            // interpolate speed
            if (!other.isTerminated() && getSpeed() != other.getSpeed()) {
                int speedDiff = other.getSpeed() - getSpeed();
                interpolatedSpeed += (int) (((double) speedDiff) / relativeOtherTime * relativeTargetTime);
            }

            int interpolatedDistance = getTotalDistance();
            TrainPosition interpolatedPosition = getPosition();
            if (!getPosition().equals(other.getPosition())) {
                int distanceDiff = other.getTotalDistance() - getTotalDistance();
                int interpolationDistance = (int) (((double) distanceDiff) / relativeOtherTime * relativeTargetTime);
                interpolatedDistance += interpolationDistance;
                interpolatedPosition = interpolatedPosition.interpolationMove(interpolationDistance, other.getPosition().getFrontEdge());
            }

            return new NormalState(getTrain(), getIndex(), targetTime, interpolatedDistance, interpolatedSpeed, interpolatedPosition, getEvents());
        }

        @Nonnull
        @Override
        public Train getTrain() {
            return train;
        }

        @Nonnull
        @Override
        public abstract TrainPosition getPosition();

        @Override
        public int getTime() {
            return time;
        }

        @Nonnull
        @Override
        public String getDescription() {
            return toString(); // TODO change to something more user-friendly
        }

        @Override
        public int getTotalDistance() {
            return distance;
        }

        public int getIndex() {
            return index;
        }

        @Nonnull
        public List<String> getEvents() {
            return events;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof InterpolatableState)) return false;

            InterpolatableState that = (InterpolatableState) obj;

            if (time != that.time) return false;
            if (getSpeed() != that.getSpeed()) return false;
            if (!train.equals(that.train)) return false;
            return getPosition().equals(that.getPosition());
        }

        @Override
        public int hashCode() {
            int result = train.hashCode();
            result = 31 * result + time;
            result = 31 * result + getSpeed();
            result = 31 * result + getPosition().hashCode();
            return result;
        }

        @Override
        public String toString() {
            return getEvents().toString();
        }
    }

    @Immutable
    @ParametersAreNonnullByDefault
    private static final class TerminatedState extends InterpolatableState {
        private final TrainPosition position;

        TerminatedState(Train train, int index, TrainPosition position, int time, int distance, List<String> events) {
            super(train, index, time, distance, events);
            this.position = position;
        }

        @Override
        public boolean isTerminated() {
            return true;
        }

        @Override
        public int getSpeed() {
            throw new IllegalStateException("Train is terminated");
        }

        @Nonnull
        @Override
        public TrainPosition getPosition() {
            return position;
        }
    }

    @Immutable
    @ParametersAreNonnullByDefault
    private static final class InitState extends InterpolatableState {
        private final TrainPosition position;

        InitState(Train train, TrainPosition position) {
            super(train, 0, 0, 0, Collections.singletonList("Init{" + position + "}"));
            this.position = position;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public int getSpeed() {
            return 0;
        }

        @Nonnull
        @Override
        public TrainPosition getPosition() {
            return position;
        }
    }

    @Immutable
    @ParametersAreNonnullByDefault
    private static final class NormalState extends InterpolatableState {
        private final int speed;
        @Nonnull
        private final TrainPosition position;

        NormalState(Train train, int index, int time, int distance, int speed, TrainPosition position, List<String> events) {
            super(train, index, time, distance, events);
            this.speed = speed;
            this.position = position;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public int getSpeed() {
            return speed;
        }

        @Nonnull
        @Override
        public TrainPosition getPosition() {
            return position;
        }
    }

    @Immutable
    @ParametersAreNonnullByDefault
    private static final class TrainPosition implements Position {
        private final Train train;
        private final LinkedList<Edge> edges;
        private final int frontDistance;
        private final int backDistance;

        private TrainPosition(Train train, LinkedList<Edge> edges, int frontDistance) {
            this.train = train;
            this.edges = edges;
            this.frontDistance = frontDistance;

            int trainLength = train.getLength() - getFrontDistance();
            int backDistance = 0;
            Iterator<Edge> iterator = edges.listIterator(1);
            Edge edge;
            while (iterator.hasNext()) {
                edge = iterator.next();
                if (trainLength == 0) {
                    iterator.remove();
                } else if (trainLength < edge.getLength()) {
                    backDistance = trainLength;
                    trainLength = 0;
                } else {
                    trainLength -= edge.getLength();
                }
            }

            this.backDistance = backDistance;
        }

        static TrainPosition init(Train train, Edge edge) {
            LinkedList<Edge> edges = new LinkedList<>();
            edges.add(edge);
            return new TrainPosition(train, edges, Math.min(train.getLength(), edge.getLength()));
        }

        @Nonnull
        @Override
        public Train getTrain() {
            return train;
        }

        @Nonnull
        @Override
        public Edge getFrontEdge() {
            return edges.getFirst();
        }

        @Override
        public int getFrontDistance() {
            return frontDistance;
        }

        @Nonnull
        @Override
        public Edge getBackEdge() {
            return edges.getLast();
        }

        @Override
        public int getBackDistance() {
            return backDistance;
        }

        @Nonnull
        @Override
        public List<Edge> getEdges() {
            return Collections.unmodifiableList(edges);
        }

        @Nonnull
        TrainPosition move(int distance) {
            int newDistance = getFrontDistance() + distance;
            return new TrainPosition(getTrain(), new LinkedList<>(edges), newDistance);
        }

        @Nonnull
        TrainPosition leaveBack(Edge newBack, int movedDistance) {
            LinkedList<Edge> edges = new LinkedList<>(getEdges());
            edges.add(newBack);
            return new TrainPosition(getTrain(), edges, getFrontDistance() + movedDistance);
        }

        @Nonnull
        TrainPosition reachFront(Edge newStart, int movedDistance) {
            LinkedList<Edge> edges = new LinkedList<>(getEdges());
            edges.addFirst(newStart);
            return new TrainPosition(getTrain(), edges, 0);
        }

        @Nonnull
        TrainPosition interpolationMove(int moveDistance, Edge possibleNewStart) {
            LinkedList<Edge> edges = new LinkedList<>(getEdges());
            int newDistance = getFrontDistance() + moveDistance;
            if (newDistance > getFrontEdge().getLength()) {
                edges.addFirst(possibleNewStart);
                newDistance -= getFrontEdge().getLength();
            }

            return new TrainPosition(getTrain(), edges, newDistance);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Train.Position)) return false;

            Train.Position that = (Train.Position) obj;

            if (!train.equals(that.getTrain())) return false;
            if (frontDistance != that.getFrontDistance()) return false;
            if (!getFrontEdge().equals(that.getFrontEdge())) return false;
            if (backDistance != that.getBackDistance()) return false;
            return getBackEdge().equals(that.getBackEdge());
        }

        @Override
        public int hashCode() {
            int result = getFrontEdge().hashCode();
            result = 31 * result + frontDistance;
            return result;
        }

        @Override
        public String toString() {
            return "Position{"
                    + "edge=" + getFrontEdge()
                    + ", frontDistance=" + frontDistance
                    + '}';
        }
    }
}
