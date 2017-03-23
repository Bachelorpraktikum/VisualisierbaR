package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.bachelorpraktikum.dbvisualization.model.Edge.EdgeFactory;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EdgeTest extends FactoryTest<Edge> {

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

    @Override
    protected EdgeFactory getFactory() {
        return Edge.in(context);
    }

    @Override
    protected Edge createRandom() {
        return getFactory().create("edge" + nodeCounter++, random.nextInt(), getNode(), getNode());
    }

    @Override
    protected Edge createSame(Edge edge) {
        return getFactory().create(
            edge.getName(),
            edge.getLength(),
            edge.getNode1(),
            edge.getNode2()
        );
    }

    @Override
    public void testCreateDifferentArg(Edge edge, int arg) {
        switch (arg) {
            case 1:
                getFactory().create(
                    edge.getName(),
                    edge.getLength() + 1,
                    edge.getNode1(),
                    edge.getNode2()
                );
                break;
            case 2:
                getFactory().create(
                    edge.getName(),
                    edge.getLength(),
                    getNode(),
                    edge.getNode2()
                );
                break;
            case 3:
                getFactory().create(
                    edge.getName(),
                    edge.getLength(),
                    edge.getNode1(),
                    getNode()
                );
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
