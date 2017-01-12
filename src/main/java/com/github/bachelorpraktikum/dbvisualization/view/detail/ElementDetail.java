package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Element;

import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;

public class ElementDetail extends ElementDetailBase {
    private Element element;

    public ElementDetail(Element element) {
        this.element = element;
    }

    @Override
    String getName() {
        try {
            String[] names = element.getName().split("_");
            return names[names.length - 1];
        } catch (IndexOutOfBoundsException ignored) {
            return element.getName();
        }
    }

    @Override
    @Nullable
    List<URL> getImageUrls() {
        return element.getType().getImageUrls();
    }

    @Override
    Coordinates getCoordinates() {
        return element.getNode().getCoordinates();
    }

    @Override
    boolean isTrain() {
        return false;
    }

    Element.State getState() {
        return element.getState();
    }
}
