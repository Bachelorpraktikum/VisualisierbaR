package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.net.URL;
import java.util.List;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.transform.Transform;

final class HauptsignalElement extends DefaultOffsetElement {
    HauptsignalElement(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter, int count) {
        super(element, parentTransform, adapter, count);
    }

    private boolean hasGeschwindigkeit() {
        return getRepresented().getNode().getElements().stream()
                .map(Element::getType)
                .anyMatch(type -> type == Element.Type.GeschwindigkeitsAnzeigerImpl);
    }

    @Override
    protected double getDesiredMax() {
        return super.getDesiredMax() + (hasGeschwindigkeit() ? 0.2 : 0.0);
    }

    @Override
    protected List<URL> getImageUrls() {
        if (hasGeschwindigkeit()) {
            return Element.Type.GeschwindigkeitsAnzeigerImpl.getImageUrls();
        } else {
            return super.getImageUrls();
        }
    }
}
