package com.github.bachelorpraktikum.dbvisualization.view.graph;

import javax.annotation.Nonnull;

import javafx.scene.shape.Shape;

/**
 * Represents an object in the graph which can be visualized by creating a shape for it.
 */
public interface Shapeable {
    /**
     * Creates a <b>new</b> Shape representing this object.
     *
     * @return a shape
     * @throws IllegalStateException if this object is not in a state in which a shape can be
     *                               created
     */
    @Nonnull
    Shape createShape();
}
