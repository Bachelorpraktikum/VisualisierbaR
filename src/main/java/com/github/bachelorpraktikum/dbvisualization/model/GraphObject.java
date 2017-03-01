package com.github.bachelorpraktikum.dbvisualization.model;

import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;

public interface GraphObject<S extends Shape> extends Shapeable<S> {

    /**
     * Gets the unique name of this object.
     *
     * @return the name
     */
    @Nonnull
    String getName();

    /**
     * Gets a human readable name for this object.<br>
     * The default implementation calls {@link #getName()}.
     *
     * @return the readable name
     */
    @Nonnull
    default String getReadableName() {
        return getName();
    }
}
