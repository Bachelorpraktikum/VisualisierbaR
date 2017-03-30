package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.GraphObject;
import com.github.bachelorpraktikum.dbvisualization.view.Highlightable;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Holds the shape representing one or multiple GraphObjects of the same type.
 *
 * @param <Represented> the type of the represented GraphObjects
 */
@ParametersAreNonnullByDefault
public interface GraphShape<Represented extends GraphObject<?>> extends Highlightable {

    /**
     * <p>Gets the full Node for the represented objects, including the (probably invisible)
     * highlighting {@link Node}.</p>
     *
     * <p>The Node returned by this method should be the one added to the Graph.</p>
     *
     * @return the full node for this GraphShape
     */
    @Nonnull
    Node getFullNode();

    /**
     * Gets the combined Node for the represented objects, without highlighting.
     *
     * @return a Node
     */
    @Nonnull
    Node getShape();

    /**
     * <p>Gets the Shape representing a single represented object.</p>
     *
     * <p>For example, the returned Shape can be colored to display the state of an Element.</p>
     *
     * @param represented the represented object
     * @return a Shape
     */
    @Nonnull
    Shape getShape(Represented represented);

    /**
     * Gets a list of all represented objects.
     *
     * @return a list of represented objects
     */
    @Nonnull
    List<Represented> getRepresentedObjects();
}
