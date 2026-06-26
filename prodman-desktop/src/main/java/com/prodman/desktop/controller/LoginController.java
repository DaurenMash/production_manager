package com.prodman.desktop.controller;

import com.prodman.desktop.ProdManApplication;
import com.prodman.desktop.model.AuthResponse;
import com.prodman.desktop.service.AuthService;
import com.prodman.desktop.utils.TokenManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        loginButton.setOnAction(e -> handleLogin());

        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });

        // Загрузка сохраненного username
        String savedUsername = TokenManager.getUsername();
        if (savedUsername != null && !savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Введите username и пароль");
            return;
        }

        loginButton.setDisable(true);
        progressIndicator.setVisible(true);
        errorLabel.setText("");

        new Thread(() -> {
            try {
                // 1. Выполняем логин
                AuthResponse response = AuthService.login(username, password);

                // 2. ДИАГНОСТИКА: выводим полученные данные
                System.out.println("DEBUG: ===== LOGIN RESPONSE =====");
                System.out.println("DEBUG: response = " + response);
                System.out.println("DEBUG: accessToken = " + response.getAccessToken());
                System.out.println("DEBUG: refreshToken = " + response.getRefreshToken());
                System.out.println("DEBUG: username = " + response.getUsername());
                System.out.println("DEBUG: userId = " + response.getUserId());
                System.out.println("DEBUG: role = " + response.getRole());
                System.out.println("DEBUG: ===========================");

                // 3. Сохраняем токены
                if (response.getAccessToken() == null || response.getAccessToken().isEmpty()) {
                    System.err.println("ERROR: accessToken is null or empty!");
                    Platform.runLater(() -> {
                        errorLabel.setText("Ошибка: сервер не вернул токен");
                        loginButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                    return;
                }

                TokenManager.saveTokens(
                        response.getAccessToken(),
                        response.getRefreshToken(),
                        response.getUsername(),
                        response.getUserId(),
                        response.getRole()
                );

                // 4. Загружаем главное окно
                Platform.runLater(() -> {
                    try {
                        ProdManApplication.loadMainView();
                    } catch (Exception e) {
                        errorLabel.setText("Ошибка загрузки главного окна");
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                System.err.println("ERROR during login: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    errorLabel.setText("Ошибка: " + e.getMessage());
                    loginButton.setDisable(false);
                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }
}