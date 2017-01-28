package com.github.bachelorpraktikum.dbvisualization.model;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicReference;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ElementTest {

    private Context context;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        this.context = new Context();
    }

    private Element createElement() {
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        return Element.in(context).create("element", Element.Type.HauptSignalImpl, node, Element.State.NOSIG);
    }

    @Test
    public void testInstanceManager() {
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        Element element = Element.in(context).create("element", Element.Type.HauptSignalImpl, node, Element.State.NOSIG);

        assertSame(element, Element.in(context).get(element.getName()));
        assertSame(element, Element.in(context).create(element.getName(), Element.Type.HauptSignalImpl, node, Element.State.NOSIG));
        assertTrue(Element.in(context).getAll().contains(element));
    }

    @Test
    public void testInstanceManagerInvalidName() {
        expected.expect(IllegalArgumentException.class);
        Element.in(context).get("e");
    }

    @Test
    public void testInstanceManagerExistsDifferentType() {
        Element element = createElement();

        expected.expect(IllegalArgumentException.class);
        Element.in(context).create(element.getName(), Element.Type.GefahrenPunktImpl, element.getNode(), element.getState());
    }

    @Test
    public void testInstanceManagerExistsDifferentNode() {
        Element element = createElement();
        Node otherNode = Node.in(context).create("otherNode", new Coordinates(10, 10));

        expected.expect(IllegalArgumentException.class);
        Element.in(context).create(element.getName(), element.getType(), otherNode, element.getState());
    }

    @Ignore("the initial state is currently not checked in the factory")
    @Test
    public void testInstanceManagerExistsDifferentState() {
        Element element = createElement();

        expected.expect(IllegalArgumentException.class);
        Element.in(context).create(element.getName(), element.getType(), element.getNode(), Element.State.FAHRT);
    }

    @Test
    public void testAddEvent() {
        Element element = createElement();

        int time = 1;
        for (Element.State state : Element.State.values()) {
            element.addEvent(state, time);

            Element.in(context).setTime(time);
            assertEquals(state, element.getState());

            Element.in(context).setTime(time + 1);
            assertEquals(state, element.getState());

            Element.in(context).setTime(0);
            time += 1;
        }
    }

    @Test
    public void testAddEventCurrentTime() {
        Element element = createElement();

        Element.State initState = Element.State.STOP;
        Element.State newState = Element.State.FAHRT;

        element.addEvent(initState, 1);
        Element.in(context).setTime(2);
        assertEquals(initState, element.getState());

        // Since the current time of all events is 1,
        // the element should be updated without a call to Element.in(context).setTime()
        element.addEvent(newState, 2);
        assertEquals(newState, element.getState());
    }

    @Test
    public void testAddEventNegativeTime() {
        Element element = createElement();

        // Should get corrected to time 0
        element.addEvent(Element.State.FAHRT, -1);
        boolean hasZeroTime = false;
        boolean zeroTimeHasWarnings = false;
        for (Event event : Element.in(context).getEvents()) {
            assertFalse(event.getTime() < Context.INIT_STATE_TIME);
            if (event.getTime() == 0) {
                hasZeroTime = true;
                if (!event.getWarnings().isEmpty()) {
                    zeroTimeHasWarnings = true;
                }
            }
        }
        assertTrue(hasZeroTime);
        assertTrue(zeroTimeHasWarnings);
    }

    @Test
    public void testAddEventPast() {
        Element element = createElement();
        element.addEvent(Element.State.FAHRT, 10);

        // Expected to be added at time 10 with warning
        element.addEvent(Element.State.STOP, 9);
        Event event = Element.in(context).getEvents().get(1);
        assertEquals(10, event.getTime());
        assertFalse(event.getWarnings().isEmpty());
    }

    @Test
    public void testAddEventNull() {
        Element element = createElement();

        expected.expect(NullPointerException.class);
        element.addEvent(null, 10);
    }

    @Test
    public void testGetName() {
        Element element = createElement();
        assertEquals("element", element.getName());
    }

    @Test
    public void testGetNode() {
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        Element element = Element.in(context).create("element", Element.Type.HauptSignalImpl, node, Element.State.NOSIG);

        assertEquals(node, element.getNode());
    }

    @Test
    public void testGetState() {
        Element element = createElement();
        ReadOnlyProperty<Element.State> stateProperty = element.stateProperty();

        assertEquals(stateProperty.getValue(), element.getState());

        element.addEvent(Element.State.FAHRT, 10);
        Element.in(context).setTime(10);

        assertEquals(Element.State.FAHRT, element.getState());
    }

    @Test
    public void testStatePropertyCastToWritable() {
        Element element = createElement();
        ReadOnlyProperty<Element.State> stateReadOnlyProperty = element.stateProperty();

        expected.expect(ClassCastException.class);
        Property<Element.State> property = (Property<Element.State>) stateReadOnlyProperty;
    }

    @Test
    public void testStatePropertySame() {
        Element element = createElement();
        ReadOnlyProperty<Element.State> stateProperty = element.stateProperty();

        element.addEvent(Element.State.FAHRT, 100);
        Element.in(context).setTime(10);
        Element.in(context).setTime(200);

        assertSame(stateProperty, element.stateProperty());
    }

    @Test
    public void testStatePropertyCalled() {
        Element element = createElement();
        ReadOnlyProperty<Element.State> stateProperty = element.stateProperty();

        AtomicReference<Element.State> called = new AtomicReference<>();
        stateProperty.addListener((observable, oldValue, newValue) -> called.set(newValue));

        Element.State state = Element.State.FAHRT;
        element.addEvent(state, 10);

        Element.in(context).setTime(10);
        assertEquals(state, called.get());
    }

    @Test
    public void testGetType() {
        Element element = createElement();
        assertEquals(Element.Type.HauptSignalImpl, element.getType());
    }
}
