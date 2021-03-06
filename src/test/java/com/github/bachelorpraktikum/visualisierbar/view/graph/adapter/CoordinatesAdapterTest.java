package com.github.bachelorpraktikum.visualisierbar.view.graph.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.bachelorpraktikum.visualisierbar.model.Context;
import com.github.bachelorpraktikum.visualisierbar.model.Coordinates;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import java.util.Random;
import javafx.geometry.Point2D;
import org.junit.Before;
import org.junit.Test;

public abstract class CoordinatesAdapterTest {

    private CoordinatesAdapter adapter;
    private Context context;

    protected final CoordinatesAdapter getAdapter() {
        return adapter;
    }

    protected final Context getContext() {
        return context;
    }

    protected abstract CoordinatesAdapter createAdapter();

    @Before
    public final void init() {
        this.context = new Context();
        createNodes();
        this.adapter = createAdapter();
    }

    private void createNodes() {
        Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            int xCoordinate = random.nextInt(Integer.MAX_VALUE);
            int yCoordinate = random.nextInt(Integer.MAX_VALUE);
            Coordinates coordinates = new Coordinates(xCoordinate, yCoordinate);
            Node.in(getContext()).create(String.valueOf(i), coordinates);
        }
        // TODO connect nodes with edges
    }

    @Test
    public void testApplyRandomCoordinates() {
        for (Node node : Node.in(getContext()).getAll()) {
            Point2D point = getAdapter().apply(node);
            assertNotNull(point);
            assertFalse(point.getX() < 0.0);
            assertFalse(point.getY() < 0.0);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testApplyNull() {
        getAdapter().apply(null);
    }

    @Test
    public void testGetCalibrationBase() {
        double base = getAdapter().getCalibrationBase();
        assertTrue(Double.isFinite(base));
        assertTrue(base > 0.0);
    }
}
