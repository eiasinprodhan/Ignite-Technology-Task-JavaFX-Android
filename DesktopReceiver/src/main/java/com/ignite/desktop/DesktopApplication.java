package com.ignite.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DesktopApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("views/connection-view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(
                getClass().getResource("styles/modern-style.css").toExternalForm()
        );

        stage.setTitle("IGNITE Communication System");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setResizable(true);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchScene(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                DesktopApplication.class.getResource("views/" + fxmlFile)
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(
                DesktopApplication.class.getResource("styles/modern-style.css").toExternalForm()
        );

        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}