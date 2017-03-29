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

    private Node getNode(Context context) {
        nodeCounter++;
        return Node.in(context).create("node" + nodeCounter, getCoordinate());
    }

    private Node getNode() {
        return getNode(context);
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

    @Test
    public void testCreateNodeDifferentContext() {
        Node node1 = getNode();
        Node node2 = getNode();

        Context otherContext = new Context();
        expected.expect(IllegalArgumentException.class);
        Edge.in(otherContext).create("e", 1, node1, node2);
    }

    @Test
    public void testGetCommonNode() {
        Edge edge = createRandom(context);
        Node otherNode = getNode();
        Edge other1 = Edge.in(context).create("other1", 10, edge.getNode1(), otherNode);
        Edge other2 = Edge.in(context).create("other2", 10, otherNode, edge.getNode1());

        assertEquals(edge.getNode1(), edge.getCommonNode(other1));
        assertEquals(edge.getNode1(), edge.getCommonNode(other2));

        assertEquals(edge.getNode1(), other1.getCommonNode(edge));
        assertEquals(edge.getNode1(), other2.getCommonNode(edge));
    }

    @Test
    public void testGetCommonNodeInvalid() {
        Edge edge = createRandom(context);
        Edge other = createRandom(context);

        expected.expect(IllegalArgumentException.class);
        edge.getCommonNode(other);
    }

    @Test
    public void testGetCommonNodeNull() {
        Edge edge = createRandom(context);

        expected.expect(NullPointerException.class);
        edge.getCommonNode(null);
    }

    @Test
    public void testGetOtherNode() {
        Edge edge = createRandom(context);
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();

        assertEquals(node2, edge.getOtherNode(node1));
        assertEquals(node1, edge.getOtherNode(node2));
    }

    @Test
    public void testGetOtherNodeInvalid() {
        Edge edge = createRandom(context);
        Node invalid = getNode();

        expected.expect(IllegalArgumentException.class);
        edge.getOtherNode(invalid);
    }

    @Test
    public void testGetOtherNodeNull() {
        Edge edge = createRandom(context);

        expected.expect(NullPointerException.class);
        edge.getOtherNode(null);
    }

    @Override
    protected EdgeFactory getFactory(Context context) {
        return Edge.in(context);
    }

    @Override
    protected Edge createRandom(Context context) {
        return getFactory(context)
            .create("edge" + nodeCounter++, random.nextInt(), getNode(context), getNode(context));
    }

    @Override
    protected Edge createSame(Context context, Edge edge) {
        return getFactory(context).create(
            edge.getName(),
            edge.getLength(),
            edge.getNode1(),
            edge.getNode2()
        );
    }

    @Override
    public void testCreateDifferentArg(Context context, Edge edge, int argIndex) {
        switch (argIndex) {
            case 1:
                getFactory(context).create(
                    edge.getName(),
                    edge.getLength() + 1,
                    edge.getNode1(),
                    edge.getNode2()
                );
                break;
            case 2:
                getFactory(context).create(
                    edge.getName(),
                    edge.getLength(),
                    getNode(context),
                    edge.getNode2()
                );
                break;
            case 3:
                getFactory(context).create(
                    edge.getName(),
                    edge.getLength(),
                    edge.getNode1(),
                    getNode(context)
                );
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
