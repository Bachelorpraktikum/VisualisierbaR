package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.bachelorpraktikum.dbvisualization.model.Shapeable.VisibleState;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
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

    @Test
    public void testIsVisibleEnabled() {
        Shapeable<?> shapeable = new TestShapeable(VisibleState.ENABLED);
        assertTrue(shapeable.isVisible(shapeable.createShape().getBoundsInParent()));
    }

    @Test
    public void testIsVisibleAuto() {
        // TODO if the AUTO implementation is done, this test should be updated
        Shapeable<?> shapeable = new TestShapeable(VisibleState.AUTO);
        assertTrue(shapeable.isVisible(shapeable.createShape().getBoundsInParent()));
    }

    @Test
    public void testIsVisibleDisabled() {
        Shapeable<?> shapeable = new TestShapeable(VisibleState.DISABLED);
        assertFalse(shapeable.isVisible(shapeable.createShape().getBoundsInParent()));
    }

    @Test
    public void testIsVisibleNullBoundsEnabled() {
        Shapeable<?> shapeable = new TestShapeable(VisibleState.ENABLED);
        assertTrue(shapeable.isVisible(null));
    }

    @Test
    public void testIsVisibleNullBoundsAuto() {
        Shapeable<?> shapeable = new TestShapeable(VisibleState.AUTO);
        assertTrue(shapeable.isVisible(null));
    }

    @Test
    public void testIsVisibleNullBoundsDisabled() {
        Shapeable<?> shapeable = new TestShapeable(VisibleState.DISABLED);
        assertFalse(shapeable.isVisible(null));
    }

    private static class TestShapeable implements Shapeable<Shape> {

        private final Property<VisibleState> visibleState;

        TestShapeable(@Nonnull VisibleState state) {
            visibleState = new SimpleObjectProperty<>(Objects.requireNonNull(state));
        }

        @Nonnull
        @Override
        public String getName() {
            return "test";
        }

        @Nonnull
        @Override
        public Shape createShape() {
            return new Rectangle(1, 1);
        }

        @Nonnull
        @Override
        public Property<VisibleState> visibleStateProperty() {
            return visibleState;
        }
    }

}
