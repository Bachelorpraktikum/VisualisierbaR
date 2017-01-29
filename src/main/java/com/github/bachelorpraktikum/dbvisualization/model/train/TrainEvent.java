package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.train.InterpolatableState.Builder;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;
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
    @Nonnull
    private final Supplier<TrainPosition> position;
    @Nonnull
    private final List<String> warnings;

    private TrainEvent(int index,
            Train train,
            int time,
            int distance,
            int totalDistance,
            Supplier<TrainPosition> position) {
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

    abstract int getSpeed();

    @Nullable
    final TrainPosition getPosition() {
        return position.get();
    }

    @Nonnull
    InterpolatableState.Builder stateBuilder() {
        return new InterpolatableState.Builder(getTrain())
                .index(getIndex())
                .time(getTime())
                .distance(getTotalDistance())
                .speed(getSpeed())
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
                    () -> before.getPosition().move(distance)
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

        private Speed(int index, Train train, int time, int distance, int totalDistance, @Nullable Supplier<TrainPosition> position, int speed) {
            super(index, train, time, distance, totalDistance, position);
            this.speed = speed;
        }

        Speed(TrainEvent before, int time, int distance, int speed) {
            this(before.getIndex() + 1,
                    before.getTrain(),
                    time,
                    distance,
                    before.getTotalDistance() + distance,
                    () -> before.getPosition().move(distance),
                    speed
            );
        }

        @Override
        final int getSpeed() {
            return speed;
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
            super(0, train, Context.INIT_STATE_TIME, 0, 0, () -> null, 0);
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
            super(1,
                    train,
                    time,
                    0,
                    0,
                    () -> Init.getPositionWithLookahead(train, startEdge),
                    0);

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
            for(Event event : train.getEvents()) {
                if(event instanceof Reach) {
                    return (Reach) event;
                }
            }
            return null;
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
                    () -> before.getPosition().move(distance)
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

        Position(int index, Train train, int time, int distance, int totalDistance, Supplier<TrainPosition> position) {
            super(index, train, time, distance, totalDistance, position);
        }

        int getSpeedBefore() {
            return getTrain().getTrainEvents().get(getIndex() - 1).getState().getSpeed();
        }

        @Override
        int getSpeed() {
            List<TrainEvent> events = getTrain().getTrainEvents();
            if(getIndex() == events.size() - 1) {
                return getSpeedBefore();
            }
            Speed speedEvent = null;
            for(TrainEvent event : events.subList(getIndex() + 1, events.size())) {
                // look for a speed event on the current front edge
                if(event instanceof Speed) {
                    speedEvent = (Speed) event;
                    break;
                } else if(event instanceof Reach) {
                    // there is no speed event on this edge
                    return getSpeedBefore();
                }
            }
            if(speedEvent == null) {
                return getSpeedBefore();
            }

            TrainEvent reachEvent = null;
            ListIterator<TrainEvent> trainEvents = events.listIterator(getIndex());
            while(trainEvents.hasPrevious()) {
                TrainEvent event = trainEvents.previous();
                if (event instanceof Reach || event instanceof Init) {
                    reachEvent = event;
                    break;
                }
            }
            if (reachEvent == null) {
                // this should never happen, because the event factory does not allow to insert
                // any event before an init event.
                throw new IllegalStateException();
            }

            int startTime = reachEvent.getTime();
            int startSpeed = reachEvent.getSpeed();
            int endTime = speedEvent.getTime();
            int endSpeed = speedEvent.getSpeed();

            int targetTime = getTime() - startTime;
            int timeDiff = endTime - startTime;
            int speedDiff = endSpeed - startSpeed;

            int addedSpeed = (int) (((double) speedDiff) / timeDiff) * targetTime;
            return startSpeed + addedSpeed;
        }
    }

    @ParametersAreNonnullByDefault
    static class Reach extends Position {
        @Nonnull
        private final Edge reached;
        private final int speed;

        Reach(TrainEvent before, int time, int distance, Edge reached) {
            super(before.getIndex() + 1,
                    before.getTrain(),
                    time,
                    distance,
                    before.getTotalDistance() + distance,
                    () -> before.getPosition().reachFront(reached));
            this.reached = reached;
            this.speed = getSpeedBefore();
        }

        Edge getReached() {
            return reached;
        }

        @Override
        int getSpeed() {
            return speed;
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
                    () -> before.getPosition().leaveBack(left, distance));
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
