package com.github.bachelorpraktikum.dbvisualization.view.graph;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.shape.Shape;

public interface GraphShape<Represented> {

    Node getShape();

    Shape getShape(Represented represented);

    List<Represented> getRepresentedObjects();
}
