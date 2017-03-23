package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ContextTest {

    @Test
    public void testCreate() {
        // Just assure no exceptions are thrown
        Context context = new Context();
    }

    @Test
    public void testEqualsReflexive() {
        Context context = new Context();
        assertTrue(context.equals(context));
    }

    @Test
    public void testEqualsSymmetry() {
        Context context1 = new Context();
        Context context2 = new Context();

        assertFalse(context1.equals(context2));
        assertFalse(context2.equals(context1));
    }

    @Test
    public void testEqualsTransitive() {
        Context context1 = new Context();
        Context context2 = new Context();
        Context context3 = new Context();

        assertFalse(context1.equals(context2));
        assertFalse(context2.equals(context3));
        assertFalse(context1.equals(context3));
    }
}
