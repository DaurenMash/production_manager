package com.prodman.desktop.controller;

import com.prodman.desktop.ProdManApplication;
import com.prodman.desktop.utils.TokenManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    // ===== FXML-элементы =====
    @FXML private BorderPane contentArea;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private MenuItem usersMenuItem;

    // Ссылки на кнопки навигации (добавьте их в FXML с fx:id)
    // Если в FXML нет fx:id, раскомментируйте и добавьте их в FXML
    @FXML private Button dashboardButton;
    @FXML private Button equipmentButton;
    @FXML private Button reportsButton;
    @FXML private Button scheduleButton;
    @FXML private Button workCalendarButton;
    @FXML private Button settingsButton;
    @FXML private Button usersButton;
    @FXML private Button employeesButton;
    @FXML private Button logoutButton;

    // Контейнер с кнопками навигации
    @FXML private VBox leftVBox;

    // ===== Состояние =====
    private Map<Button, Runnable> viewActions = new HashMap<>();
    private Button activeButton;

    // ===== Инициализация =====
    @FXML
    public void initialize() {
        // Устанавливаем информацию о пользователе
        userLabel.setText(TokenManager.getUsername());
        roleLabel.setText(TokenManager.getRole());

        // Показываем пункт меню "Пользователи" только для ADMIN
        if ("ADMIN".equals(TokenManager.getRole())) {
            usersMenuItem.setVisible(true);
        }

        // Настраиваем действия для кнопок
        initializeViewActions();

        // Загружаем дашборд по умолчанию
        showDashboardView();
    }

    // ===== Настройка действий для кнопок =====
    private void initializeViewActions() {
        viewActions.put(dashboardButton, this::showDashboardView);
        viewActions.put(equipmentButton, this::showEquipmentView);
        viewActions.put(reportsButton, this::showReportsView);
        viewActions.put(scheduleButton, this::showScheduleView);
        viewActions.put(workCalendarButton, this::showWorkCalendarView);
        viewActions.put(settingsButton, this::showSettingsView);
        viewActions.put(usersButton, this::showUsersView);
        viewActions.put(employeesButton, this::showEmployeesView);
    }

    // ===== Установка активной кнопки =====
    private void setActiveButton(Button button) {
        // Убираем активный класс у всех кнопок
        for (Node node : leftVBox.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("active");
            }
        }
        // Добавляем активный класс выбранной кнопке
        if (button != null) {
            button.getStyleClass().add("active");
            activeButton = button;
        }
    }

    // ===== Загрузка представлений =====
    @FXML
    public void showDashboardView() {
        setActiveButton(dashboardButton);
        loadView("/view/dashboard.fxml", "Дашборд");
    }

    @FXML
    public void showEquipmentView() {
        setActiveButton(equipmentButton);
        loadView("/view/equipment.fxml", "Оборудование");
    }

    @FXML
    public void showWorkCalendarView() {
        setActiveButton(workCalendarButton);
        loadView("/view/work-calendar.fxml", "Календарь работы");
    }

    @FXML
    public void showUsersView() {
        setActiveButton(usersButton);
        loadView("/view/users.fxml", "Пользователи");
    }

    @FXML
    public void showReportsView() {
        setActiveButton(reportsButton);
        loadView("/view/reports.fxml", "Отчеты");
    }

    @FXML
    public void showScheduleView() {
        setActiveButton(scheduleButton);
        loadView("/view/schedule.fxml", "График работ");
    }

    @FXML
    public void showSettingsView() {
        setActiveButton(settingsButton);
        loadView("/view/settings.fxml", "Настройки");
    }

    @FXML
    public void showEmployeesView() {
        setActiveButton(employeesButton);
        loadView("/view/employees.fxml", "Работники");
    }

    // ===== Обработчики меню =====
    @FXML
    public void handleUsers() {
        showUsersView();
    }

    @FXML
    public void handleLogout() {
        try {
            ProdManApplication.logout();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка при выходе: " + e.getMessage());
        }
    }

    @FXML
    public void handleExit() {
        System.exit(0);
    }

    @FXML
    public void handleProfile() {
        loadView("/view/profile.fxml", "Профиль");
    }

    @FXML
    public void handleAbout() {
        showInfo("О программе", "ProdMan v1.0\n\n" +
                "Система управления производством\n" +
                "© 2026 Все права защищены.");
    }

    // ===== Вспомогательные методы =====
    private void loadView(String fxmlPath, String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane view = loader.load();

            // Анимация смены контента (опционально)
            view.setOpacity(0);
            contentArea.setCenter(view);

            // Плавное появление
            view.setOpacity(1);

        } catch (NullPointerException e) {
            // Если FXML файл не найден
            showError("Представление '" + viewName + "' не найдено.\n" +
                    "Проверьте наличие файла: " + fxmlPath);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка загрузки '" + viewName + "':\n" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Неизвестная ошибка при загрузке '" + viewName + "':\n" + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #1e1e2f; -fx-text-fill: white;");
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #1e1e2f; -fx-text-fill: white;");
        alert.showAndWait();
    }
}