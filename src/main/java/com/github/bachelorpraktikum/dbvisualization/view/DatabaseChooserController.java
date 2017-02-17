package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DatabaseChooserController implements SourceChooser {

    @FXML
    public BorderPane rootPane;
    @FXML
    public TextField uriField;
    @FXML
    public Label uriError;

    private ReadOnlyObjectWrapper<URI> databaseUriProperty;

    @FXML
    public void initialize() {
        databaseUriProperty = new ReadOnlyObjectWrapper<>();

        uriField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                databaseUriProperty.set(null);
                return;
            }
            URI uri = null;
            try {
                uri = new URI(newValue);
                databaseUriProperty.set(uri);
            } catch (URISyntaxException ignored) {
                ignored.printStackTrace();
            } finally {
                // Display the error message if the URI hasn't been set
                uriError.setVisible(uri == null);
            }
        });
    }

    @Nullable
    @Override
    public URI getResourceUri() {
        return databaseUriProperty.getValue();
    }

    @Nonnull
    @Override
    public ReadOnlyObjectProperty<URI> resourceUriProperty() {
        return databaseUriProperty.getReadOnlyProperty();
    }

    @Nonnull
    @Override
    public String getRootPaneId() {
        return rootPane.getId();
    }

    @Nonnull
    @Override
    public DataSource.Type getResourceType() {
        return DataSource.Type.DATABASE;
    }

    @Override
    public void setInitialUri(URI initialUri) {
        uriField.setText(initialUri.getPath());
    }
}
