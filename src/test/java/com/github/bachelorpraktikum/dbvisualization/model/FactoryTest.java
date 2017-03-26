package com.github.bachelorpraktikum.dbvisualization.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public abstract class FactoryTest<T extends GraphObject<?>> {

    private Context context;

    /**
     * Gets the factory for T.
     *
     * @param context the Context for which to return the factory
     * @return a factory instance
     */
    protected abstract Factory<T> getFactory(Context context);

    /**
     * Create a unique instance of T.
     *
     * @param context the Context in which to create the instance
     * @return an instance of T
     */
    protected abstract T createRandom(Context context);

    /**
     * Call create() with the same arguments with which the given object is created.
     *
     * @param context the Context in which to recreate the object
     * @param t the object to recreate
     * @return the object returned by create()
     */
    protected abstract T createSame(Context context, T t);

    /**
     * <p>Creates a new instance of T with the same arguments with which t was created. Only the
     * argument with index "argIndex" should be different than the original.</p>
     *
     * <p>The argument index is 0-based. This method will never be called with an invalid argument
     * index or an argument index of 0. 0 index is not tested because it should always be the unique
     * name.</p>
     *
     * @param context the Context in which to recreate
     * @param t the object to "almost duplicate"
     * @param argIndex the index of the argument to change
     */
    public abstract void testCreateDifferentArg(Context context, T t, int argIndex);

    private Factory<T> getFactory() {
        return getFactory(context);
    }

    private T createRandom() {
        return createRandom(context);
    }

    private T createSame(T t) {
        return createSame(context, t);
    }

    private void testCreateDifferentArg(T t, int argIndex) {
        testCreateDifferentArg(context, t, argIndex);
    }

    @Before
    public void createContext() {
        context = new Context();
    }

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

    @Test
    public void testForMemoryLeak() {
        Context context = new Context();
        WeakReference<Context> weakContext = new WeakReference<>(context);
        WeakReference<Factory<T>> weakFactory = new WeakReference<>(getFactory(context));
        T t = createRandom(context);
        WeakReference<T> weakT = new WeakReference<>(t);

        System.gc();
        assertNotNull(weakContext.get());
        assertNotNull(weakT.get());
        assertNotNull(weakFactory.get());

        context = null;
        System.gc();
        assertNull(weakContext.get());

        t = null;
        System.gc();
        assertNull(weakFactory.get());
        assertNull(weakT.get());
    }
}
