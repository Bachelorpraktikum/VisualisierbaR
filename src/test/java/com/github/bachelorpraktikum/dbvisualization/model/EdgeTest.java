package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by chabare on 19.11.16
 */
public class EdgeTest {

    private Context context;
    private Random random;
    private int nodeCounter;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        context = new Context();
        nodeCounter = -1;
        random = new Random();
    }

    private Coordinates getCoordinate() {
        return new Coordinates(random.nextInt(Integer.MAX_VALUE),
            random.nextInt(Integer.MAX_VALUE));
    }

    private Node getNode() {
        nodeCounter++;
        return Node.in(context).create("node" + nodeCounter, getCoordinate());
    }

    @Test
    public void testInstanceManager() {
        Edge edge = Edge.in(context).create("edge1", 30, getNode(), getNode());
        assertSame(edge, Edge.in(context).get(edge.getName()));
        assertSame(edge, Edge.in(context).create(edge.getName(),
            edge.getLength(), edge.getNode1(), edge.getNode2()));
        assertTrue(Edge.in(context).getAll().contains(edge));
    }

    @Test
    public void testInstanceManagerInvalidName() {
        expected.expect(IllegalArgumentException.class);
        Edge.in(context).get("t");
    }

    @Test
    public void testInstanceManagerExistsDifferentLength() {
        String name = "Edge";
        Edge edge = Edge.in(context).create(name, 10, getNode(), getNode());

        expected.expect(IllegalArgumentException.class);
        Edge.in(context).create(name, 20, edge.getNode1(), edge.getNode2());
    }

    @Test
    public void testInstanceManagerExistsDifferentNode1() {
        String name = "t";
        Edge edge = Edge.in(context).create(name, 10, getNode(), getNode());

        expected.expect(IllegalArgumentException.class);
        Edge.in(context).create(name, 10, getNode(), edge.getNode2());
    }

    @Test
    public void testInstanceManagerExistsDifferentNode2() {
        String name = "t";
        Edge edge = Edge.in(context).create(name, 10, getNode(), getNode());

        expected.expect(IllegalArgumentException.class);
        Edge.in(context).create(name, 10, edge.getNode1(), getNode());
    }

    @Test
    public void testName() {
        Edge edge = Edge.in(context).create("Edge", 50, getNode(), getNode());
        assertEquals("Edge", edge.getName());
    }

    @Test
    public void testLength() {
        Edge edge = Edge.in(context).create("Edge", 50, getNode(), getNode());
        assertEquals(50, edge.getLength());
    }

    @Test
    public void testNode1() {
        Node node = getNode();
        Edge edge = Edge.in(context).create("Edge", 50, node, getNode());

        assertSame(edge.getNode1(), node);
    }

    @Test
    public void testNode2() {
        Node node = getNode();
        Edge edge = Edge.in(context).create("Edge", 50, getNode(), node);

        assertSame(edge.getNode2(), node);
    }

    @Test
    public void testToString() {
        Node n1 = getNode();
        Node n2 = getNode();
        Edge edge = Edge.in(context).create("Edge", 60, n1, n2);

        assertEquals(edge.toString(), "Edge{name='Edge', length=60, node1=" + n1.toString() +
            ", node2=" + n2.toString() + "}");
    }

    @Test
    public void testNullLength() {
        Integer length = null;

        expected.expect(NullPointerException.class);
        Edge edge = Edge.in(context).create("Edge", length, getNode(), getNode());
    }

    @Test
    public void testNullNodes() {
        expected.expect(NullPointerException.class);
        Edge edge = Edge.in(context).create("Edge", 50, null, null);
    }

    @Test
    public void testNullName() {
        expected.expect(NullPointerException.class);
        Edge edge = Edge.in(context).create(null, 50, getNode(), getNode());
    }
}
