package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.net.URL;

import javax.annotation.Nonnull;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

final class HauptsignalElement extends PathElement {
    HauptsignalElement(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(element, parentTransform, adapter);
    }

    @Override
    protected URL[] getImageUrls() {
        if(getRepresented().getNode().getElements().stream()
                .map(Element::getType)
                .anyMatch(type -> type == Element.Type.GeschwindigkeitsAnzeigerImpl)) {
            return Element.Type.GeschwindigkeitsAnzeigerImpl.getImageUrls();
        }else {
            return super.getImageUrls();
        }
    }
}
