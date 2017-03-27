package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.bachelorpraktikum.dbvisualization.model.Element.State;
import com.github.bachelorpraktikum.dbvisualization.model.Element.Type;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.lang.ref.WeakReference;
import javafx.collections.ObservableList;
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
        assertEquals(context, context);
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

    @Test
    public void testGetObservableEvents() {
        Context context = new Context();
        Node node1 = Node.in(context).create("node1", new Coordinates(0, 1));
        Node node2 = Node.in(context).create("node2", new Coordinates(0, 2));
        Edge edge = Edge.in(context).create("edge", 20, node1, node2);
        Train train = Train.in(context).create("train", "t", 10);
        Messages messages = Messages.in(context);
        Element element = Element.in(context)
            .create("element", Type.HauptSignal, node2, State.FAHRT);

        ObservableList<? extends Event> events = context.getObservableEvents();
        assertEquals(2, events.size());

        train.eventFactory().init(0, edge);
        element.addEvent(State.STOP, 2);
        messages.add(5, "test", node1);

        assertEquals(5, events.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetObservableEventsAdd() {
        Context context = new Context();
        context.getObservableEvents().add(null);
    }

    @Test
    public void testAddObject() {
        Context context = new Context();
        Object object = new Object();
        WeakReference<Object> weakObject = new WeakReference<>(object);
        context.addObject(object);

        object = null;
        System.gc();
        assertFalse(weakObject.get() == null);

        context = null;
        System.gc();
        assertTrue(weakObject.get() == null);
    }
}
