package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Element;

import java.net.URL;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class GraphObject<T> {
    private static final Object TRAINS = new Object();
    private final T t;
    private final URL imageUrl;
    private final String name;

    private GraphObject(T t, URL imageUrl, String name) {
        this.t = t;
        this.imageUrl = imageUrl;
        this.name = name;
    }

    public static GraphObject<Element.Type> element(Element.Type elementType) {
        return new GraphObject<>(elementType, getUrl(elementType), elementType.getName());
    }

    public static GraphObject<?> trains() {
        return new GraphObject<>(TRAINS, Element.class.getResource(String.format("symbols/%s.png", "train")), "Züge");
    }

    private static URL getUrl(Element.Type type) {
        String f;
        switch (type) {
            case HauptSignalImpl:
                f = "haupt";
                break;
            case GefahrenPunktImpl:
                f = "achs";
                break;
            case MagnetImpl:
                f = "magnet";
                break;
            case VorSignalImpl:
                f = "vor";
                break;
            case SichtbarkeitsPunktImpl:
                f = "sicht";
                break;
            default:
                // todo LOG THIS
                f = "test";
        }

        return Element.class.getResource(String.format("symbols/%s.png", f));
    }

    public T getWrapped() {
        return t;
    }

    public URL getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphObject<?> that = (GraphObject<?>) o;

        return t.equals(that.t);
    }

    @Override
    public int hashCode() {
        return t.hashCode();
    }
}
