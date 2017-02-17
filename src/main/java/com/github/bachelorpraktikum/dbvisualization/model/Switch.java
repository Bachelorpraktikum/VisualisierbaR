package com.github.bachelorpraktikum.dbvisualization.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a switch consisting of exactly 3 {@link Element elements} of type {@link
 * Element.Type#WeichenPunktImpl}.
 */
@ParametersAreNonnullByDefault
public final class Switch {

    @Nonnull
    private final List<Element> elements;

    private Switch() {
        elements = new ArrayList<>(3);
    }

    private void addElement(Element element) {
        if (elements.size() == 3) {
            throw new IllegalStateException("only 3 elements per switch allowed");
        }
        elements.add(element);
    }

    /**
     * Gets a list containing the 3 {@link Element elements} this {@link Switch} consists of.
     *
     * @return the elements
     * @throws IllegalStateException if this switch has not been completely initialized
     */
    @Nonnull
    public List<Element> getElements() {
        if (elements.size() < 3) {
            throw new IllegalStateException(
                "Switch not completely initialized. Elements: " + elements);
        }
        return Collections.unmodifiableList(elements);
    }

    /**
     * Gets the main element of this switch.
     *
     * @return the main element
     */
    @Nonnull
    public Element getMainElement() {
        return elements.stream()
            .filter(element -> element.getNode().getEdges().size() == 3)
            .findFirst()
            .orElseThrow(IllegalStateException::new);
    }

    @Override
    public String toString() {
        return "Switch{"
            + "elements=" + elements
            + '}';
    }

    /**
     * Gets the {@link Factory} instance for the given {@link Context}.
     *
     * @param context the context
     * @return the factory
     * @throws NullPointerException if context is null
     */
    @Nonnull
    static Factory in(Context context) {
        return Factory.getInstance(context);
    }

    static final class Factory {

        private static final Map<Context, Factory> instances = new WeakHashMap<>();
        @Nullable
        private Switch currentSwitch;

        private static Factory getInstance(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }
            return instances.computeIfAbsent(context, g -> new Factory());
        }

        private Factory() {
        }

        @Nonnull
        Switch create(Element element) {
            return buildSwitch(element);
        }

        @Nonnull
        private Switch buildSwitch(Element element) {
            if (currentSwitch == null) {
                currentSwitch = new Switch();
            }

            Switch result = currentSwitch;

            result.addElement(element);
            if (result.elements.size() == 3) {
                currentSwitch = null;
            }
            return result;
        }
    }
}
