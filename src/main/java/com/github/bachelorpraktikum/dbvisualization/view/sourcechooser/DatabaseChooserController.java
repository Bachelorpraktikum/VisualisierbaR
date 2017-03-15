package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javax.annotation.Nonnull;

public class DatabaseChooserController implements SourceChooser<DataSource> {

    @FXML
    private BorderPane rootPaneDatabase;
    @FXML
    private TextField ipField;
    @FXML
    private TextField databaseNameField;
    @FXML
    private TextField portField;
    @FXML
    public Label uriError;

    private ReadOnlyObjectWrapper<URI> databaseURIProperty;
    private ReadOnlyObjectWrapper<String> databaseNameProperty;
    private ReadOnlyObjectWrapper<Integer> portProperty;
    private ReadOnlyObjectWrapper<URI> completeURIProperty;
    private ObservableBooleanValue uriChosen;

    @FXML
    public void initialize() {
        databaseURIProperty = new ReadOnlyObjectWrapper<>();
        databaseNameProperty = new ReadOnlyObjectWrapper<>();
        portProperty = new ReadOnlyObjectWrapper<>();
        completeURIProperty = new ReadOnlyObjectWrapper<>();
        uriChosen = completeURIProperty.isNotNull();

        ipField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                databaseURIProperty.set(null);
                return;
            }
            URI uri = null;
            newValue = newValue.trim();
            try {
                uri = new URI(newValue);
                databaseURIProperty.set(uri);
                check();
            } catch (URISyntaxException ignored) {
                String message = String.format("%s is not a valid URI.", newValue);
                Logger.getLogger(getClass().getName()).info(message);
            } finally {
                // Display the error message if the URI hasn't been set
                uriError.setVisible(uri == null);
            }
        });

        databaseNameProperty.bindBidirectional(databaseNameField.textProperty());

        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            newValue = newValue.trim();
            try {
                int port = Integer.valueOf(newValue);
                portProperty.set(port);
                check();
            } catch (NumberFormatException ignored) {
                portProperty.set(null);
            }
        });

        loadInitialValues();
    }

    private void loadInitialValues() {
        String configKey = ConfigKey.initialDatabaseUri.getKey();
        String uriString = ConfigFile.getInstance().getProperty(configKey);
        if (uriString != null && !uriString.isEmpty()) {
            try {
                URI uri = URI.create(uriString);
                ipField.setText(String.format("%s://%s", uri.getScheme(), uri.getHost()));
                portField.setText(String.valueOf(uri.getPort()));
                databaseNameField.setText(uri.getPath().substring(1));
            } catch (IllegalArgumentException e) {
                String message = String.format("URI from config isn't valid:\n%s", e);
                Logger.getLogger(getClass().getName()).info(message);
            }
        }
    }

    private void check() {
        if (databaseURIProperty.get() != null
            && databaseNameProperty.get() != null
            && portProperty.get() != null) {
            URI uri = createCompleteURI();
            if (uri != null) {
                completeURIProperty.set(uri);
                setInitialUri(uri);
            }
        } else {
            completeURIProperty.set(null);
        }
    }

    private URI createCompleteURI() {
        String uriString = String
            .format("%s:%d/%s", databaseURIProperty.get().toString(), portProperty.get(),
                databaseNameProperty.get());
        URI uri = null;

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            String message = String.format("Couldn't create uri after check: %s", uriString);
            Logger.getLogger(getClass().getName()).severe(message);
        }

        return uri;
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

    private void setInitialUri(@Nonnull URI uri) {
        String key = ConfigKey.initialDatabaseUri.getKey();
        ConfigFile.getInstance().setProperty(key, uri.toString());
    }
}
