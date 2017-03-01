package com.github.bachelorpraktikum.dbvisualization.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
final class MessageEvent implements Event {

    private static final Logger log = Logger.getLogger(Node.class.getName());

    private final int time;
    @Nonnull
    private final String text;
    @Nonnull
    private final Node node;
    @Nonnull
    private final ObservableList<String> warnings;

    MessageEvent(int time, String text, Node node, List<String> warnings) {
        this.time = time;
        this.text = Objects.requireNonNull(text);
        this.node = Objects.requireNonNull(node);
        this.warnings = FXCollections.observableList(new ArrayList<>(warnings));
    }

    @Override
    public String toString() {
        return "MessageEvent{"
            + "time=" + time
            + ", text=" + text
            + ", node=" + node
            + ", warnings=" + warnings
            + '}';
    }

    @Override
    public int getTime() {
        return time;
    }

    @Nonnull
    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        for (String warning : getWarnings()) {
            sb.append(System.lineSeparator())
                .append("[WARN] ").append(warning);
        }
        return sb.toString();
    }

    @Nonnull
    @Override
    public ObservableList<String> getWarnings() {
        return warnings;
    }

    @Nonnull
    private Node getNode() {
        return node;
    }

    void fire(Function<Node, javafx.scene.Node> nodeResolve) {
        javafx.scene.Node node = nodeResolve.apply(getNode());
        Alert dialog = new Alert(AlertType.INFORMATION);
        dialog.initModality(Modality.NONE);
        dialog.initOwner(node.getScene().getWindow());

        Bounds nodeBounds = node.localToScreen(node.getBoundsInLocal());
        dialog.setX(nodeBounds.getMaxX());
        dialog.setY(nodeBounds.getMaxY());

        dialog.setTitle("MessageEvent " + getNode().getReadableName());
        dialog.setContentText(getDescription());
        dialog.setHeaderText("Message at time " + getTime() + ":");
        dialog.show();
    }
}
