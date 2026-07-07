package com.prodman.desktop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.config.ApiConfig;
import com.prodman.desktop.utils.TokenManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TestCreateController {

    private static final Logger log = LoggerFactory.getLogger(TestCreateController.class);
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private VBox questionsContainer;
    @FXML private Button addQuestionButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label messageLabel;

    private List<QuestionUI> questionUIs = new ArrayList<>();

    @FXML
    public void initialize() {
        addQuestionButton.setOnAction(e -> addQuestion());
        saveButton.setOnAction(e -> saveTest());
        cancelButton.setOnAction(e -> closeWindow());
        addQuestion(); // Добавляем первый пустой вопрос
    }

    private void addQuestion() {
        QuestionUI ui = new QuestionUI();
        questionUIs.add(ui);
        questionsContainer.getChildren().add(ui.getRoot());
    }

    @SuppressWarnings("unchecked")
    private void saveTest() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty()) {
            showMessage("Введите название теста", true);
            return;
        }

        if (questionUIs.isEmpty()) {
            showMessage("Добавьте хотя бы один вопрос", true);
            return;
        }

        // Собираем вопросы
        List<Map<String, Object>> questions = new ArrayList<>();
        for (QuestionUI ui : questionUIs) {
            Map<String, Object> q = ui.getQuestion();
            if (q == null) continue;
            questions.add(q);
        }

        if (questions.isEmpty()) {
            showMessage("Заполните вопросы корректно", true);
            return;
        }

        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showMessage("Не найден токен", true);
            return;
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("title", title);
            request.put("description", description);
            request.put("createdBy", TokenManager.getUsername());
            request.put("questions", questions);

            RequestBody body = RequestBody.create(
                mapper.writeValueAsString(request),
                MediaType.parse("application/json")
            );

            Request httpRequest = new Request.Builder()
                .url(ApiConfig.TESTS)
                .post(body)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                if (response.isSuccessful()) {
                    showMessage("✅ Тест успешно создан!", false);
                    closeWindow();
                } else {
                    showMessage("❌ Ошибка создания: " + response.code(), true);
                }
            }
        } catch (Exception e) {
            log.error("Error creating test", e);
            showMessage("❌ Ошибка: " + e.getMessage(), true);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError ? "-fx-text-fill: #d32f2f;" : "-fx-text-fill: #2e7d32;");
    }

    // ===== Внутренний класс для UI вопроса =====
    private class QuestionUI {
        private final VBox root;
        private final TextField questionField;
        private final VBox optionsContainer;
        private final CheckBox multipleCheck;
        private final Button addOptionButton;
        private final Button removeButton;
        private final List<TextField> optionFields = new ArrayList<>();

        public QuestionUI() {
            root = new VBox(10);
            root.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");

            // Текст вопроса
            questionField = new TextField();
            questionField.setPromptText("Введите текст вопроса...");

            // Чекбокс "Множественный выбор"
            multipleCheck = new CheckBox("Множественный выбор");

            // Контейнер для вариантов ответов
            optionsContainer = new VBox(5);

            // Кнопка добавления варианта
            addOptionButton = new Button("➕ Добавить вариант");
            addOptionButton.setOnAction(e -> addOption());

            // Кнопка удаления вопроса
            removeButton = new Button("✕ Удалить вопрос");
            removeButton.setStyle("-fx-background-color: #ffcdd2;");
            removeButton.setOnAction(e -> {
                questionsContainer.getChildren().remove(root);
                questionUIs.remove(this);
            });

            // Собираем
            root.getChildren().addAll(
                questionField,
                multipleCheck,
                new Label("Варианты ответов (первый — правильный, отметьте галочкой):"),
                optionsContainer,
                addOptionButton,
                removeButton
            );

            // Добавляем первый вариант
            addOption();
        }

        private void addOption() {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            CheckBox correctCheck = new CheckBox("✓");
            correctCheck.setTooltip(new Tooltip("Правильный ответ"));

            TextField optionField = new TextField();
            optionField.setPromptText("Вариант ответа...");
            optionField.setPrefWidth(300);

            Button removeOptBtn = new Button("✕");
            removeOptBtn.setStyle("-fx-background-color: #ffcdd2;");
            removeOptBtn.setOnAction(e -> {
                optionsContainer.getChildren().remove(row);
                optionFields.remove(optionField);
            });

            row.getChildren().addAll(correctCheck, optionField, removeOptBtn);
            optionsContainer.getChildren().add(row);
            optionFields.add(optionField);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> getQuestion() {
            String text = questionField.getText().trim();
            if (text.isEmpty()) {
                return null;
            }

            List<String> options = new ArrayList<>();
            List<Integer> correctOptions = new ArrayList<>();

            for (int i = 0; i < optionsContainer.getChildren().size(); i++) {
                HBox row = (HBox) optionsContainer.getChildren().get(i);
                CheckBox correctCheck = (CheckBox) row.getChildren().get(0);
                TextField field = (TextField) row.getChildren().get(1);
                String optionText = field.getText().trim();
                if (!optionText.isEmpty()) {
                    options.add(optionText);
                    if (correctCheck.isSelected()) {
                        correctOptions.add(i);
                    }
                }
            }

            if (options.isEmpty()) {
                return null;
            }

            // Если не отмечено ни одного правильного — считаем первый правильным
            if (correctOptions.isEmpty() && !options.isEmpty()) {
                correctOptions.add(0);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("text", text);
            result.put("options", options);
            result.put("correctOptions", correctOptions);
            result.put("multipleChoice", multipleCheck.isSelected());

            return result;
        }

        public VBox getRoot() {
            return root;
        }
    }
}