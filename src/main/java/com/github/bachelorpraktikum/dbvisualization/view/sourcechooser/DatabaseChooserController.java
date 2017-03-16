package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javax.annotation.Nonnull;

public class DatabaseChooserController implements SourceChooser<DataSource> {

    private static final int DEFAULT_SQL_PORT = 3306;
    private static final String DEFAULT_PROTOCOL = "http";
    private static final int INVALID_PORT = -1;
    @FXML
    private BorderPane rootPaneDatabase;
    @FXML
    private TextField databaseURIField;
    @FXML
    private TextField databaseNameField;
    @FXML
    private TextField portField;
    @FXML
    public Label uriError;

    private ReadOnlyObjectWrapper<String> databaseURIProperty;
    private ReadOnlyObjectWrapper<String> databaseNameProperty;
    private IntegerProperty portProperty;
    private ReadOnlyObjectWrapper<URI> completeURIProperty;
    private ObservableBooleanValue uriChosen;

    @FXML
    public void initialize() {
        databaseURIProperty = new ReadOnlyObjectWrapper<>();
        databaseNameProperty = new ReadOnlyObjectWrapper<>();
        portProperty = new SimpleIntegerProperty(INVALID_PORT);
        completeURIProperty = new ReadOnlyObjectWrapper<>();
        uriChosen = completeURIProperty.isNotNull();

        databaseURIField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                databaseURIProperty.set(null);
                return;
            }
            databaseURIProperty.set(newValue.trim());
            check();
        });

        databaseNameProperty.bindBidirectional(databaseNameField.textProperty());

        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            newValue = newValue.trim();
            try {
                int port = Integer.parseUnsignedInt(newValue);
                portProperty.set(port);
                check();
            } catch (NumberFormatException ignored) {
                portProperty.set(DEFAULT_SQL_PORT);
            }
        });

        loadInitialValues();
    }

    private void loadInitialValues() {
        String uriConfigKey = ConfigKey.initialDatabaseUri.getKey();
        String portConfigKey = ConfigKey.initialDatabasePort.getKey();
        String nameConfigKey = ConfigKey.initialDatabaseName.getKey();
        String uriString = ConfigFile.getInstance().getProperty(uriConfigKey);
        String portString = ConfigFile.getInstance().getProperty(portConfigKey);
        String nameString = ConfigFile.getInstance().getProperty(nameConfigKey);
        if (uriString != null && portString != null && nameString != null) {
            int port = DEFAULT_SQL_PORT;
            try {
                port = Integer.parseUnsignedInt(portString);
            } catch (NumberFormatException e) {
                String message = String.format("Couldn't parse port to number: %s", e.getMessage());
                Logger.getLogger(getClass().getName()).info(message);
            }
            if (nameString.startsWith("/")) {
                nameString = nameString.replaceFirst("/", "");
            }
            URI uri = createCompleteURI(uriString, port, nameString);

            completeURIProperty.set(uri);
            databaseURIField.setText(uri.getHost());
            portField.setText(String.valueOf(uri.getPort()));
            String path = uri.getPath();
            if (path.startsWith("/")) {
                path = path.replaceFirst("/", "");
            }
            databaseNameField.setText(path);
        }

        if (portField.getText().trim().isEmpty()) {
            portField.setText(String.valueOf(DEFAULT_SQL_PORT));
        }
    }

    private void check() {
        URI uri = null;
        if (databaseURIProperty.get() != null
            && databaseNameProperty.get() != null && !databaseNameProperty.get().trim().isEmpty()
            && portProperty.get() != INVALID_PORT) {
            String uriString = databaseURIProperty.get();
            uri = createCompleteURI(uriString, portProperty.get(),
                databaseNameProperty.get());
            if (uri != null) {
                setInitialUri(uri);
            }
        }
        completeURIProperty.set(uri);
    }

    private URI createCompleteURI(String url, int port, String path) {
        String uriString = String
            .format("%s:%d/%s", url, port, path);
        if (!uriString.contains("://")) {
            uriString = String.format("%s://%s", DEFAULT_PROTOCOL, uriString);
        }
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
        String uriKey = ConfigKey.initialDatabaseUri.getKey();
        String portKey = ConfigKey.initialDatabasePort.getKey();
        String nameKey = ConfigKey.initialDatabaseName.getKey();
        if (uri.getHost() != null) {
            ConfigFile.getInstance().setProperty(uriKey, uri.getHost());
        }
        ConfigFile.getInstance().setProperty(portKey, String.valueOf(uri.getPort()));
        ConfigFile.getInstance().setProperty(nameKey, uri.getPath());
    }
}
