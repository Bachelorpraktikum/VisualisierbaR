package com.github.bachelorpraktikum.dbvisualization.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Test;

public abstract class FactoryTest<T extends GraphObject<?>> {

    protected abstract Factory<T> getFactory();

    protected abstract T createRandom();

    protected abstract T createSame(T t);

    public abstract void testCreateDifferentArg(T t, int arg);

    @Test
    public void testGet() {
        T t = createRandom();
        assertEquals(t, getFactory().get(t.getName()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInvalid() {
        getFactory().get("invalid");
    }

    @Test(expected = NullPointerException.class)
    public void testGetNull() {
        getFactory().get(null);
    }

    @Test
    public void testCreateSame() {
        T t = createRandom();
        T same = createSame(t);
        assertEquals(t, same);
        assertSame(t, same);
    }

    @Test
    public void testGetAllNotNull() {
        assertNotNull(getFactory().getAll());
    }

    @Test
    public void testGetAllContainsRandom() {
        T t = createRandom();
        assertTrue(getFactory().getAll().contains(t));
    }

    @Test
    public void testHasCreate() {
        assertTrue(Arrays.stream(getFactory().getClass().getDeclaredMethods())
            .map(Method::getName)
            .anyMatch(name -> name.equals("create"))
        );
    }

    @Test
    public void testInvalidRecreation() {
        T t = createRandom();
        for (Method method : getFactory().getClass().getDeclaredMethods()) {
            if (method.getName().equals("create")) {
                for (int counter = 1; counter < method.getParameterCount(); counter++) {
                    try {
                        testCreateDifferentArg(t, counter);
                        fail("Expected to throw IllegalArgumentException with invalid argument no. "
                            + counter);
                    } catch (IllegalArgumentException expected) {
                        // this should happen
                    }
                }
            }
        }
    }
}
