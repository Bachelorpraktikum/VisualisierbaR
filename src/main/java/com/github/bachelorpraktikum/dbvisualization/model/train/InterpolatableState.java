package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@ParametersAreNonnullByDefault
final class InterpolatableState implements Train.State {

    @Nonnull
    private final Train train;
    private final int index;
    private final int time;
    private final int distance;
    @Nullable
    private final TrainPosition position;
    private final int speed;
    private final boolean terminated;
    private final boolean initialized;

    private InterpolatableState(Train train,
        int index,
        boolean terminated,
        boolean initialized,
        int time,
        int distance,
        @Nullable TrainPosition position,
        int speed) {
        this.train = train;
        this.index = index;
        this.terminated = terminated;
        this.initialized = initialized;
        this.time = time;
        this.distance = distance;
        this.position = position;
        this.speed = speed;
    }

    @ParametersAreNonnullByDefault
    static class Builder {

        @Nonnull
        private final Train train;

        private int time = Integer.MIN_VALUE;
        private int speed = -1;
        private boolean terminated = false;
        private boolean initialized = true;
        private int index = -1;
        private int distance = -1;
        @Nullable
        private TrainPosition position = null;

        /**
         * Creates a new builder for the given train.
         *
         * @param train the {@link Train}
         * @throws NullPointerException if train is null
         */
        Builder(Train train) {
            this.train = Objects.requireNonNull(train);
        }

        /**
         * Sets the time since the simulation start.
         *
         * @param time the time in milliseconds
         * @return this Builder
         * @throws IllegalArgumentException if time is negative
         */
        Builder time(int time) {
            if (time < Context.INIT_STATE_TIME) {
                throw new IllegalArgumentException("time is negative");
            }
            this.time = time;
            return this;
        }

        /**
         * Sets the speed of the train.
         *
         * @param speed the speed in TODO find out speed unit
         * @return this Builder
         * @throws IllegalArgumentException if speed is negative
         */
        Builder speed(int speed) {
            if (speed < 0) {
                throw new IllegalArgumentException(String.format("speed (%d) is negative", speed));
            }
            this.speed = speed;
            return this;
        }

        /**
         * Defines whether the train is terminated.<br>
         * Optional, defaults to false.
         *
         * @param terminated whether the train is terminated
         * @return this Builder
         */
        Builder terminated(boolean terminated) {
            this.terminated = terminated;
            return this;
        }

        /**
         * Defines whether the train has been initialized.<br>
         * Optional, defaults to true.
         *
         * @param initialized whether the train is initialized
         * @return this Builder
         */
        Builder initialized(boolean initialized) {
            this.initialized = initialized;
            return this;
        }

        /**
         * Sets the index of the last event that is represented by this state.
         *
         * @param index the index in the Train events list
         * @return this Builder
         * @throws IllegalArgumentException if index is negative
         */
        Builder index(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index is negative");
            }
            this.index = index;
            return this;
        }

        /**
         * Sets the total distance the train has travelled since the simulation start.
         *
         * @param distance the total distance in meters
         * @return this Builder
         * @throws IllegalArgumentException if distance is negative
         */
        Builder distance(int distance) {
            if (distance < 0) {
                throw new IllegalArgumentException("distance is negative");
            }
            this.distance = distance;
            return this;
        }

        /**
         * Sets the position the train is at.
         * Can be null if the train is not initialized.
         *
         * @param position the position
         * @return this Builder
         */
        Builder position(@Nullable TrainPosition position) {
            this.position = position;
            return this;
        }


        /**
         * Builds an {@link InterpolatableState} based on the configuration of this Builder.<br>
         * <h3>The following methods are required to be called before calling this one:</h3>
         * <ul>
         * <li>{@link #time(int)}</li>
         * <li>{@link #speed(int)}</li>
         * <li>{@link #index(int)}</li>
         * <li>{@link #distance(int)}</li>
         * <li>{@link #position(TrainPosition)}</li>
         * </ul>
         *
         * @return an instance of InterpolatableState
         * @throws IllegalStateException if any of the required values have not been set.
         */
        InterpolatableState build() {
            if (time < Context.INIT_STATE_TIME
                || speed < 0
                || index < 0
                || distance < 0
                || (initialized && position == null)) {
                throw new IllegalStateException("not all required values set");
            }
            return new InterpolatableState(
                train,
                index,
                terminated,
                initialized,
                time,
                distance,
                position,
                speed
            );
        }

        @Override
        public String toString() {
            return "Builder{"
                + "train=" + train
                + ", time=" + time
                + ", speed=" + speed
                + ", terminated=" + terminated
                + ", initialized=" + initialized
                + ", index=" + index
                + ", distance=" + distance
                + ", position=" + position
                + '}';
        }
    }

    @Nonnull
    final InterpolatableState interpolate(int targetTime, InterpolatableState other) {
        if (compareTo(other) > 0) {
            return other.interpolate(targetTime, this);
        }

        if (getTime() > targetTime) {
            throw new IllegalArgumentException("time not between states");
        }

        if (!isInitialized()) {
            return new Builder(getTrain())
                .index(getIndex())
                .time(targetTime)
                .distance(0)
                .speed(0)
                .initialized(false)
                .position(null)
                .build();
        }

        int relativeTargetTime = targetTime - getTime();
        int relativeOtherTime = other.getTime() - getTime();

        int interpolatedSpeed = getSpeed();
        // interpolate speed
        if (!other.isTerminated() && getSpeed() != other.getSpeed()) {
            int speedDiff = other.getSpeed() - getSpeed();
            interpolatedSpeed += (int) (((double) speedDiff) / relativeOtherTime
                * relativeTargetTime);
        }

        int interpolatedDistance = getTotalDistance();
        TrainPosition interpolatedPosition = getPosition();
        if (!getPosition().equals(other.getPosition())) {
            int distanceDiff = other.getTotalDistance() - getTotalDistance();
            int interpolationDistance = (int) (((double) distanceDiff) / relativeOtherTime
                * relativeTargetTime);
            interpolatedDistance += interpolationDistance;
            interpolatedPosition = interpolatedPosition
                .interpolationMove(interpolationDistance, other.getPosition().getFrontEdge());
        }

        return new Builder(getTrain())
            .index(getIndex())
            .time(targetTime)
            .distance(interpolatedDistance)
            .speed(interpolatedSpeed)
            .position(interpolatedPosition)
            .build();
    }

    @Nonnull
    @Override
    public Train getTrain() {
        return train;
    }

    @Nonnull
    @Override
    public TrainPosition getPosition() {
        if (!isInitialized() || position == null) {
            throw new IllegalStateException("Tried to get position of uninitialized train");
        }
        return position;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public int getTotalDistance() {
        return distance;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InterpolatableState)) {
            return false;
        }

        InterpolatableState that = (InterpolatableState) obj;

        if (time != that.time) {
            return false;
        }
        if (getSpeed() != that.getSpeed()) {
            return false;
        }
        if (!train.equals(that.train)) {
            return false;
        }
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
}
