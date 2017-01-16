package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Event;

import com.github.bachelorpraktikum.dbvisualization.model.train.InterpolatableState.Builder;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
abstract class TrainEvent implements Event {
    private final int index;
    @Nonnull
    private final Train train;
    private final int time;
    private final int distance;
    private final int totalDistance;
    @Nullable
    private final TrainPosition position;
    @Nonnull
    private final List<String> warnings;

    private TrainEvent(int index, Train train, int time, int distance, int totalDistance, @Nullable TrainPosition position) {
        this.index = index;
        this.train = train;
        this.time = time;
        this.distance = distance;
        this.totalDistance = totalDistance;
        this.position = position;
        this.warnings = new LinkedList<>();
    }

    protected void addWarning(String warning) {
        warnings.add(warning);
    }

    @Override
    @Nonnull
    public List<String> getWarnings() {
        return warnings;
    }

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

    @Nullable
    final TrainPosition getPosition() {
        return position;
    }

    @Nonnull
    InterpolatableState.Builder stateBuilder() {
        return new InterpolatableState.Builder(getTrain())
                .index(getIndex())
                .time(getTime())
                .distance(getTotalDistance())
                .position(getPosition());
    }

    /**
     * Gets the state of the train after this event.
     * This method may return different states for consecutive calls.
     *
     * @return the state
     */
    @Nonnull
    final InterpolatableState getState() {
        return stateBuilder().build();
    }

    /**
     * This is a workaround for speed events without a speed.
     */
    @ParametersAreNonnullByDefault
    static class Move extends Position {
        Move(TrainEvent before, int time, int distance) {
            super(before.getIndex() + 1,
                    before.getTrain(),
                    time,
                    distance,
                    before.getTotalDistance() + distance,
                    before.getPosition().move(distance)
            );
            addWarning("Speed event without speed!");
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Speed{"
                    + "time=" + getTime()
                    + ", distance=" + getDistance()
                    + ", speed=NULL"
                    + "}";
        }
    }

    @ParametersAreNonnullByDefault
    static class Speed extends TrainEvent {
        private final int speed;

        private Speed(int index, Train train, int time, int distance, int totalDistance, @Nullable TrainPosition position, int speed) {
            super(index, train, time, distance, totalDistance, position);
            this.speed = speed;
        }

        Speed(TrainEvent before, int time, int distance, int speed) {
            this(before.getIndex() + 1,
                    before.getTrain(),
                    time,
                    distance,
                    before.getTotalDistance() + distance,
                    before.getPosition().move(distance),
                    speed
            );
        }

        final int getSpeed() {
            return speed;
        }

        @Nonnull
        @Override
        InterpolatableState.Builder stateBuilder() {
            return super.stateBuilder()
                    .speed(getSpeed());
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Speed{"
                    + "time=" + getTime()
                    + ", distance=" + getDistance()
                    + ", speed=" + getSpeed()
                    + "}";
        }
    }

    @ParametersAreNonnullByDefault
    static class Start extends Speed {
        Start(Train train) {
            super(0, train, 0, 0, 0, null, 0);
        }

        @Nonnull
        @Override
        Builder stateBuilder() {
            return super.stateBuilder().initialized(false);
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Start{"
                    + "time=" +getTime()
                    + ", this event purely serves as a helper for interpolation";
        }
    }

    @ParametersAreNonnullByDefault
    static class Init extends Speed {
        Init(int time, Train train, Edge startEdge) {
            super(1, train, time, 0, 0, TrainPosition.init(train, startEdge), 0);
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
                    before.getTotalDistance() + distance,
                    before.getPosition().move(distance)
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
        InterpolatableState.Builder stateBuilder() {
            return super.stateBuilder()
                    .terminated(true);
        }
    }

    @ParametersAreNonnullByDefault
    private abstract static class Position extends TrainEvent {
        Position(int index, Train train, int time, int distance, int totalDistance, TrainPosition position) {
            super(index, train, time, distance, totalDistance, position);
        }

        @Nonnull
        @Override
        InterpolatableState.Builder stateBuilder() {
            return super.stateBuilder()
                    .speed(calculateSpeed());
        }

        @Nonnull
        private Speed getSpeedEventBefore() {
            for (int index = getIndex() - 1; index >= 0; index--) {
                TrainEvent event = getTrain().getTrainEvents().get(index);
                if (event instanceof Speed) {
                    return (Speed) event;
                }
            }
            throw new IllegalStateException("Missing init event");
        }

        @Nullable
        private Speed getSpeedEventAfter() {
            List<TrainEvent> events = getTrain().getTrainEvents();
            for (int index = getIndex() + 1; index < events.size(); index++) {
                TrainEvent event = events.get(index);
                if (event instanceof Speed) {
                    return (Speed) event;
                }
            }
            return null;
        }

        private int calculateSpeed() {
            Speed before = getSpeedEventBefore();
            Speed after = getSpeedEventAfter();

            if (after == null) {
                return before.getSpeed();
            }

            int relativeAfterTime = after.getTime() - before.getTime();
            int relativeTime = getTime() - before.getTime();

            int interpolatedSpeed = before.getSpeed();
            int speedDiff = after.getSpeed() - before.getSpeed();
            interpolatedSpeed += (int) (((double) speedDiff) / relativeAfterTime * relativeTime);
            return interpolatedSpeed;
        }
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
                    before.getTotalDistance() + distance,
                    before.getPosition().reachFront(reached));
            this.reached = reached;
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
                    before.getTotalDistance() + distance,
                    before.getPosition().leaveBack(left, distance));
            this.left = left;
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
