package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShape;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class Elements {
    private Elements() {
    }

    public static Collection<GraphShape<Element>> create(Node node, CoordinatesAdapter adapter) {
        List<GraphShape<Element>> shapes = new ArrayList<>(node.getElements().size());
        // TODO temporary hack to layout multiple elements
        int count = 0;

        List<Element> compositeElements = new LinkedList<>();

        for (Element element : node.getElements()) {
            switch (element.getType()) {
                case MagnetImpl:
                    shapes.add(new MagnetElement(element, node, adapter));
                    break;
                case SichtbarkeitsPunktImpl:
                case GefahrenPunktImpl:
                    shapes.add(new DefaultOffsetElement(element, node, adapter, count++));
                    break;
                case WeichenPunktImpl:
                    if (element.equals(element.getSwitch().get().getMainElement()))
                        shapes.add(new WeichenpunktElement(element, node, adapter));
                    break;
                case SwWechselImpl:
                    shapes.add(new StellwerkWechselElement(element, node, adapter));
                    break;
                case VorSignalImpl:
                case HauptSignalImpl:
                case GeschwindigkeitsAnzeigerImpl:
                    compositeElements.add(element);
                    break;
                default:
                    shapes.add(new DummyElement(element, node, adapter, count++));
            }
        }
        if (!compositeElements.isEmpty()) {
            shapes.add(new CompositeElement(compositeElements, node, adapter));
        }
        return shapes;
    }
}
