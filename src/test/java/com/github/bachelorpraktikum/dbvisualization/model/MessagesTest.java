package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import javafx.collections.ObservableList;
import org.junit.Test;

public class MessagesTest {

    @Test
    public void testInNotNull() {
        assertNotNull(Messages.in(new Context()));
    }

    @Test(expected = NullPointerException.class)
    public void testInNullContext() {
        Messages.in(null);
    }

    @Test
    public void testInSameForSameContext() {
        Context context = new Context();
        Messages messages = Messages.in(context);
        System.gc();

        Messages same = Messages.in(context);
        assertSame(messages, same);
        assertEquals(messages, same);
    }

    @Test
    public void testInDifferentForDifferentContext() {
        Context context1 = new Context();
        Context context2 = new Context();

        Messages messages1 = Messages.in(context1);
        Messages messages2 = Messages.in(context2);

        assertNotSame(messages1, messages2);
        assertNotEquals(messages1, messages2);
    }

    @Test
    public void testGetEventsEmptyNotNull() {
        Messages messages = Messages.in(new Context());
        ObservableList<? extends Event> events = messages.getEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    private Node createRandomNode(Context context) {
        return Node.in(context).create("n", new Coordinates(42, 420));
    }

    @Test
    public void testAdd() {
        Context context = new Context();
        Messages messages = Messages.in(context);
        ObservableList<? extends Event> events = messages.getEvents();

        int time = 100;
        String text = "testasdf";
        Node node = createRandomNode(context);
        messages.add(time, text, node);

        assertEquals(1, events.size());
        Event event = events.get(0);
        assertEquals(time, event.getTime());
        assertTrue(event.getDescription().contains(String.valueOf(time)));
        assertTrue(event.getDescription().contains(text));
        assertTrue(event.getWarnings().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNegativeTime() {
        int time = -1;
        String text = "test";
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages.in(context).add(time, text, node);
    }

    @Test
    public void testAddInvalidTime() {
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages messages = Messages.in(context);

        int firstTime = 10;
        messages.add(firstTime, "test", node);
        Event firstEvent = messages.getEvents().get(0);
        assertEquals(firstTime, firstEvent.getTime());
        assertTrue(firstEvent.getWarnings().isEmpty());

        String text = "test2";
        messages.add(5, text, node);
        Event secondEvent = messages.getEvents().get(1);
        assertEquals(firstTime, secondEvent.getTime());
        assertTrue(secondEvent.getDescription().contains(text));
        assertEquals(1, secondEvent.getWarnings().size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullText() {
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages.in(context).add(10, null, node);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullNode() {
        Messages.in(new Context()).add(10, "test", null);
    }

    @Test
    public void testFireEventsInBetween() {
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages messages = Messages.in(context);
        messages.add(5, "test", node);
        messages.add(10, "test2", node);
        messages.add(15, "test3", node);

        Set<MessageEvent> fired = new HashSet<>();
        MessageEvent.testFire = fired::add;

        assertTrue(messages.fireEventsBetween(n -> null, 0, 10));
        assertEquals(2, fired.size());
    }

    @Test
    public void testFireEventsInBetweenStepping() {
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages messages = Messages.in(context);
        messages.add(5, "test", node);
        messages.add(10, "test2", node);
        messages.add(15, "test3", node);
        messages.add(20, "test4", node);
        messages.add(25, "test5", node);

        Set<MessageEvent> fired = new HashSet<>();
        MessageEvent.testFire = fired::add;

        // first step, fires two events
        assertTrue(messages.fireEventsBetween(n -> null, 0, 14));
        assertEquals(2, fired.size());

        // second step, fires one event
        fired.clear();
        assertTrue(messages.fireEventsBetween(n -> null, 14, 19));
        assertEquals(1, fired.size());
    }

    @Test
    public void testFireEventsInBetweenStartAtEventTime() {
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages messages = Messages.in(context);
        messages.add(5, "test", node);
        messages.add(10, "test2", node);

        Set<MessageEvent> fired = new HashSet<>();
        MessageEvent.testFire = fired::add;

        // startTime is excluded (see JavaDoc)
        assertFalse(messages.fireEventsBetween(n -> null, 5, 8));
        assertTrue(fired.isEmpty());
    }

    @Test
    public void testFireInBetweenNegativeStart() {
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages messages = Messages.in(context);
        messages.add(0, "test", node);
        messages.add(5, "test2", node);

        Set<MessageEvent> fired = new HashSet<>();
        MessageEvent.testFire = fired::add;

        assertTrue(messages.fireEventsBetween(n -> null, -1, 4));
        assertEquals(1, fired.size());
    }

    @Test
    public void testFireInBetweenReversedOrder() {
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages messages = Messages.in(context);
        messages.add(5, "test", node);
        messages.add(10, "test2", node);

        Set<MessageEvent> fired = new HashSet<>();
        MessageEvent.testFire = fired::add;

        assertTrue(messages.fireEventsBetween(n -> null, 8, 12));
        assertEquals(1, fired.size());

        fired.clear();
        assertTrue(messages.fireEventsBetween(n -> null, 2, 6));
        assertEquals(1, fired.size());
    }

    @Test
    public void testFireEventsInBetweenRefire() {
        Context context = new Context();
        Node node = createRandomNode(context);
        Messages messages = Messages.in(context);
        messages.add(5, "test", node);
        messages.add(10, "test2", node);

        Set<MessageEvent> fired = new HashSet<>();
        MessageEvent.testFire = fired::add;

        assertTrue(messages.fireEventsBetween(n -> null, 8, 12));
        assertEquals(1, fired.size());

        fired.clear();
        assertTrue(messages.fireEventsBetween(n -> null, 4, 13));
        assertEquals(2, fired.size());
    }

    @Test
    public void testFireEventsInBetweenEmpty() {
        Context context = new Context();
        Messages messages = Messages.in(context);

        Set<MessageEvent> fired = new HashSet<>();
        MessageEvent.testFire = fired::add;

        assertFalse(messages.fireEventsBetween(n -> null, 0, 1000));
        assertTrue(fired.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testFireEventsInBetweenNullResolver() {
        Messages messages = Messages.in(new Context());
        messages.fireEventsBetween(null, 0, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFireEventsInBetweenInvalidTimes() {
        Messages messages = Messages.in(new Context());
        messages.fireEventsBetween(n -> null, 1000, 0);
    }

    @Test
    public void testToString() {
        Context context = new Context();
        Messages messages = Messages.in(context);
        Node node = createRandomNode(context);
        int time = 10;
        String text = "test1";
        messages.add(time, text, node);

        Event event = messages.getEvents().get(0);
        assertNotNull(event.toString());
        assertTrue(event.toString().contains(String.valueOf(time)));
        assertTrue(event.toString().contains(text));
        assertTrue(event.toString().contains(node.getName()));
    }

    @Test
    public void testIntPairIsPrivate() {
        for (Class<?> type : Messages.class.getClasses()) {
            if (type.getSimpleName().equals("IntPair")) {
                fail("IntPair should not be public");
            }
        }
    }

    @Test
    public void testIntPair() throws ReflectiveOperationException {
        // needs to be done with reflection, because IntPair is - and should be - private
        Class<?> type = Messages.class.getDeclaredClasses()[0];
        Method getKey = type.getDeclaredMethod("getKey");
        Method getValue = type.getDeclaredMethod("getValue");
        Constructor<?> constructor = type.getDeclaredConstructor(int.class, int.class);
        Object intPair = constructor.newInstance(42, 420);

        assertEquals(42, getKey.invoke(intPair));
        assertEquals(420, getValue.invoke(intPair));
    }
}
