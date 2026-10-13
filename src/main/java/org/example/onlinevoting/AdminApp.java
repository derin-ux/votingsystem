package org.example.onlinevoting;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.onlinevoting.util.SceneNavigator;

import java.io.IOException;

public class AdminApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneNavigator.setStage(primaryStage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/onlinevoting/fxml/admin_login.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Voting System — Admin");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
