package com.github.bachelorpraktikum.dbvisualization.view;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

@Nonnull
public final class TooltipUtil {
    private TooltipUtil() {
    }

    public static void install(Node node, Tooltip tooltip) {
        install(node, tooltip, t -> {
            // Tooltip is already initialized with text, nothing to do here
        });
    }

    public static void install(Node node, Supplier<String> textSupplier) {
        install(node, new Tooltip(), t -> t.setText(textSupplier.get()));
    }

    private static void install(Node node, Tooltip tooltip, Consumer<Tooltip> tooltipPreparer) {
        node.setOnMouseEntered(event -> {
            Bounds bounds = node.localToScreen(node.getBoundsInLocal());
            tooltipPreparer.accept(tooltip);
            tooltip.show(node, bounds.getMinX(), bounds.getMaxY() + 5);
        });

        node.setOnMouseExited(event -> {
            tooltip.hide();
        });
    }
}
