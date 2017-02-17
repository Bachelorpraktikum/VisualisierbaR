package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;
import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;

import java.io.File;
import java.net.URI;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;


public class FileChooserController implements SourceChooser {
    @FXML
    private TextField pathField;
    @FXML
    private Pane rootPane;

    @FXML
    private Button explorerButton;
    private FileChooser fileChooser;

    private ReadOnlyObjectWrapper<URI> fileURIProperty;

    @FXML
    private void initialize() {
        fileURIProperty = new ReadOnlyObjectWrapper<>();
        fileChooser = new FileChooser();
        String initialDirectory = getInitialDirectory();
        setInitialDirectory(initialDirectory);
        explorerButton.setOnAction(event -> updatePath(openFileChooser()));

        explorerButton.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                explorerButton.fire();
            }
        });

        pathField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                fileURIProperty.set(null);
                return;
            }
            URI uri = new File(newValue).getAbsoluteFile().toURI();
            fileURIProperty.set(uri);
        });

        pathField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ((Button) rootPane.getScene().lookup("#openSource")).fire();
            }
        });
    }

    /**
     * Opens the system explorer where a file can be chosen.
     * If a file was chosen the {@link File file} will be returned.
     * If no file was chosen and the explorer has been closed, null will be returned.
     *
     * @return A {@link File file} or null
     */
    private File openFileChooser() {
        return fileChooser.showOpenDialog(rootPane.getScene().getWindow());
    }

    /**
     * Updates the text in the path field with the absolute path of the <code>file</code>.
     *
     * @param file File to get the path from
     */
    private void updatePath(@Nullable File file) {
        if (file == null) {
            return;
        }

        pathField.setText(file.getAbsolutePath());
    }

    private void setInitialDirectory(String initialDirectory) {
        fileChooser.setInitialDirectory(new File(initialDirectory));
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public URI getResourceURI() {
        return fileURIProperty.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ReadOnlyObjectProperty<URI> resourceURIProperty() {
        return fileURIProperty.getReadOnlyProperty();
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
        return DataSource.Type.LOG_FILE;
    }

    @Override
    public void setInitialURI(URI initialURI) {
        setInitialDirectory(initialURI.getPath());

        ConfigFile.getInstance().put(getLogFileKey(), initialURI.getPath());
    }

    /**
     * Tries to retrieve the initial directory from the configuration file.
     * If that fails, the $HOME directory will be used.
     *
     * @return Initial directory for the file chooser
     */
    private String getInitialDirectory() {
        String defaultDirectory = System.getProperty("user.home");

        return String.valueOf(ConfigFile.getInstance().getOrDefault(getLogFileKey(), defaultDirectory));
    }

    private String getLogFileKey() {
        String logFileKey = ResourceBundle.getBundle("config_keys").getString("initialDirectoryKey");
        return String.format(logFileKey, getResourceType().toString());
    }
}
