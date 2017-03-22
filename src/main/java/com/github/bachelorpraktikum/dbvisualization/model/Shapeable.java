package com.github.bachelorpraktikum.dbvisualization.model;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import javafx.beans.property.Property;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents anything that can be represented as a shape.
 *
 * @param <S> the type of shape {@link #createShape()} returns
 */
@ParametersAreNonnullByDefault
public interface Shapeable<S extends Shape> {

    enum VisibleState {
        ENABLED, DISABLED, AUTO
    }

    /**
     * Gets the name for this Shapeable.
     *
     * @return the name
     */
    @Nonnull
    String getName();

    /**
     * Creates a shape representing this Shapeable in the graph.
     *
     * @return a shape
     */
    @Nonnull
    S createShape();

    /**
     * <p>Creates a shape representing this Shapeable outside of the graph.
     * This might be used in the legend, or the details sidebar.</p>
     *
     * <p>The default implementation calls {@link #createShape()}</p>
     *
     * @return a shape
     */
    @Nonnull
    default Shape createIconShape() {
        return createShape();
    }

    /**
     * Holds the current visibility state of this Shapeable.
     *
     * @return the state property
     */
    @Nonnull
    Property<VisibleState> visibleStateProperty();

    /**
     * The minimal size on screen this Shapeable needs to cover to be visible if state is AUTO.
     *
     * @return the minimal area
     */
    default double minSize() {
        return 20;
    }

    default boolean isVisible(Bounds bounds) {
        if (bounds == null) {
            return true;
        }
        VisibleState state = visibleStateProperty().getValue();
        switch (state) {
            case ENABLED:
                return true;
            case DISABLED:
                return false;
            case AUTO:
            default:
                return true; // TODO calculate
        }
    }

    @Nonnull
    static Shape createShape(URL... urls) {
        return createShape(Arrays.asList(urls));
    }

    @Nonnull
    static Shape createShape(Collection<URL> urls) {
        try {
            Shape shape = null;

            for (URL url : urls) {
                FXMLLoader loader = new FXMLLoader(url);
                if (shape == null) {
                    shape = loader.load();
                } else {
                    shape = Shape.union(shape, loader.load());
                }
            }

            return shape;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
