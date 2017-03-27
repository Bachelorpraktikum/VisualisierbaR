package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;

public class ShapeableTest {

    @Test(expected = NullPointerException.class)
    public void testCreateShapeNull() {
        Shapeable.createShape((URL[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateShapeNullCollection() {
        Shapeable.createShape((Collection<URL>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShapeEmpty() {
        Shapeable.createShape(Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShapeInvalid() {
        Shapeable.createShape(Element.class.getResource("symbols/invalid.fxml"));
    }

    @Test
    public void testCreateShapeSingle() {
        URL url = Element.class.getResource("symbols/train.fxml");
        assertNotNull(Shapeable.createShape(url));
    }

    @Test
    public void testCreateShapeMultiple() {
        URL url1 = Element.class.getResource("symbols/SichtbarkeitspunktImpl.fxml");
        URL url2 = Element.class.getResource("symbols/SichtbarkeitspunktImpl2.fxml");
        assertNotNull(Shapeable.createShape(url1, url2));
    }
}
