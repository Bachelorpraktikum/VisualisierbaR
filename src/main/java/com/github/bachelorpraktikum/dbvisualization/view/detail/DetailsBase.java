package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.GraphObject;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableIntegerValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class DetailsBase<O extends GraphObject<?>> {

    private final O element;
    private final ObservableIntegerValue time;
    private final List<Object> bindings;

    DetailsBase(O element, ObservableIntegerValue time, String fxmlLocation) {
        this.element = Objects.requireNonNull(element);
        this.time = Objects.requireNonNull(time);
        this.bindings = new LinkedList<>();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlLocation));
        loader.setResources(ResourceBundle.getBundle("bundles.localization"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Stores the given binding so it won't be garbage collected.
     *
     * @param binding a binding, or any object
     */
    void addBinding(Object binding) {
        bindings.add(Objects.requireNonNull(binding));
    }

    @Nonnull
    O getObject() {
        return element;
    }

    @Nonnull
    String getName() {
        return getObject().getReadableName();
    }

    /**
     * Gets the GraphObject-specific details for the represented object.
     *
     * @return an arbitrary node
     */
    @Nonnull
    abstract Node getDetails();

    @Nonnull
    String getCoordinatesString(@Nullable Point2D coord) {
        if (coord == null) {
            return ResourceBundle.getBundle("bundles.localization").getString("unavailable");
        } else {
            return String.format("x: %f | y: %f", coord.getX(), coord.getY());
        }
    }

    /**
     * <p>Gets an ObservableIntegerValue holding the current simulation time.</p>
     *
     * <p>Warning: Only weak listeners should be registered on this Observable</p>
     *
     * @return the observable time
     */
    @Nonnull
    ObservableIntegerValue timeProperty() {
        return time;
    }

    /**
     * Gets the icon shape for the represented object.
     *
     * @return the icon shape
     */
    @Nonnull
    Shape getShape() {
        return getObject().createIconShape();
    }

    /**
     * Creates the appropriate DetailsBase for the given GraphObject.
     *
     * @param object the GraphObject
     * @param timeProperty a property holding the simulation time
     * @param centerPane the center pane of the main window
     * @return a DetailsBase instance
     * @throws IllegalArgumentException if there is no appropriate implementation for the type of
     * the given object
     */
    @Nonnull
    public static DetailsBase<?> create(GraphObject<?> object, ObservableIntegerValue timeProperty,
        Pane centerPane) {
        if (object instanceof Train) {
            Train train = (Train) object;
            return new TrainDetails(train, timeProperty, centerPane);
        } else if (object instanceof Element) {
            Element element = (Element) object;
            return new ElementDetails(element, timeProperty);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
