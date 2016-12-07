package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.SimpleCoordinatesAdapter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Random;

public class GraphTest {
    private Context context;
    private Graph graph;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private Node createNode(String id, Random rand) {
        int xCoordinate = rand.nextInt(Integer.MAX_VALUE);
        int yCoordinate = rand.nextInt(Integer.MAX_VALUE);
        Coordinates coordinates = new Coordinates(xCoordinate, yCoordinate);
        return Node.in(context).create(id, coordinates);
    }

    private void createEdges() {
        Random random = new Random();

        for (int i = 0; i < 10000; i++) {
            Node start = createNode(i + "start", random);
            Node end = createNode(i + "end", random);
            int length = random.nextInt(100) + 1;
            Edge edge = Edge.in(context).create("edge" + i, length, start, end);
        }
    }

    @Before
    public void init() {
        this.context = new Context();
        createEdges();
        this.graph = new Graph(context, new SimpleCoordinatesAdapter());
    }

    @Test
    public void testCreateNullContext() {
        expected.expect(NullPointerException.class);
        Graph graph = new Graph(null, new SimpleCoordinatesAdapter());
    }

    @Test
    public void testCreateNullAdapter() {
        expected.expect(NullPointerException.class);
        Graph graph = new Graph(context, null);
    }

    @Ignore("NEEDS UPDATE")
    @Test
    public void testCreateShapeNotNull() {
        // Shape shape = graph.createShape();
        // assertNotNull(shape);
    }
}
