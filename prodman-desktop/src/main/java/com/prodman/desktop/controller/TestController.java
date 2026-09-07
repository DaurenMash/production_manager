package com.prodman.desktop.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.config.ApiConfig;
import com.prodman.desktop.utils.TokenManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ===== Таблица тестов =====
    @FXML private TableView<Map<String, Object>> testsTable;
    @FXML private TableColumn<Map<String, Object>, String> colTitle;
    @FXML private TableColumn<Map<String, Object>, String> colDescription;
    @FXML private TableColumn<Map<String, Object>, Integer> colQuestions;
    @FXML private TableColumn<Map<String, Object>, Void> colAction;

    // ===== Таблица результатов =====
    @FXML private TableView<Map<String, Object>> resultsTable;
    @FXML private TableColumn<Map<String, Object>, String> colTestName;
    @FXML private TableColumn<Map<String, Object>, String> colScore;
    @FXML private TableColumn<Map<String, Object>, String> colStatus;
    @FXML private TableColumn<Map<String, Object>, String> colDate;

    private ObservableList<Map<String, Object>> testsList = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> resultsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTestTable();
        setupResultsTable();
        loadTests();
        loadMyResults();
    }

    // ===== Настройка таблицы тестов =====
    @SuppressWarnings("unchecked")
    private void setupTestTable() {
        colTitle.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty((String) cell.getValue().get("title")));
        colDescription.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty((String) cell.getValue().get("description")));
        colQuestions.setCellValueFactory(cell -> {
            List<Map<String, Object>> questions = (List<Map<String, Object>>) cell.getValue().get("questions");
            return new javafx.beans.property.SimpleIntegerProperty(
                questions != null ? questions.size() : 0
            ).asObject();
        });

        // Кнопка "Пройти тест"
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button startBtn = new Button("▶ Пройти");

            {
                startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
                startBtn.setOnAction(e -> {
                    Map<String, Object> test = getTableView().getItems().get(getIndex());
                    openTestPassing(test);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : startBtn);
            }
        });

        testsTable.setItems(testsList);
    }

    // ===== Настройка таблицы результатов =====
    @SuppressWarnings("unchecked")
    private void setupResultsTable() {
        colTestName.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty((String) cell.getValue().get("testTitle")));
        colScore.setCellValueFactory(cell -> {
            Double score = (Double) cell.getValue().get("score");
            return new javafx.beans.property.SimpleStringProperty(
                score != null ? String.format("%.1f%%", score) : "0%"
            );
        });
        colStatus.setCellValueFactory(cell -> {
            Boolean passed = (Boolean) cell.getValue().get("passed");
            return new javafx.beans.property.SimpleStringProperty(
                passed != null && passed ? "✅ Сдан" : "❌ Не сдан"
            );
        });
        colDate.setCellValueFactory(cell -> {
            String date = (String) cell.getValue().get("completedAt");
            if (date != null && date.length() > 10) {
                return new javafx.beans.property.SimpleStringProperty(date.substring(0, 10));
            }
            return new javafx.beans.property.SimpleStringProperty(date != null ? date : "");
        });

        resultsTable.setItems(resultsList);
    }

    // ===== Загрузка тестов =====
    private void loadTests() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен");
            return;
        }

        try {
            // ✅ РАСКОММЕНТИРУЙТЕ ЭТОТ БЛОК
            Request request = new Request.Builder()
                    .url(ApiConfig.TESTS)
                    .header("Authorization", "Bearer " + token)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    List<Map<String, Object>> list = mapper.readValue(
                            json,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    testsList.clear();
                    testsList.addAll(list);
                } else {
                    showError("Ошибка загрузки тестов: " + response.code());
                }
            }

        } catch (Exception e) {
            log.error("Error loading tests", e);
            showError("Ошибка загрузки тестов: " + e.getMessage());
        }
    }

    // ===== Загрузка моих результатов =====
    private void loadMyResults() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен");
            return;
        }

        try {
            // ✅ РАСКОММЕНТИРУЙТЕ
            String employeeId = TokenManager.getUserId();
            String url = ApiConfig.TEST_RESULTS_BY_EMPLOYEE + employeeId;
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + token)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    List<Map<String, Object>> list = mapper.readValue(
                            json,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    resultsList.clear();
                    resultsList.addAll(list);
                }
            }

        } catch (Exception e) {
            log.error("Error loading results", e);
            showError("Ошибка загрузки результатов: " + e.getMessage());
        }
    }

    // ===== Открыть прохождение теста =====
    private void openTestPassing(Map<String, Object> test) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/test-passing.fxml"));
            Parent root = loader.load();

            TestPassingController controller = loader.getController();
            controller.setTest(test);
            controller.setOnCompleteCallback(this::loadMyResults);

            Stage stage = new Stage();
            stage.setTitle("Прохождение теста: " + test.get("title"));
            stage.setScene(new Scene(root, 700, 500));
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error opening test", e);
            showError("Ошибка открытия теста: " + e.getMessage());
        }
    }

    // ===== Создание теста (только ADMIN) =====
    @FXML
    private void handleCreateTest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/test-create.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Создание теста");
            stage.setScene(new Scene(root, 800, 600));
            stage.showAndWait();

            loadTests(); // Обновляем список после создания

        } catch (Exception e) {
            log.error("Error opening create test", e);
            showError("Ошибка открытия создания теста: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadTests();
        loadMyResults();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}