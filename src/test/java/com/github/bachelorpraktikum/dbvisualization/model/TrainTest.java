package com.github.bachelorpraktikum.dbvisualization.model;

import com.github.bachelorpraktikum.dbvisualization.model.train.Train;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TrainTest {
    private Context context;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        context = new Context();
    }

    @Test
    public void testInstanceManager() {
        Train train = Train.in(context).create("t", "train", 100);
        assertSame(train, Train.in(context).get(train.getName()));
        assertSame(train, Train.in(context).create(train.getName(), train.getReadableName(), train.getLength()));
        assertTrue(Train.in(context).getAll().contains(train));
    }

    @Test
    public void testInstanceManagerInvalidName() {
        expected.expect(IllegalArgumentException.class);
        Train.in(context).get("t");
    }

    @Test
    public void testInstanceManagerExistsDifferentLength() {
        String name = "t";
        String readableName = "train";
        Train.in(context).create(name, readableName, 10);
        expected.expect(IllegalArgumentException.class);
        Train.in(context).create(name, readableName, 20);
    }

    @Test
    public void testInstanceManagerExistsDifferentReadableName() {
        String name = "t";
        String readableName = "train";
        Train.in(context).create(name, readableName, 10);
        expected.expect(IllegalArgumentException.class);
        Train.in(context).create(name, "trainz", 10);
    }

    private Edge[] createEdges(Integer edgeLength, Integer... edgeLengths) {
        Edge[] result = new Edge[edgeLengths.length + 1];
        Node startNode = Node.in(context).create("startNode", new Coordinates(0, 0));
        Node previousNode = Node.in(context).create("n0", new Coordinates(0, 1));
        result[0] = Edge.in(context).create("e0", edgeLength, startNode, previousNode);

        for (int i = 1; i <= edgeLengths.length; i++) {
            Node node = Node.in(context).create("n" + i, new Coordinates(i, 1));
            result[i] = Edge.in(context).create("e" + i, edgeLengths[i - 1], previousNode, node);
            previousNode = node;
        }

        return result;
    }

    @Test
    public void testName() {
        Train train = Train.in(context).create("t", "train", 100);
        assertEquals("t", train.getName());
    }

    @Test
    public void testReadableName() {
        Train train = Train.in(context).create("t", "train", 100);
        assertEquals("train", train.getReadableName());
    }

    @Test
    public void testLength() {
        Train train = Train.in(context).create("t", "train", 50);
        assertEquals(50, train.getLength());
    }

    @Test
    public void testGetStateNegativeTimeBeforeInit() {
        Train train = Train.in(context).create("t", "train", 20);
        Edge edge = createEdges(30)[0];
        train.eventFactory().init(0, edge);

        expected.expect(IllegalArgumentException.class);
        train.getState(Context.INIT_STATE_TIME - 1);
    }

    @Test
    public void testGetStateNotInitialized() {
        Train train = Train.in(context).create("t", "train", 20);
        assertFalse(train.getState(0).isInitialized());
        assertFalse(train.getState(100).isInitialized());

        expected.expect(IllegalStateException.class);
        train.getState(0).getPosition();
    }

    @Test
    public void testInitStart() {
        Train train = Train.in(context).create("t", "train", 20);
        Edge edge = createEdges(30)[0];
        train.eventFactory().init(0, edge);

        Train.State state = train.getState(0);
        assertEquals(train.getLength(), state.getPosition().getFrontDistance());
        assertEquals(edge.getLength(), state.getPosition().getBackDistance());
        assertEquals(edge, state.getPosition().getFrontEdge());
        assertEquals(edge, state.getPosition().getBackEdge());
        assertEquals(0, state.getTotalDistance());
        assertTrue(state.isInitialized());
    }

    @Test
    public void testInitMiddle() {
        Train train = Train.in(context).create("t", "train", 20);
        Edge edge2 = createEdges(30, 40, 50)[1];
        train.eventFactory().init(0, edge2);

        Train.State state = train.getState(0);
        assertEquals(train.getLength(), state.getPosition().getFrontDistance());
        assertEquals(edge2, state.getPosition().getFrontEdge());
        assertEquals(edge2.getLength(), state.getPosition().getBackDistance());
        assertEquals(0, state.getTotalDistance());
        assertTrue(state.isInitialized());
    }

    @Test
    public void testSpeed() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);

        Train.State initState = train.getState(0);

        int time = 10;
        int distance = 20;
        int speed = 30;
        train.eventFactory().speed(time, distance, speed);

        assertEquals(initState, train.getState(0));

        Train.State state = train.getState(10);
        assertFalse(state.isTerminated());
        assertEquals(train, state.getTrain());
        assertEquals(time, state.getTime());
        assertEquals(edge, state.getPosition().getFrontEdge());
        assertEquals(train.getLength() + distance, state.getPosition().getFrontDistance());
        assertEquals(speed, state.getSpeed());
        assertEquals(distance, state.getTotalDistance());
        assertTrue(state.isInitialized());

        assertEquals(state, train.getState(20));
    }

    @Test
    public void testReach() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(20, 40);
        train.eventFactory().init(0, edges[0]);

        train.eventFactory().reach(10, edges[1], 10);
        Train.State state = train.getState(10);
        assertEquals(10, state.getTime());
        assertEquals(0, state.getPosition().getFrontDistance());
        assertEquals(edges[1], state.getPosition().getFrontEdge());
        assertEquals(10, state.getTotalDistance());

        assertEquals(edges[0], state.getPosition().getBackEdge());
        assertEquals(10, state.getPosition().getBackDistance());
        assertFalse(state.isTerminated());
    }

    @Test
    public void testLeave() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(20, 40);
        train.eventFactory().init(0, edges[0]);

        train.eventFactory().reach(10, edges[1], 10);
        train.eventFactory().leave(20, edges[1], 10);
        Train.State state = train.getState(20);
        assertEquals(20, state.getTime());
        assertEquals(edges[1].getLength(), state.getPosition().getBackDistance());
        assertEquals(edges[1], state.getPosition().getBackEdge());

        assertEquals(edges[1], state.getPosition().getFrontEdge());
        assertEquals(10, state.getPosition().getFrontDistance());
        assertEquals(20, state.getTotalDistance());
        assertFalse(state.isTerminated());
    }

    @Test
    public void testTerminate() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(20)[0];
        train.eventFactory().init(0, edge);

        train.eventFactory().speed(5, 2, 20);
        train.eventFactory().terminate(10, 2);
        Train.State state = train.getState(10);
        assertEquals(10, state.getTime());
        assertTrue(state.isTerminated());
        assertEquals(14, state.getPosition().getFrontDistance());
        assertEquals(4, state.getTotalDistance());
        assertEquals(20, state.getSpeed());
    }

    @Test
    public void testSpeedInterpolation() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);

        train.eventFactory().speed(20, 5, 20);

        assertEquals(12, train.getState(12).getSpeed());
        assertEquals(15, train.getState(15).getSpeed());
        assertEquals(17, train.getState(17).getSpeed());
    }

    @Test
    public void testSpeedInterpolationWithNonSpeedInbetween() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(15, 50);
        train.eventFactory().init(0, edges[0]);

        train.eventFactory().reach(10, edges[1], 5);
        train.eventFactory().leave(15, edges[1], 10);
        train.eventFactory().move(17, 5);
        train.eventFactory().speed(20, 5, 10);

        assertEquals(2, train.getState(12).getSpeed());
        assertEquals(5, train.getState(15).getSpeed());
        assertEquals(7, train.getState(17).getSpeed());
    }

    @Test
    public void testSpeedInterpolationBeforeReach() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(15, 50);
        train.eventFactory().init(0, edges[0]);

        train.eventFactory().reach(10, edges[1], 5);
        train.eventFactory().speed(20, 5, 10);

        assertEquals(0, train.getState(0).getSpeed());
        assertEquals(0, train.getState(9).getSpeed());
    }

    @Test
    public void testPositionInterpolation() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);

        train.eventFactory().speed(10, 10, 5);

        assertEquals(11, train.getState(1).getPosition().getFrontDistance());
        assertEquals(13, train.getState(3).getPosition().getFrontDistance());
        assertEquals(15, train.getState(5).getPosition().getFrontDistance());
    }

    @Test
    public void testDistanceInterpolation() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);

        train.eventFactory().speed(10, 10, 10);
        train.eventFactory().speed(20, 10, 20);

        assertEquals(10, train.getState(10).getTotalDistance());
        assertEquals(20, train.getState(20).getTotalDistance());
        assertEquals(12, train.getState(12).getTotalDistance());
        assertEquals(15, train.getState(15).getTotalDistance());
        assertEquals(18, train.getState(18).getTotalDistance());
    }

    @Test
    public void testMergeEvents() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(20, 20);
        train.eventFactory().init(0, edges[0]);

        train.eventFactory().reach(10, edges[1], 10);
        train.eventFactory().speed(10, 5, 10);

        Train.State state = train.getState(10);
        assertEquals(5, state.getPosition().getFrontDistance());
        assertEquals(10, state.getSpeed());
        assertEquals(15, state.getTotalDistance());
    }

    @Test
    public void testGetStateWithStart() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(20, 20);
        train.eventFactory().init(0, edges[0]);

        train.eventFactory().reach(10, edges[1], 10);
        train.eventFactory().speed(20, 5, 10);

        Train.State wanted = train.getState(20);

        Train.State state = train.getState(15);
        assertEquals(wanted, train.getState(20, state));
    }

    @Test
    public void testGetStateWithStartAfterWanted() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(20, 20);
        train.eventFactory().init(0, edges[0]);

        train.eventFactory().reach(10, edges[1], 10);
        train.eventFactory().speed(20, 5, 10);

        Train.State wanted = train.getState(15);

        Train.State state = train.getState(20);
        assertEquals(wanted, train.getState(15, state));
    }

    @Test
    public void testGetStateWithStartNull() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(20);
        train.eventFactory().init(0, edges[0]);

        expected.expect(NullPointerException.class);
        train.getState(10, null);
    }

    @Test
    public void testGetStateWithStartWrongTrain() {
        Train train = Train.in(context).create("t", "train", 10);
        Train train2 = Train.in(context).create("t2", "train2", 20);
        Edge[] edges = createEdges(20, 20);

        train.eventFactory().init(0, edges[0]);
        train2.eventFactory().init(0, edges[0]);

        Train.State state = train2.getState(5);
        expected.expect(IllegalArgumentException.class);
        train.getState(10, state);
    }

    @Test
    public void testInitAlreadyInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge[] edges = createEdges(20, 20);
        train.eventFactory().init(0, edges[0]);
        expected.expect(IllegalStateException.class);
        train.eventFactory().init(0, edges[1]);
    }

    @Test
    public void testSpeedNotInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        expected.expect(IllegalStateException.class);
        train.eventFactory().speed(10, 10, 10);
    }

    @Test
    public void testReachNotInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        expected.expect(IllegalStateException.class);
        train.eventFactory().reach(10, edge, 10);
    }

    @Test
    public void testLeaveNotInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        expected.expect(IllegalStateException.class);
        train.eventFactory().leave(10, edge, 10);
    }

    @Test
    public void testTerminateNotInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        expected.expect(IllegalStateException.class);
        train.eventFactory().terminate(10, 10);
    }

    @Test
    public void testSpeedAlreadyTerminated() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);
        train.eventFactory().terminate(5, 0);
        train.eventFactory().speed(10, 10, 10);
        assertFalse(train.getEvents().get(train.getEvents().size() - 1).getWarnings().isEmpty());
    }

    @Test
    public void testReachAlreadyTerminated() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);
        train.eventFactory().terminate(5, 0);
        train.eventFactory().reach(10, edge, 10);
        assertFalse(train.getEvents().get(train.getEvents().size() - 1).getWarnings().isEmpty());
    }

    @Test
    public void testLeaveAlreadyTerminated() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);
        train.eventFactory().terminate(5, 0);
        train.eventFactory().leave(10, edge, 10);
        assertFalse(train.getEvents().get(train.getEvents().size() - 1).getWarnings().isEmpty());
    }

    @Test
    public void testTerminateAlreadyTerminated() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);
        train.eventFactory().terminate(5, 0);
        train.eventFactory().terminate(10, 10);
        assertFalse(train.getEvents().get(train.getEvents().size() - 1).getWarnings().isEmpty());
    }

    @Test
    public void testEventBeforeLast() {
        Train train = Train.in(context).create("t", "train", 10);
        Edge edge = createEdges(50)[0];
        train.eventFactory().init(0, edge);
        train.eventFactory().speed(10, 10, 20);
        Event beforeEvent = train.getEvents().get(train.getEvents().size() - 1);
        train.eventFactory().speed(5, 10, 10);
        Event newEvent = train.getEvents().get(train.getEvents().size() - 1);
        assertTrue(beforeEvent != newEvent);

        assertTrue(beforeEvent.getWarnings().isEmpty());
        assertEquals(10, beforeEvent.getTime());

        assertFalse(newEvent.getWarnings().isEmpty());
        assertEquals(10, newEvent.getTime());
        assertEquals(beforeEvent.getTime(), newEvent.getTime());
    }
}
