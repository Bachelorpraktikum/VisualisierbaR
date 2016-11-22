package com.github.bachelorpraktikum.dbvisualization;

import com.github.bachelorpraktikum.dbvisualization.view.SourceController;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class DBVisualizer extends Application {
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/SourceChooser.fxml"));
        loader.load();
        SourceController controller = loader.getController();

        controller.setStage(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(DBVisualizer.class, args);
    }
}
