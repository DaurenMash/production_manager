package com.prodman.desktop.controller;

import com.prodman.desktop.ProdManApplication;
import com.prodman.desktop.utils.TokenManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController {

    @FXML private BorderPane contentArea;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private MenuItem usersMenuItem;

    @FXML
    public void initialize() {
        userLabel.setText(TokenManager.getUsername());
        roleLabel.setText(TokenManager.getRole());

        if ("ADMIN".equals(TokenManager.getRole())) {
            usersMenuItem.setVisible(true);
        }

        showDashboardView();
    }

    @FXML
    public void showDashboardView() {
        loadView("/view/dashboard.fxml");
    }

    @FXML
    public void showEquipmentView() {
        loadView("/view/equipment.fxml");
    }

    @FXML
    public void showWorkCalendarView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/work-calendar.fxml"));
            Pane calendarView = loader.load();
            contentArea.setCenter(calendarView);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load work calendar view: " + e.getMessage());
        }
    }

    @FXML
    public void showUsersView() {
        loadView("/view/users.fxml");
    }

    @FXML
    public void showReportsView() {
        loadView("/view/dashboard.fxml");
    }

    @FXML
    public void showScheduleView() {
        loadView("/view/dashboard.fxml");
    }

    @FXML
    public void showSettingsView() {
        loadView("/view/dashboard.fxml");
    }

    @FXML
    public void showEmployeesView() {
        loadView("/view/employees.fxml");
    }

    @FXML
    public void handleUsers() {
        loadView("/view/users.fxml");
    }

    @FXML
    public void handleLogout() throws Exception {
        ProdManApplication.logout();
    }

    @FXML
    public void handleExit() {
        System.exit(0);
    }

    @FXML
    public void handleProfile() {
        loadView("/view/dashboard.fxml");
    }

    @FXML
    public void handleAbout() {
        loadView("/view/dashboard.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane view = loader.load();
            contentArea.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load view: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}