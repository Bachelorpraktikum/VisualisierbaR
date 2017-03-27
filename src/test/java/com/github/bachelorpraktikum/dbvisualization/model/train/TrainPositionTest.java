package com.github.bachelorpraktikum.dbvisualization.model.train;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.SimpleCoordinatesAdapter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Point2D;
import org.junit.Before;
import org.junit.Test;

public class TrainPositionTest {

    private Context context;
    /**
     * length: 20
     */
    private Train train;

    /*
              <n0>
                \
                 \
                  e0
                   \
                    \
        <n1> --e1-- <n2> --e2-- <n3> --e3-- <n4> --e4-- <n5>
    */
    private Node node0;
    /**
     * length: 40
     * note: goes from node2 to node0 ("reversed")
     */
    private Edge edge0;
    private Node node1;
    /**
     * length: 40
     */
    private Edge edge1;
    private Node node2;
    /**
     * length: 10
     */
    private Edge edge2;
    private Node node3;
    /**
     * length: 40
     */
    private Edge edge3;
    private Node node4;
    /**
     * length: 40
     */
    private Edge edge4;
    private Node node5;


    @Before
    public void init() {
        context = new Context();
        train = Train.in(context).create("train", "t", 20);

        node0 = Node.in(context).create("node0", new Coordinates(0, 0));
        node1 = Node.in(context).create("node1", new Coordinates(0, 1));
        node2 = Node.in(context).create("node2", new Coordinates(1, 1));
        node3 = Node.in(context).create("node3", new Coordinates(2, 1));
        node4 = Node.in(context).create("node4", new Coordinates(3, 1));
        node5 = Node.in(context).create("node5", new Coordinates(4, 1));

        edge0 = Edge.in(context).create("edge0", 40, node2, node0);
        edge1 = Edge.in(context).create("edge1", 40, node1, node2);
        edge2 = Edge.in(context).create("edge2", 10, node2, node3);
        edge3 = Edge.in(context).create("edge3", 40, node3, node4);
        edge4 = Edge.in(context).create("edge4", 40, node4, node5);
    }

    @Test
    public void testInit() {
        TrainPosition init = TrainPosition.init(train, edge1, node1, node2);
        check(init,
            edge1,
            train.getLength(),
            node1.getCoordinates().toPoint2D().add(0.5, 0),
            edge1,
            edge1.getLength(),
            node1.getCoordinates().toPoint2D(),
            Collections.singletonList(edge1)
        );
    }

    @Test(expected = NullPointerException.class)
    public void testInitNullTrain() {
        TrainPosition.init(null, edge0, node0, node2);
    }

    @Test(expected = NullPointerException.class)
    public void testInitNullEdge() {
        TrainPosition.init(train, null, node1, node2);
    }

    @Test(expected = NullPointerException.class)
    public void testInitNullNode1() {
        TrainPosition.init(train, edge1, null, node2);
    }

    @Test(expected = NullPointerException.class)
    public void testInitNullNode2() {
        TrainPosition.init(train, edge1, node1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitInvalidNode1() {
        TrainPosition.init(train, edge1, node0, node2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitInvalidNode2() {
        TrainPosition.init(train, edge1, node1, node3);
    }

    @Test
    public void testMove() {
        TrainPosition init = TrainPosition.init(train, edge1, node1, node2);
        TrainPosition move = init.move(20);

        check(move,
            edge1,
            edge1.getLength(),
            node2.getCoordinates().toPoint2D(),
            edge1,
            train.getLength(),
            node2.getCoordinates().toPoint2D().subtract(0.5, 0),
            Collections.singletonList(edge1)
        );
    }

    @Test
    public void testReachFront() {
        TrainPosition init = TrainPosition.init(train, edge1, node1, node2);
        TrainPosition reached = init.reachFront(edge2);

        check(reached,
            edge2,
            0,
            node2.getCoordinates().toPoint2D(),
            edge1,
            train.getLength(),
            node2.getCoordinates().toPoint2D().subtract(0.5, 0),
            Arrays.asList(edge2, edge1)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReachFrontReversedInit() {
        TrainPosition init = TrainPosition.init(train, edge1, node2, node1);
        init.reachFront(edge2);
    }

    @Test
    public void testLeaveBack() {
        TrainPosition init = TrainPosition.init(train, edge3, node3, node4);
        TrainPosition reach = init.reachFront(edge4);
        TrainPosition move = reach.move(10);
        TrainPosition leave = move.leaveBack(edge4, 10);

        check(leave,
            edge4,
            train.getLength(),
            node4.getCoordinates().toPoint2D().add(0.5, 0),
            edge4,
            edge4.getLength(),
            node4.getCoordinates().toPoint2D(),
            Collections.singletonList(edge4)
        );
    }

    @Test
    public void testInterpolationMoveNoReach() {
        TrainPosition init = TrainPosition.init(train, edge3, node3, node4);
        TrainPosition moved = init.interpolationMove(10, edge4);

        check(moved,
            edge3,
            train.getLength() + 10,
            node4.getCoordinates().toPoint2D().subtract(0.25, 0),
            edge3,
            train.getLength() + 10,
            node3.getCoordinates().toPoint2D().add(0.25, 0),
            Collections.singletonList(edge3)
        );
    }

    @Test
    public void testInterpolationMoveReach() {
        TrainPosition init = TrainPosition.init(train, edge3, node3, node4);
        TrainPosition moved = init.interpolationMove(30, edge4);

        check(moved,
            edge4,
            10,
            node4.getCoordinates().toPoint2D().add(0.25, 0),
            edge3,
            10,
            node4.getCoordinates().toPoint2D().subtract(0.25, 0),
            Arrays.asList(edge4, edge3)
        );
    }

    @Test
    public void testInterpolationMoveLeave() {
        TrainPosition init = TrainPosition.init(train, edge3, node3, node4);
        TrainPosition moved = init.reachFront(edge4).move(10).interpolationMove(20, null);

        check(moved,
            edge4,
            30,
            node5.getCoordinates().toPoint2D().subtract(0.25, 0),
            edge4,
            30,
            node4.getCoordinates().toPoint2D().add(0.25, 0),
            Collections.singletonList(edge4)
        );
    }

    @Test
    public void testEquals() {
        TrainPosition init = TrainPosition.init(train, edge1, node1, node2);
        TrainPosition init2 = TrainPosition.init(train, edge1, node1, node2);
        Train otherTrain = Train.in(context).create("train2", "t2", 10);
        TrainPosition initOtherTrain = TrainPosition.init(otherTrain, edge1, node1, node2);
        TrainPosition reach = init.reachFront(edge2);
        TrainPosition move = reach.move(5);

        // null
        assertFalse(init.equals(null));

        // reflexive
        assertEquals(init, init);

        // symmetric
        assertEquals(init, init2);
        assertEquals(init2, init);

        assertNotEquals(init, initOtherTrain);
        assertNotEquals(initOtherTrain, init);

        assertNotEquals(init, reach);
        assertNotEquals(reach, init);

        // transitive
        assertNotEquals(init, reach);
        assertNotEquals(reach, move);
        assertNotEquals(init, move);
    }

    @Test
    public void testHashCode() {
        TrainPosition init1 = TrainPosition.init(train, edge1, node1, node2);
        TrainPosition init2 = TrainPosition.init(train, edge1, node1, node2);

        assertEquals(init1, init2);
        assertEquals(init1.hashCode(), init2.hashCode());
    }

    @Test
    public void testToString() {
        TrainPosition init = TrainPosition.init(train, edge1, node1, node2);
        assertNotNull(init.toString());
        assertFalse(init.toString().trim().isEmpty());
    }

    @Test
    public void testGetPositions() {
        TrainPosition init = TrainPosition.init(train, edge1, node1, node2);
        TrainPosition move = init.reachFront(edge2).reachFront(edge3).move(5);

        List<Point2D> expected = Arrays.asList(
            move.getFrontCoordinates(),
            node3.getCoordinates().toPoint2D(),
            node2.getCoordinates().toPoint2D(),
            move.getBackCoordinates()
        );
        CoordinatesAdapter adapter = new SimpleCoordinatesAdapter();

        assertEquals(expected, move.getPositions(adapter));
    }

    @Test
    public void testGetPositionsSingleEdge() {
        TrainPosition init = TrainPosition.init(train, edge1, node1, node2);
        TrainPosition move = init.move(10);

        List<Point2D> expected = Arrays.asList(
            move.getFrontCoordinates(),
            move.getBackCoordinates()
        );
        CoordinatesAdapter adapter = new SimpleCoordinatesAdapter();

        assertEquals(expected, move.getPositions(adapter));
    }

    private void check(Train.Position position,
        Edge frontEdge,
        int frontDistance,
        Point2D frontPos,
        Edge backEdge,
        int backDistance,
        Point2D backPos,
        List<Edge> edges) {
        assertNotNull(position);

        CoordinatesAdapter adapter = new SimpleCoordinatesAdapter();

        assertEquals(frontEdge, position.getFrontEdge());
        assertEquals(frontDistance, position.getFrontDistance());
        assertEquals(frontPos, position.getFrontCoordinates());
        assertEquals(frontPos, position.getFrontPosition(adapter));

        assertEquals(backEdge, position.getBackEdge());
        assertEquals(backDistance, position.getBackDistance());
        assertEquals(backPos, position.getBackCoordinates());
        assertEquals(backPos, position.getBackPosition(adapter));

        assertEquals(edges, position.getEdges());
    }
}
