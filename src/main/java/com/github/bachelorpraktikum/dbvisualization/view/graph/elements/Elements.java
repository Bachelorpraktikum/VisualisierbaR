package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShape;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.transform.Transform;

public final class Elements {
    private Elements() {
    }

    public static Collection<GraphShape<Element>> create(Node node, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        List<GraphShape<Element>> shapes = new ArrayList<>(node.getElements().size());
        // TODO temporary hack to layout multiple elements
        int count = 0;
        for (Element element : node.getElements()) {
            switch (element.getType()) {
                case MagnetImpl:
                    shapes.add(new MagnetElement(element, parentTransform, adapter));
                    break;
                case SichtbarkeitsPunktImpl:
                case VorSignalImpl:
                case GefahrenPunktImpl:
                    shapes.add(new DefaultOffsetElement(element, parentTransform, adapter, count++));
                    break;
                case WeichenPunktImpl:
                    if (element.equals(element.getSwitch().get().getMainElement()))
                        shapes.add(new WeichenpunktElement(element, parentTransform, adapter));
                    break;
                case SwWechselImpl:
                    shapes.add(new StellwerkWechselElement(element, parentTransform, adapter));
                    break;
                case HauptSignalImpl:
                    shapes.add(new HauptsignalElement(element, parentTransform, adapter, count++));
                    break;
                case GeschwindigkeitsAnzeigerImpl:
                    // included in Hauptsignal
                    break;
                default:
                    shapes.add(new DummyElement(element, parentTransform, adapter, count++));
            }
        }
        return shapes;
    }
}
