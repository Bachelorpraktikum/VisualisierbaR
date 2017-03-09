package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javax.annotation.Nonnull;

public class DatabaseChooserController implements SourceChooser<DataSource> {

    @FXML
    public BorderPane rootPane;
    @FXML
    public TextField uriField;
    @FXML
    public Label uriError;

    private ReadOnlyObjectWrapper<URI> databaseUriProperty;
    private ObservableBooleanValue uriChosen;

    @FXML
    public void initialize() {
        databaseUriProperty = new ReadOnlyObjectWrapper<>();
        uriChosen = databaseUriProperty.isNotNull();

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
                System.out.println(ignored.getMessage());
                databaseUriProperty.set(null);
            } finally {
                // Display the error message if the URI hasn't been set
                uriError.setVisible(uri == null);
            }
        });

        URI initialUri = getInitialUri();
        uriField.setText(initialUri == null ? "" : initialUri.toString());

        databaseUriProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                setInitialUri(newValue);
            }
        });
    }

    @Nonnull
    @Override
    public ObservableBooleanValue inputChosen() {
        return uriChosen;
    }

    @Nonnull
    @Override
    public DataSource getResource() throws IOException {
        return null; // TODO this should probably return a SubprocessSource
    }

    private URI getInitialUri() {
        String key = ConfigKey.initialDatabaseUri.getKey();

        String uri = ConfigFile.getInstance().getProperty(key);
        if (uri != null) {
            try {
                return new URI(uri);
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private void setInitialUri(@Nonnull URI uri) {
        String key = ConfigKey.initialDatabaseUri.getKey();
        ConfigFile.getInstance().setProperty(key, uri.toString());
    }
}
