package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.bachelorpraktikum.dbvisualization.model.Element.ElementFactory;
import com.github.bachelorpraktikum.dbvisualization.model.Element.State;
import com.github.bachelorpraktikum.dbvisualization.model.Element.Type;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ElementTest extends FactoryTest<Element> {

    private Context context;
    private int counter;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        this.context = new Context();
    }

    private Node createNode() {
        Coordinates coordinates = new Coordinates(counter++, counter++);
        return Node.in(context).create("node" + counter++, coordinates);
    }

    private Element createElement() {
        Node node = createNode();
        return Element.in(context)
            .create("element" + counter++, Element.Type.HauptSignal, node, Element.State.NOSIG);
    }

    @Test
    public void testInitialState() {
        Node node = Node.in(context).create("node", new Coordinates(10, 10));

        Element.State initState = Element.State.FAHRT;
        Element element = Element.in(context)
            .create("e", Element.Type.GefahrenPunkt, node, initState);

        Element.State otherState = Element.State.STOP;
        int otherTime = 20;
        element.addEvent(otherState, otherTime);

        assertEquals(initState, element.getState());

        Element.in(context).setTime(10);
        assertEquals(initState, element.getState());

        Element.in(context).setTime(30);
        assertEquals(otherState, element.getState());
        Element.in(context).setTime(0);
        assertEquals(initState, element.getState());
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
    public void testAddTwoEventsSameTime() {
        Element element = createElement();

        int time = 20;

        element.addEvent(Element.State.FAHRT, time);
        element.addEvent(Element.State.STOP, time);

        Element.in(context).setTime(time);
        assertEquals(Element.State.STOP, element.getState());
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
        // the index of the new event is 2, because the init event has index 0
        Event event = Element.in(context).getEvents().get(2);
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
        Element element = getFactory()
            .create("element", Type.GefahrenPunkt, createNode(), State.NOSIG);
        assertEquals("element", element.getName());
    }

    @Test
    public void testGetNode() {
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        Element element = Element.in(context)
            .create("element", Element.Type.HauptSignal, node, Element.State.NOSIG);

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
        assertEquals(Element.Type.HauptSignal, element.getType());
    }

    @Override
    protected ElementFactory getFactory() {
        return Element.in(context);
    }

    @Override
    protected Element createRandom() {
        return createElement();
    }

    @Override
    protected Element createSame(Element element) {
        return getFactory().create(
            element.getName(),
            element.getType(),
            element.getNode(),
            element.getState()
        );
    }

    @Override
    public void testCreateDifferentArg(Element element, int arg) {
        switch (arg) {
            case 1:
                Element.Type[] values = Element.Type.values();
                int newIndex = (element.getType().ordinal() + 1) % values.length;
                Element.Type type = values[newIndex];
                getFactory().create(element.getName(), type, element.getNode(), element.getState());
                break;
            case 2:
                getFactory()
                    .create(element.getName(), element.getType(), createNode(), element.getState());
                break;
            case 3:
                getFactory()
                    .create(element.getName(), element.getType(), element.getNode(), State.FAHRT);
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
