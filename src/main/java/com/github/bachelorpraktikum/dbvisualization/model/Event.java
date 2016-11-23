package com.github.bachelorpraktikum.dbvisualization.model;

import javax.annotation.Nonnull;

/**
 * Represents one or more events that happened at a specific point in time.
 */
public interface Event extends Comparable<Event> {

    /**
     * Gets the time this event occured.
     *
     * @return the time in milliseconds since simulation start
     */
    int getTime();

    /**
     * Gets a string representation of this event.
     *
     * @return a potentially human readable description
     */
    @Nonnull
    String getDescription();

    default int compareTo(@Nonnull Event other) {
        return Integer.compare(getTime(), other.getTime());
    }
}
