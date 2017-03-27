package com.github.bachelorpraktikum.dbvisualization.model.train;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.train.InterpolatableState.Builder;
import org.junit.Before;
import org.junit.Test;

public class InterpolatableStateBuilderTest {

    private Context context;

    @Before
    public void init() {
        this.context = new Context();
    }

    private Train createTrain() {
        return Train.in(context).create("train", "t", 10);
    }

    @Test(expected = NullPointerException.class)
    public void testNullTrain() {
        createBuilder(null);
    }

    private Builder createBuilder(Train train) {
        return new Builder(train);
    }

    private Builder createBuilder() {
        return createBuilder(createTrain());
    }

    private TrainPosition initPosition(Train train) {
        Node node1 = Node.in(context).create("node1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("node2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("edge", 50, node1, node2);

        return TrainPosition.init(train, edge, node1, node2);
    }

    @Test(expected = IllegalStateException.class)
    public void testNotConfigured() {
        createBuilder().build();
    }

    @Test
    public void testFullConfig() {
        Train train = createTrain();

        InterpolatableState state = createBuilder(train)
            .time(Context.INIT_STATE_TIME)
            .speed(10)
            .terminated(true)
            .initialized(false)
            .index(10)
            .distance(10)
            .position(initPosition(train))
            .build();

        assertNotNull(state);
    }

    @Test
    public void testToString() {
        assertNotNull(createBuilder().toString());
        assertFalse(createBuilder().toString().trim().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTime() {
        createBuilder().time(-2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeSpeed() {
        createBuilder().speed(-0.1);
    }

    @Test
    public void testTerminated() {
        // assure no exceptions are thrown
        createBuilder().terminated(true).terminated(false);
    }

    @Test
    public void testInitialized() {
        // assure no exceptions are thrown
        createBuilder().initialized(true).initialized(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeIndex() {
        createBuilder().index(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDistance() {
        createBuilder().distance(-1);
    }

    @Test
    public void testPosition() {
        Train train = createTrain();
        // assure no exceptions are thrown
        createBuilder(train).position(null).position(initPosition(train));
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingTime() {
        Train train = createTrain();
        createBuilder(train)
            .speed(10)
            .terminated(true)
            .initialized(false)
            .index(10)
            .distance(10)
            .position(initPosition(train))
            .build();
    }


    @Test(expected = IllegalStateException.class)
    public void testMissingSpeed() {
        Train train = createTrain();
        createBuilder(train)
            .time(Context.INIT_STATE_TIME)
            .terminated(true)
            .initialized(false)
            .index(10)
            .distance(10)
            .position(initPosition(train))
            .build();
    }

    @Test
    public void testMissingTerminated() {
        Train train = createTrain();
        assertNotNull(createBuilder(train)
            .time(Context.INIT_STATE_TIME)
            .speed(10)
            .initialized(false)
            .index(10)
            .distance(10)
            .position(initPosition(train))
            .build());
    }

    @Test
    public void testMissingInitialized() {
        Train train = createTrain();
        assertNotNull(createBuilder(train)
            .time(Context.INIT_STATE_TIME)
            .speed(10)
            .terminated(true)
            .index(10)
            .distance(10)
            .position(initPosition(train))
            .build());
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingIndex() {
        Train train = createTrain();
        createBuilder(train)
            .time(Context.INIT_STATE_TIME)
            .speed(10)
            .terminated(true)
            .initialized(false)
            .distance(10)
            .position(initPosition(train))
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingDistance() {
        Train train = createTrain();
        createBuilder(train)
            .time(Context.INIT_STATE_TIME)
            .speed(10)
            .terminated(true)
            .initialized(false)
            .index(10)
            .position(initPosition(train))
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingPositionInitialized() {
        Train train = createTrain();
        createBuilder(train)
            .time(Context.INIT_STATE_TIME)
            .speed(10)
            .terminated(true)
            .initialized(true)
            .index(10)
            .distance(10)
            .build();
    }

    @Test
    public void testMissingPositionUninitialized() {
        Train train = createTrain();
        assertNotNull(createBuilder(train)
            .time(Context.INIT_STATE_TIME)
            .speed(10)
            .terminated(true)
            .initialized(false)
            .index(10)
            .distance(10)
            .build());
    }
}
