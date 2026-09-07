package com.prodman.desktop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.config.ApiConfig;
import com.prodman.desktop.utils.TokenManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TestPassingController {

    private static final Logger log = LoggerFactory.getLogger(TestPassingController.class);
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML private Label titleLabel;
    @FXML private Label progressLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;

    private Map<String, Object> test;
    private List<Map<String, Object>> questions;
    private int currentQuestionIndex = 0;
    private Map<String, List<Integer>> selectedAnswers = new HashMap<>();
    private Runnable onCompleteCallback;

    public void setTest(Map<String, Object> test) {
        this.test = test;
        this.questions = (List<Map<String, Object>>) test.get("questions");
        if (questions == null) {
            questions = new ArrayList<>();
        }
        // Инициализируем выбранные ответы
        for (Map<String, Object> q : questions) {
            selectedAnswers.put((String) q.get("id"), new ArrayList<>());
        }
        showQuestion(0);
    }

    public void setOnCompleteCallback(Runnable callback) {
        this.onCompleteCallback = callback;
    }

    @FXML
    public void initialize() {
        prevButton.setOnAction(e -> navigateTo(currentQuestionIndex - 1));
        nextButton.setOnAction(e -> navigateTo(currentQuestionIndex + 1));
        submitButton.setOnAction(e -> submitTest());
    }

    private void navigateTo(int index) {
        if (index >= 0 && index < questions.size()) {
            // Сохраняем текущие ответы
            saveCurrentAnswers();
            currentQuestionIndex = index;
            showQuestion(index);
        }
    }

    private void saveCurrentAnswers() {
        // Ответы сохраняются в selectedAnswers при выборе вариантов
    }

    @SuppressWarnings("unchecked")
    private void showQuestion(int index) {
        questionsContainer.getChildren().clear();

        if (questions.isEmpty()) {
            Label empty = new Label("Вопросы не найдены");
            empty.setStyle("-fx-text-fill: #333;");
            questionsContainer.getChildren().add(empty);
            return;
        }

        Map<String, Object> q = questions.get(index);
        String questionId = (String) q.get("id");
        String text = (String) q.get("text");
        List<String> options = (List<String>) q.get("options");
        Boolean multiple = (Boolean) q.get("multipleChoice");
        if (multiple == null) multiple = false;

        // Заголовок вопроса
        Label questionLabel = new Label((index + 1) + ". " + text);
        questionLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 0 0 10 0;");
        questionsContainer.getChildren().add(questionLabel);

        // Прогресс
        progressLabel.setText((index + 1) + " / " + questions.size());

        // Варианты ответов
        List<Integer> selected = selectedAnswers.getOrDefault(questionId, new ArrayList<>());

        if (multiple) {
            for (int i = 0; i < options.size(); i++) {
                final int optionIndex = i;
                CheckBox cb = new CheckBox(options.get(i));
                cb.setSelected(selected.contains(i));
                cb.setStyle("-fx-text-fill: #333; -fx-padding: 5 0;");
                cb.setOnAction(e -> {
                    List<Integer> current = selectedAnswers.getOrDefault(questionId, new ArrayList<>());
                    if (cb.isSelected()) {
                        current.add(optionIndex);
                    } else {
                        current.remove(Integer.valueOf(optionIndex));
                    }
                    selectedAnswers.put(questionId, current);
                });
                questionsContainer.getChildren().add(cb);
            }
        } else {
            ToggleGroup group = new ToggleGroup();
            for (int i = 0; i < options.size(); i++) {
                final int optionIndex = i;
                RadioButton rb = new RadioButton(options.get(i));
                rb.setToggleGroup(group);
                rb.setSelected(selected.contains(i));
                rb.setStyle("-fx-text-fill: #333; -fx-padding: 5 0;");
                rb.setOnAction(e -> {
                    selectedAnswers.put(questionId, List.of(optionIndex));
                });
                questionsContainer.getChildren().add(rb);
            }
        }

        // Обновляем кнопки
        prevButton.setDisable(index == 0);
        nextButton.setDisable(index == questions.size() - 1);
        submitButton.setVisible(index == questions.size() - 1);
    }

    @SuppressWarnings("unchecked")
    private void submitTest() {
        // Сохраняем последние ответы
        saveCurrentAnswers();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Завершить тест?");
        confirm.setContentText("Вы уверены, что хотите завершить тест? Ответы нельзя будет изменить.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен");
            return;
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("testId", test.get("id"));
            request.put("employeeId", TokenManager.getUserId());
            request.put("employeeName", TokenManager.getUsername());
            request.put("answers", selectedAnswers);

            RequestBody body = RequestBody.create(
                mapper.writeValueAsString(request),
                MediaType.parse("application/json")
            );

            Request httpRequest = new Request.Builder()
                .url(ApiConfig.TEST_RESULTS_SUBMIT)
                .post(body)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Map<String, Object> result = mapper.readValue(json, Map.class);
                    
                    Double score = (Double) result.get("score");
                    Boolean passed = (Boolean) result.get("passed");
                    
                    String status = passed != null && passed ? "СДАН ✅" : "НЕ СДАН ❌";
                    String message = String.format("Результат: %.1f%% - %s", score != null ? score : 0, status);
                    
                    showResult(message, passed != null && passed);
                    
                    if (onCompleteCallback != null) {
                        onCompleteCallback.run();
                    }
                    
                    closeWindow();
                } else {
                    showError("Ошибка отправки теста: " + response.code());
                }
            }
        } catch (Exception e) {
            log.error("Error submitting test", e);
            showError("Ошибка: " + e.getMessage());
        }
    }

    private void showResult(String message, boolean passed) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Результат теста");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle(
            passed ? "-fx-background-color: #e8f5e9;" : "-fx-background-color: #fce4ec;"
        );
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}