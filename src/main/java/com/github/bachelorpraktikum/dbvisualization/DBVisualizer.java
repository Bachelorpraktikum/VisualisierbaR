package com.github.bachelorpraktikum.dbvisualization;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import com.github.bachelorpraktikum.dbvisualization.view.sourcechooser.SourceController;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class DBVisualizer extends Application {

    private static final String APP_NAME_KEY = "app_name";

    static {
        try (InputStream in = DBVisualizer.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException e) {
            System.out.println("Can't initialize logging!");
            System.exit(100);
        }
    }

    private static final Logger log = Logger.getLogger(DBVisualizer.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        ResourceBundle localizationBundle = ResourceBundle.getBundle("bundles.localization");
        primaryStage.setTitle(localizationBundle.getString(APP_NAME_KEY));

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("view/sourcechooser/SourceChooser.fxml")
        );
        loader.setResources(localizationBundle);
        loader.load();
        SourceController controller = loader.getController();

        controller.setStage(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(DBVisualizer.class, args);
        ConfigFile.getInstance().store();
    }
}
