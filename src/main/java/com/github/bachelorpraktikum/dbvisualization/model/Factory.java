package com.github.bachelorpraktikum.dbvisualization.model;

import java.util.Collection;
import javax.annotation.Nonnull;

/**
 * <p>Manages instances of a class implementing GraphObject. Does not allow two objects to have the
 * same name.</p>
 *
 * <p>Implementing classes should also provide a create(name, ...) method returning an instance of
 * type T.</p>
 *
 * @param <T> the type of GraphObject which's instances this EdgeFactory manages
 */
public interface Factory<T extends GraphObject<?>> {

    /**
     * Gets the instance with the given unique name.
     *
     * @param name the instance's name
     * @return the instance with this name
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if there is no object associated with the name
     */
    @Nonnull
    T get(String name);

    /**
     * Gets all instances in this {@link Context}.
     *
     * @return all instances
     */
    @Nonnull
    Collection<T> getAll();
}
