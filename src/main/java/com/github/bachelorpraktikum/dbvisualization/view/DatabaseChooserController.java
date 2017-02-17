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

    private ReadOnlyObjectWrapper<URI> databaseURIProperty;

    @FXML
    public void initialize() {
        databaseURIProperty = new ReadOnlyObjectWrapper<>();

        uriField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                databaseURIProperty.set(null);
                return;
            }
            URI uri = null;
            try {
                uri = new URI(newValue);
                databaseURIProperty.set(uri);
            } catch (URISyntaxException ignored) {
            } finally {
                // Display the error message if the URI hasn't been set
                uriError.setVisible(uri == null);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public URI getResourceURI() {
        return databaseURIProperty.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ReadOnlyObjectProperty<URI> resourceURIProperty() {
        return databaseURIProperty.getReadOnlyProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getRootPaneId() {
        return rootPane.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public DataSource.Type getResourceType() {
        return DataSource.Type.DATABASE;
    }

    @Override
    public void setInitialURI(URI initialURI) {
        uriField.setText(initialURI.getPath());
    }
}
