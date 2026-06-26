package com.prodman.desktop;

import com.prodman.desktop.utils.TokenManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ProdManApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Установка иконки окна
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/prodman.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Icon not found, using default");
        }

        if (TokenManager.hasValidToken()) {
            loadMainView();
        } else {
            loadLoginView();
        }

        stage.setTitle("ProdMan - Управление производством");
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        stage.show();
    }

    public static void loadLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(ProdManApplication.class.getResource("/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setWidth(400);
            primaryStage.setHeight(500);
            primaryStage.setResizable(false);

            // Добавить иконку на сцену
            try {
                Image icon = new Image(ProdManApplication.class.getResourceAsStream("/icons/prodman.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(ProdManApplication.class.getResource("/view/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setResizable(true);

            try {
                Image icon = new Image(ProdManApplication.class.getResourceAsStream("/icons/prodman.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logout() throws Exception {
        TokenManager.clearToken();
        loadLoginView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}