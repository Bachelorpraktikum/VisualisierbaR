package com.github.bachelorpraktikum.dbvisualization.model;

import javafx.geometry.Point2D;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a position in a graph.<br>
 * The coordinates are an abstract representation of the position, not an absolute location.
 */
@Immutable
@ParametersAreNonnullByDefault
public final class Coordinates {

    private final int x;
    private final int y;

    /**
     * Creates a new Coordinates instance.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @throws IllegalArgumentException if a coordinate is negative
     */
    public Coordinates(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinate can't be negative");
        }
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate.
     *
     * @return a positive int
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate.
     *
     * @return a positive int
     */
    public int getY() {
        return y;
    }

    /**
     * Returns a {@link Point2D} representation of this coordinate.
     * This method performs no translation.
     *
     * @return a Point2D
     */
    public Point2D toPoint2D() {
        return new Point2D(getX(), getY());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Coordinates that = (Coordinates) obj;

        if (x != that.x) {
            return false;
        }
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "Coordinates{"
            + "x=" + x
            + ", y=" + y
            + '}';
    }
}
