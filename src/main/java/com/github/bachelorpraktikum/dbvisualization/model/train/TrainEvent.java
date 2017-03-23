package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.train.InterpolatableState.Builder;
import java.util.LinkedList;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
abstract class TrainEvent implements Event {

    private static final Logger log = Logger.getLogger(TrainEvent.class.getName());
    private static final double SPEED_COMPARE_DELTA_DEFAULT = 0.5;
    private static final double SPEED_COMPARE_DELTA;

    static {
        String deltaString = ConfigKey.speedCheckDelta.get();
        if (deltaString == null) {
            log.warning(String.format(
                "Missing config entry %s, using default value %f",
                ConfigKey.speedCheckDelta.getKey(),
                SPEED_COMPARE_DELTA_DEFAULT
            ));
            SPEED_COMPARE_DELTA = SPEED_COMPARE_DELTA_DEFAULT;
        } else {
            double delta;
            try {
                delta = Double.parseDouble(deltaString);
            } catch (NumberFormatException e) {
                log.warning(String.format(
                    "Could not parse %s in config file, using default value %f",
                    ConfigKey.speedCheckDelta.getKey(),
                    SPEED_COMPARE_DELTA_DEFAULT
                ));
                delta = SPEED_COMPARE_DELTA_DEFAULT;
            }
            SPEED_COMPARE_DELTA = delta;
        }
    }

    private final int index;
    @Nonnull
    private final Train train;
    private final int time;
    private final int distance;
    private final int totalDistance;
    @Nonnull
    private final ObservableList<String> warnings;
    private boolean speedChecked = false;
    @Nullable
    private InterpolatableState cached = null;

    private final InterpolatableState.Builder stateBuilder;

    private TrainEvent(
        int index,
        Train train,
        int time,
        int distance,
        int totalDistance
    ) {
        this.index = index;
        this.train = train;
        this.time = time;
        this.distance = distance;
        this.totalDistance = totalDistance;
        this.warnings = FXCollections.observableList(new LinkedList<>());
        this.stateBuilder = new InterpolatableState.Builder(getTrain())
            .index(getIndex())
            .time(getTime())
            .distance(getTotalDistance());
    }

    protected void addWarning(String warning) {
        warnings.add(warning);
    }

    @Nullable
    protected TrainEvent getPreviousEvent() {
        if (getIndex() == 0) {
            return null;
        }
        return getTrain().getTrainEvents().get(getIndex() - 1);
    }

    @Override
    @Nonnull
    public ObservableList<String> getWarnings() {
        return warnings;
    }

    /**
     * <p>Whether the state built by this event can be cached.</p>
     *
     * <p>This should be true for all events that only rely on previous events.</p>
     *
     * @return whether caching is valid
     */
    abstract boolean canCache();

    final int getIndex() {
        return index;
    }

    @Nonnull
    final Train getTrain() {
        return train;
    }

    @Override
    public final int getTime() {
        return time;
    }

    final int getDistance() {
        return distance;
    }

    final int getTotalDistance() {
        return totalDistance;
    }

    abstract double getSpeed();

    @Nullable
    abstract TrainPosition getPosition();

    @Nonnull
    InterpolatableState.Builder stateBuilder() {
        double speed = getSpeed();
        if (!speedChecked) {
            speedChecked = true;
            TrainEvent prevEvent = getPreviousEvent();
            if (prevEvent instanceof Speed) {
                Speed speedEvent = (Speed) prevEvent;
                int expectedSpeed = speedEvent.getSpeedAfter();
                if (!equalWithDelta(speed, expectedSpeed, SPEED_COMPARE_DELTA)) {
                    addWarning(String.format(
                        "Expected speed: %d; Calculated: %f", expectedSpeed, speed
                    ));
                }
            }
        }
        return stateBuilder
            .speed(speed)
            .position(getPosition());
    }

    private boolean equalWithDelta(double d1, double d2, double delta) {
        double diff = Math.abs(d1 - d2);
        return diff < delta;
    }

    /**
     * Gets the state of the train after this event.
     * This method may return different states for consecutive calls.
     *
     * @return the state
     */
    @Nonnull
    final InterpolatableState getState() {
        if (canCache() && cached != null) {
            return cached;
        } else {
            // always cache the state and use canCache() as an indicator for
            // whether the cached value can be used
            return (cached = stateBuilder().build());
        }
    }

    @Override
    public String toString() {
        return getDescription();
    }

    /**
     * This is a workaround for speedAfter events without a speedAfter.
     */
    @ParametersAreNonnullByDefault
    static class Move extends Position {

        Move(TrainEvent before, int time, int distance) {
            super(before.getIndex() + 1,
                before.getTrain(),
                time,
                distance,
                before.getTotalDistance() + distance
            );
            addWarning("Speed event without speedAfter!");
        }

        @Nonnull
        @Override
        TrainPosition getPosition() {
            return getPreviousEvent().getPosition().move(getDistance());
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Speed{"
                + "time=" + getTime()
                + ", distance=" + getDistance()
                + ", speedAfter=NULL"
                + "}";
        }
    }

    @ParametersAreNonnullByDefault
    static class Speed extends Position {

        private final int speedAfter;

        private Speed(int index, Train train, int time, int distance, int totalDistance,
            int speed) {
            super(index, train, time, distance, totalDistance);
            this.speedAfter = speed;
        }

        Speed(TrainEvent before, int time, int distance, int speed) {
            this(before.getIndex() + 1,
                before.getTrain(),
                time,
                distance,
                before.getTotalDistance() + distance,
                speed
            );
        }

        int getSpeedAfter() {
            return speedAfter;
        }

        @Nonnull
        @Override
        TrainPosition getPosition() {
            return getPreviousEvent().getPosition().move(getDistance());
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Speed{"
                + "time=" + getTime()
                + ", distance=" + getDistance()
                + ", speedAfter=" + speedAfter
                + "}";
        }
    }

    @ParametersAreNonnullByDefault
    static class Start extends TrainEvent {

        Start(Train train) {
            super(0, train, Context.INIT_STATE_TIME, 0, 0);
        }

        @Nonnull
        @Override
        Builder stateBuilder() {
            return super.stateBuilder().initialized(false);
        }

        @Override
        double getSpeed() {
            return 0;
        }

        @Nullable
        @Override
        TrainPosition getPosition() {
            return null;
        }

        @Override
        boolean canCache() {
            return true;
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Start{"
                + "time=" + getTime()
                + ", this event purely serves as a helper for interpolation";
        }
    }

    @ParametersAreNonnullByDefault
    static class Init extends TrainEvent {

        private final Edge startEdge;

        Init(int time, Train train, Edge startEdge) {
            super(1,
                train,
                time,
                0,
                0);
            this.startEdge = startEdge;
        }

        private static TrainPosition getPositionWithLookahead(Train train, Edge startEdge) {
            Reach reach = findFirstReachEvent(train);
            if (reach == null) {
                return TrainPosition
                    .init(train, startEdge, startEdge.getNode1(), startEdge.getNode2());
            }
            Edge reached = reach.getReached();
            Node common = reached.getCommonNode(startEdge);
            Node other = startEdge.getOtherNode(common);
            return TrainPosition.init(train, startEdge, other, common);
        }

        private static Reach findFirstReachEvent(Train train) {
            for (Event event : train.getTrainEvents()) {
                if (event instanceof Reach) {
                    return (Reach) event;
                }
            }
            return null;
        }

        @Override
        boolean canCache() {
            return false;
        }

        @Override
        double getSpeed() {
            return 0;
        }

        @Nullable
        @Override
        TrainPosition getPosition() {
            return Init.getPositionWithLookahead(getTrain(), startEdge);
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Init{"
                + "time=" + getTime()
                + ", startEdge=" + getPosition().getBackEdge().getName()
                + "}";
        }
    }

    static class Terminate extends Position {

        Terminate(TrainEvent before, int time, int distance) {
            super(before.getIndex() + 1,
                before.getTrain(),
                time,
                distance,
                before.getTotalDistance() + distance
            );
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Terminate{"
                + "time=" + getTime()
                + ", totalDistance=" + getTotalDistance()
                + "}";
        }

        @Nonnull
        @Override
        TrainPosition getPosition() {
            return getPreviousEvent().getPosition().move(getDistance());
        }

        @Nonnull
        @Override
        InterpolatableState.Builder stateBuilder() {
            return super.stateBuilder()
                .terminated(true);
        }
    }

    @ParametersAreNonnullByDefault
    private abstract static class Position extends TrainEvent {

        Position(int index, Train train, int time, int distance, int totalDistance) {
            super(index, train, time, distance, totalDistance);
        }

        @Override
        boolean canCache() {
            return true;
        }

        @Override
        double getSpeed() {
            TrainEvent before = getPreviousEvent();
            double diffSeconds = (getTime() - before.getTime()) / 1000.0;
            if (diffSeconds == 0) {
                return before.getSpeed();
            }
            return getDistance() / diffSeconds;
        }

        @Nonnull
        @Override
        abstract TrainPosition getPosition();
    }

    @ParametersAreNonnullByDefault
    static class Reach extends Position {

        @Nonnull
        private final Edge reached;

        Reach(TrainEvent before, int time, int distance, Edge reached) {
            super(before.getIndex() + 1,
                before.getTrain(),
                time,
                distance,
                before.getTotalDistance() + distance);
            this.reached = reached;
        }

        @Nonnull
        Edge getReached() {
            return reached;
        }

        @Nonnull
        @Override
        TrainPosition getPosition() {
            return getPreviousEvent().getPosition().reachFront(reached);
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Reach{"
                + "time=" + getTime()
                + ", distance=" + getDistance()
                + ", reached=" + reached.getName()
                + "}";
        }
    }

    @ParametersAreNonnullByDefault
    static class Leave extends Position {

        @Nonnull
        private final Edge left;

        Leave(TrainEvent before, int time, int distance, Edge left) {
            super(before.getIndex() + 1,
                before.getTrain(),
                time,
                distance,
                before.getTotalDistance() + distance);
            this.left = left;
        }

        @Nonnull
        @Override
        TrainPosition getPosition() {
            return getPreviousEvent().getPosition().leaveBack(left, getDistance());
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Leave{"
                + "time=" + getTime()
                + ", distance=" + getDistance()
                + ", backEdge=" + left.getName()
                + "}";
        }
    }
}
