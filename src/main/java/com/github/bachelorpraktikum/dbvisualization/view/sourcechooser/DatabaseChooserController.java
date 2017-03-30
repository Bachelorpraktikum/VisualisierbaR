package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.database.Database;
import com.github.bachelorpraktikum.dbvisualization.database.DatabaseUser;
import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import com.github.bachelorpraktikum.dbvisualization.datasource.DatabaseSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
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
            } else {
                databaseURIProperty.set(newValue.trim());
            }
            check();
        });

        databaseNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            databaseNameProperty.set(newValue);
            check();
        });

        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            newValue = newValue.trim();
            try {
                int port = Integer.parseUnsignedInt(newValue);
                portProperty.set(port);
            } catch (NumberFormatException ignored) {
                portProperty.set(INVALID_PORT);
            }
            check();
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
            nameString = stripLeadingSlash(nameString);
            URI uri = createCompleteURI(uriString, port, nameString);

            completeURIProperty.set(uri);
            databaseURIField.setText(uri.getHost());
            String path = uri.getPath();
            path = stripLeadingSlash(path);
            databaseNameField.setText(path);
            portField.setText(String.valueOf(uri.getPort()));
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
        uriError.setVisible(uri == null);
    }

    private URI createCompleteURI(String url, int port, String path) {
        String uriString = String.format("%s:%d/%s", url, port, path);
        uriString = prependScheme(uriString);
        URI uri = null;

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            String message = String.format("Couldn't create uri after check: %s", uriString);
            Logger.getLogger(getClass().getName()).severe(message);
        }

        return uri;
    }

    private String prependScheme(String uri) {
        if (!uri.contains("://")) {
            uri = String.format("%s://%s", DEFAULT_PROTOCOL, uri);
        }

        return uri;
    }

    private String stripLeadingSlash(@Nonnull String uriString) {
        if (uriString.startsWith("/")) {
            uriString = uriString.replaceFirst("/", "");
        }

        return uriString;
    }

    @Nonnull
    @Override
    public ObservableBooleanValue inputChosen() {
        return uriChosen;
    }

    @Nonnull
    @Override
    public DataSource getResource() throws IOException {
        Database database = createDatabase();
        return new DatabaseSource(database, null);
    }

    private Database createDatabase() {
        Database database = new Database(completeURIProperty.get());
        DatabaseUser user = database.getUser();
        while(!database.testConnection()) {
            DatabaseUser newUser = showLoginWindow();
        }

        return database;
    }

    private void setInitialUri(@Nonnull URI uri) {
        String uriKey = ConfigKey.initialDatabaseUri.getKey();
        String portKey = ConfigKey.initialDatabasePort.getKey();
        String nameKey = ConfigKey.initialDatabaseName.getKey();
        if (uri.getHost() != null) {
            ConfigFile.getInstance().setProperty(uriKey, uri.getHost());
        }
        ConfigFile.getInstance().setProperty(portKey, String.valueOf(uri.getPort()));
        ConfigFile.getInstance().setProperty(nameKey, stripLeadingSlash(uri.getPath()));
    }


    private DatabaseUser showLoginWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginWindow.fxml"));
        loader.setResources(ResourceBundle.getBundle("bundles.localization"));
        try {
            loader.load();
        } catch (IOException e) {
            // This should never happen, because the location is set (see load function)
            return null;
        }

        LoginController controller = loader.getController();
        controller.setStage((Stage) rootPaneDatabase.getScene().getWindow());
        return new DatabaseUser(controller.getUser(), controller.getPassword());
    }
}
