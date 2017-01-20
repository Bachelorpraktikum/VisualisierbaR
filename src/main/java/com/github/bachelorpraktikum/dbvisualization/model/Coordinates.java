package com.github.bachelorpraktikum.dbvisualization.model;

import com.sun.javafx.geom.Vec2d;

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
     * Calculates the normalized vector from this Coordinate to the given one.
     *
     * @param c2 the coordinate the vector points to
     * @return a vector
     */
    public Vec2d normVectorTo(Coordinates c2) {
        Vec2d vec = new Vec2d(c2.getX() - x, c2.getY() - y);
        double vecLength = Math.sqrt(vec.x*vec.x + vec.y*vec.y);
        vec.set(vec.x / vecLength, vec.y / vecLength);
        return vec;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Coordinates that = (Coordinates) obj;

        if (x != that.x) return false;
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
