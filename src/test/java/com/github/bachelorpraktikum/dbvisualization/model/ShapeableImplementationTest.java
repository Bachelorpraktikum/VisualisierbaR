package com.github.bachelorpraktikum.dbvisualization.model;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public abstract class ShapeableImplementationTest {

    protected abstract Shapeable<?> getShapeable();

    @Test
    public void testGetName() {
        Shapeable<?> shapeable = getShapeable();
        assertNotNull(shapeable.getName());
        assertFalse(shapeable.getName().isEmpty());
    }

    @Test
    public void testCreateShape() {
        Shapeable<?> shapeable = getShapeable();
        assertNotNull(shapeable.createShape());
    }

    @Test
    public void testCreateIconShape() {
        Shapeable<?> shapeable = getShapeable();
        assertNotNull(shapeable.createIconShape());
    }

    @Test
    public void testVisibleStateProperty() {
        Shapeable<?> shapeable = getShapeable();
        assertNotNull(shapeable.visibleStateProperty());
    }
}
