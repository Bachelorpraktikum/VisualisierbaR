package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Element;

import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;

public class ElementDetail extends ElementDetailBase {
    private Element element;

    ElementDetail(Element element) {
        this.element = element;
    }

    @Override
    String getName() {
        try {
            return element.getName().split("_")[0];
        } catch (IndexOutOfBoundsException ignored) {
            return element.getName();
        }
    }

    @Override
    @Nullable
    URL getImageURL() {
        List<URL> imageUrls = element.getType().getImageUrls();
        if (imageUrls.size() > 0) {
            return null;
        }

        return imageUrls.get(0);
    }

    @Override
    Coordinates getCoordinates() {
        return element.getNode().getCoordinates();
    }

    @Override
    boolean isTrain() {
        return false;
    }
}
