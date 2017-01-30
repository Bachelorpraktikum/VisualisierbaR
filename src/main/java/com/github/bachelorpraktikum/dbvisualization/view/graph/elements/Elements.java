package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.Switch;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShape;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class Elements {
    private Elements() {
    }

    public static Collection<GraphShape<Element>> create(Node node, CoordinatesAdapter adapter) {
        List<GraphShape<Element>> shapes = new ArrayList<>(node.getElements().size());
        // TODO temporary hack to layout multiple elements
        int count = 0;

        List<Element> compositeElements = new LinkedList<>();
        Map<Switch, GraphShape<Element>> switches = new HashMap<>();

        for (Element element : node.getElements()) {
            switch (element.getType()) {
                case MagnetImpl:
                    shapes.add(new MagnetElement(element, node, adapter));
                    break;
                case SichtbarkeitsPunktImpl:
                case GefahrenPunktImpl:
                    shapes.add(new RotatedDefaultOffsetElement(element, node, adapter, count++));
                    break;
                case WeichenPunktImpl:
                    Switch aSwitch = element.getSwitch().get();
                    if(!switches.containsKey(aSwitch)) {
                        GraphShape<Element> switchShape = new WeichenpunktElement(aSwitch, node, adapter);
                        switches.put(aSwitch, switchShape);
                        shapes.add(switchShape);
                    }
                    break;
                case SwWechselImpl:
                    shapes.add(new StellwerkWechselElement(element, node, adapter));
                    break;
                case GeschwindigkeitsVoranzeiger:
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
            shapes.add(new CompositeElement(compositeElements, node, adapter, count));
        }
        return shapes;
    }
}
