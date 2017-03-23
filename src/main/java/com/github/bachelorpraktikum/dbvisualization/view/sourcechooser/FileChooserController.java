package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.datasource.FileSource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class FileChooserController implements SourceChooser<FileSource> {

    @FXML
    private TextField pathField;
    @FXML
    private Pane rootPane;

    @FXML
    private Button explorerButton;
    private FileChooser fileChooser;

    private ReadOnlyObjectWrapper<URI> fileUriProperty;
    private BooleanBinding fileChosen;

    @FXML
    private void initialize() {
        fileUriProperty = new ReadOnlyObjectWrapper<>();
        fileChosen = fileUriProperty.isNotNull();
        fileChooser = new FileChooser();
        String initialDirectory = getInitialDirectory();
        fileChooser.setInitialDirectory(new File(initialDirectory));

        explorerButton.setOnAction(event -> updatePath(openFileChooser()));
        explorerButton.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                explorerButton.fire();
            }
        });

        pathField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                fileUriProperty.set(null);
                return;
            }
            setInitialDirectory(newValue);
            URI uri = new File(newValue).getAbsoluteFile().toURI();
            fileUriProperty.set(uri);
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
    @Nullable
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

    @Nonnull
    @Override
    public FileSource getResource() throws IOException {
        return new FileSource(new File(fileUriProperty.get().toURL().getFile()));
    }

    @Nonnull
    @Override
    public ObservableBooleanValue inputChosen() {
        return fileChosen;
    }

    /**
     * Tries to retrieve the initial directory from the configuration file.
     * If that fails, the $HOME directory will be used.
     *
     * @return Initial directory for the file chooser
     */
    private String getInitialDirectory() {
        String path = ConfigKey.initialLogFileDirectory.get(System.getProperty("user.home"));
        if (!new File(path).isDirectory()) {
            return System.getProperty("user.home");
        }
        return path;
    }

    private void setInitialDirectory(String path) {
        File file = new File(path);
        if (file.isFile()) {
            path = file.getParent();
            ConfigKey.initialLogFileDirectory.set(path);
            fileChooser.setInitialDirectory(new File(path));
        }
    }
}
