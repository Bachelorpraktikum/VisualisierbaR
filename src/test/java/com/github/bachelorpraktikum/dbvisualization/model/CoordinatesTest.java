package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by chabare on 19.11.16
 */
public class CoordinatesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNegativeX() {
        exception.expect(IllegalArgumentException.class);
        new Coordinates(-1, 0);
    }

    @Test
    public void testNegativeY() {
        exception.expect(IllegalArgumentException.class);
        new Coordinates(0, -1);
    }

    @Test
    public void testEquals() {
        int x1 = 0;
        int x2 = 1;
        int y1 = 0;
        int y2 = 1;

        Coordinates coordinate11 = new Coordinates(x1, y1);
        Coordinates coordinate11_2 = new Coordinates(x1, y1);
        Coordinates coordinate11_dup = coordinate11;
        Coordinates coordinate12 = new Coordinates(x1, y2);
        Coordinates coordinate21 = new Coordinates(x2, y1);
        Coordinates coordinate22 = new Coordinates(x2, y2);

        assertTrue(coordinate11.equals(coordinate11));
        assertTrue(coordinate12.equals(coordinate12));

        assertTrue(coordinate11.equals(coordinate11_2));
        assertTrue(coordinate11.equals(coordinate11_dup));

        assertFalse(coordinate11.equals(coordinate12));
        assertFalse(coordinate11.equals(coordinate21));
        assertFalse(coordinate11.equals(coordinate22));

        assertFalse(coordinate12.equals(coordinate11));
        assertFalse(coordinate12.equals(coordinate21));
        assertFalse(coordinate21.equals(coordinate22));

        assertFalse(coordinate11.equals(null));
    }

    @Test
    public void testToString() {
        Coordinates coordinate = new Coordinates(1, 2);

        assertEquals(coordinate.toString(), "Coordinates{x=1, y=2}");
    }

    @Test
    public void testHashCode() {
        Coordinates coordinate;
        Coordinates coordinate2;

        Random sec = new Random();
        for (int counter = 0; counter < 20000; counter++) {
            int x = sec.nextInt(Integer.MAX_VALUE);
            int y = sec.nextInt(Integer.MAX_VALUE);

            if (x != y) {
                coordinate = new Coordinates(x, y);
                coordinate2 = new Coordinates(y, x);
                assertNotEquals(coordinate.hashCode(), coordinate2.hashCode());
            }
        }
    }

    @Test
    public void testGetX() {
        Coordinates coordinate = new Coordinates(1, 1);

        assertEquals(coordinate.getX(), 1);
    }

    @Test
    public void testGetY() {
        Coordinates coordinate = new Coordinates(1, 1);

        assertEquals(coordinate.getY(), 1);
    }
}
