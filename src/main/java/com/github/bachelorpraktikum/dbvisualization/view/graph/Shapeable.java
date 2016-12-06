package com.github.bachelorpraktikum.dbvisualization.view.graph;

import javax.annotation.Nonnull;

import javafx.scene.shape.Shape;

/**
 * Represents an object in the graph which can be visualized by creating a shape for it.
 */
public interface Shapeable {
    /**
     * Creates a Shape representing this object. If the represented object hasn't changed, the Shape
     * can be an instance created for a previous call of this method.
     *
     * @return a shape
     * @throws IllegalStateException if no Shape can be created in the current context
     */
    @Nonnull
    Shape createShape();
}
