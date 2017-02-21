package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.view.Highlightable;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.shape.Shape;

public interface GraphShape<Represented> extends Highlightable {

    Node getFullNode();

    Node getShape();

    Shape getShape(Represented represented);

    List<Represented> getRepresentedObjects();
}
