package com.github.bachelorpraktikum.dbvisualization.model;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    /**
     * <p>Decides whether a Shape created by this Shapeable should be visible.</p>
     *
     * <p>If the current visibleState is {@link VisibleState#AUTO}, the visibility is determined by
     * the given bounds of the shape and the minimum size returned by {@link #minSize()}. If the
     * bounds are null, AUTO is equal to {@link VisibleState#ENABLED}</p>
     *
     * <p><b>Implementation note:</b><br> The current implementation treats AUTO and ENABLED
     * equally and ignores {@link #minSize()}. </p>
     *
     * @param bounds the bounds on screen of the shape, or null
     * @return whether the shape should be visible
     */
    default boolean isVisible(@Nullable Bounds bounds) {
        VisibleState state = visibleStateProperty().getValue();
        if (bounds == null) {
            return state != VisibleState.DISABLED;
        }
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

    /**
     * Creates Shapes from the FXML files at the specified locations and applies {@link
     * Shape#union(Shape, Shape)} on them.
     *
     * @param urls URLs to FXML resources
     * @return a shape
     * @throws IllegalArgumentException if no URLs or invalid URLs have been given
     */
    @Nonnull
    static Shape createShape(URL... urls) {
        return createShape(Arrays.asList(urls));
    }

    /**
     * Creates Shapes from the FXML files at the specified locations and applies {@link
     * Shape#union(Shape, Shape)} on them.
     *
     * @param urls URLs to FXML resources
     * @return a shape
     * @throws NullPointerException if urls is null
     * @throws IllegalArgumentException if no URLs or invalid URLs have been given
     */
    @Nonnull
    static Shape createShape(Collection<URL> urls) {
        if (Objects.requireNonNull(urls).isEmpty()) {
            throw new IllegalArgumentException();
        }
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
            Logger.getLogger(Shapeable.class.getName())
                .severe("Error trying to load Shape from FXML file:"
                    + System.lineSeparator()
                    + e);
            throw new IllegalArgumentException(e);
        }
    }
}
