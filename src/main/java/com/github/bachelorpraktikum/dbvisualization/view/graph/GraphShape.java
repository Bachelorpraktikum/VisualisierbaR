package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.view.Highlightable;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface GraphShape<Represented> extends Highlightable {

    @Nonnull
    Node getFullNode();

    @Nonnull
    Node getShape();

    @Nonnull
    Shape getShape(Represented represented);

    @Nonnull
    List<Represented> getRepresentedObjects();
}
