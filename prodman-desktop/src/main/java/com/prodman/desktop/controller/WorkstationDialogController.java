package com.prodman.desktop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.config.ApiConfig;
import com.prodman.desktop.utils.TokenManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class WorkstationDialogController {

    private static final Logger log = LoggerFactory.getLogger(WorkstationDialogController.class);
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML private TextField departmentField;
    @FXML private TextField titleField;
    @FXML private ListView<String> employeesList;
    @FXML private Label messageLabel;

    private Map<String, Object> editData;
    private Runnable onSaveCallback;

    public void setData(Map<String, Object> data) {
        this.editData = data;
        if (data != null) {
            departmentField.setText((String) data.get("department"));
            titleField.setText((String) data.get("title"));
            
            List<String> employeeIds = (List<String>) data.get("employeeIds");
            if (employeeIds != null) {
                employeesList.setItems(FXCollections.observableArrayList(employeeIds));
            }
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSave() {
        String department = departmentField.getText().trim();
        String title = titleField.getText().trim();

        if (department.isEmpty() || title.isEmpty()) {
            showMessage("Заполните все поля", true);
            return;
        }

        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showMessage("Не найден токен", true);
            return;
        }

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("department", department);
            data.put("title", title);
            data.put("createdBy", TokenManager.getUsername());

            String url = ApiConfig.WORKSTATIONS;
            String method = "POST";

            if (editData != null) {
                url = ApiConfig.WORKSTATIONS + "/" + editData.get("id");
                method = "PUT";
            }

            RequestBody body = RequestBody.create(
                mapper.writeValueAsString(data),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    closeWindow();
                } else {
                    showMessage("Ошибка сохранения: " + response.code(), true);
                }
            }
        } catch (Exception e) {
            log.error("Error saving workstation", e);
            showMessage("Ошибка: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) departmentField.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
    }
}