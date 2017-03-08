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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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
        sb.append("Message{")
            .append("time=").append(getTime())
            .append(", ").append("node=").append(getNode().getReadableName())
            .append(", ").append("text=").append(text)
            .append("}");
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
        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE);
        dialog.initOwner(node.getScene().getWindow());
        dialog.setResultConverter(type -> null);

        Bounds nodeBounds = node.localToScreen(node.getBoundsInLocal());
        dialog.setX(nodeBounds.getMaxX());
        dialog.setY(nodeBounds.getMaxY());

        dialog.setTitle("MessageEvent at time " + getTime());
        dialog.setContentText(text);

        // The dialog need a button. Otherwise it won't be closable.
        ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButton);
        dialog.show();
    }
}
