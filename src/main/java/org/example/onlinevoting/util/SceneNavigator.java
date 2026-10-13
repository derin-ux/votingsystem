package org.example.onlinevoting.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneNavigator {

    private static Stage primaryStage;

    private SceneNavigator() {
        // Utility class, not meant to be instantiated
    }

    // Called once from AdminApp / VoterApp so the navigator knows which window to change
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlPath) throws IOException {
        switchTo(fxmlPath, null);
    }

    // Returns the screen's controller so the caller can pass data in if it needs to
    public static <T> T switchTo(String fxmlPath, String title) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneNavigator.setStage() was never called.");
        }
        URL url = SceneNavigator.class.getResource(fxmlPath);
        if (url == null){
            throw new IOException("Could not load this page");
        }

        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root));
        if (title != null) {
            primaryStage.setTitle(title);
        }
        primaryStage.show();

        return loader.getController();
    }
}