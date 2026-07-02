package com.prodman.desktop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.utils.TokenManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeEditController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEditController.class);
    private static final String API_BASE_URL = "http://localhost:8888/work-calendar/api/v1/employees";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private Map<String, Object> employeeData;
    private Runnable onSaveCallback;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField positionsField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField departmentField;
    @FXML private TextField phoneField;
    @FXML private TextField maxHoursField;
    @FXML private ComboBox<String> preferredShiftCombo;
    @FXML private DatePicker vacationStartPicker;
    @FXML private DatePicker vacationEndPicker;
    @FXML private DatePicker sickLeaveStartPicker;
    @FXML private DatePicker sickLeaveEndPicker;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button saveButton;

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList(
            "AVAILABLE", "BUSY", "VACATION", "SICK_LEAVE", "UNAVAILABLE"
        ));
        preferredShiftCombo.setItems(FXCollections.observableArrayList(
            "MORNING", "EVENING", "NIGHT", ""
        ));
    }

    public void setEmployeeData(Map<String, Object> employee) {
        this.employeeData = employee;
        populateForm();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @SuppressWarnings("unchecked")
    private void populateForm() {
        if (employeeData == null) return;

        firstNameField.setText((String) employeeData.get("firstName"));
        lastNameField.setText((String) employeeData.get("lastName"));
        emailField.setText((String) employeeData.get("email"));

        Object positions = employeeData.get("positions");
        if (positions instanceof List) {
            positionsField.setText(String.join(", ", (List<String>) positions));
        }

        Object status = employeeData.get("status");
        if (status instanceof String) {
            statusCombo.setValue((String) status);
        } else if (status instanceof Map) {
            statusCombo.setValue((String) ((Map) status).get("name"));
        }

        departmentField.setText((String) employeeData.get("department"));
        phoneField.setText((String) employeeData.get("phoneNumber"));

        Object maxHours = employeeData.get("maxHoursPerWeek");
        if (maxHours != null) {
            maxHoursField.setText(String.valueOf(maxHours));
        }

        String shift = (String) employeeData.get("preferredShift");
        preferredShiftCombo.setValue(shift != null ? shift : "");

        try {
            String vacationStart = (String) employeeData.get("vacationStart");
            if (vacationStart != null && !vacationStart.isEmpty()) {
                vacationStartPicker.setValue(LocalDate.parse(vacationStart));
            }
        } catch (Exception e) { /* ignore */ }

        try {
            String vacationEnd = (String) employeeData.get("vacationEnd");
            if (vacationEnd != null && !vacationEnd.isEmpty()) {
                vacationEndPicker.setValue(LocalDate.parse(vacationEnd));
            }
        } catch (Exception e) { /* ignore */ }
    }

    @FXML
    private void handleSave() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен. Пожалуйста, войдите заново.");
            return;
        }

        try {
            if (!validateForm()) return;

            Map<String, Object> data = buildEmployeeData();
            String id = (String) employeeData.get("id");

            Request request = new Request.Builder()
                .url(API_BASE_URL + "/" + id)
                .put(RequestBody.create(
                    mapper.writeValueAsString(data),
                    MediaType.parse("application/json")
                ))
                .header("Authorization", "Bearer " + token)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    showSuccess("Работник успешно обновлен!");
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    closeWindow();
                } else {
                    showError("Ошибка обновления: " + response.code());
                }
            }
        } catch (Exception e) {
            log.error("Error updating employee", e);
            showError("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private Map<String, Object> buildEmployeeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", firstNameField.getText().trim());
        data.put("lastName", lastNameField.getText().trim());
        data.put("email", emailField.getText().trim());

        String positionsText = positionsField.getText().trim();
        if (!positionsText.isEmpty()) {
            List<String> positions = Arrays.stream(positionsText.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            data.put("positions", positions);
        }

        data.put("status", statusCombo.getValue());
        data.put("department", departmentField.getText().trim());
        data.put("phoneNumber", phoneField.getText().trim());

        String maxHours = maxHoursField.getText().trim();
        if (!maxHours.isEmpty()) {
            data.put("maxHoursPerWeek", Integer.parseInt(maxHours));
        }

        String shift = preferredShiftCombo.getValue();
        data.put("preferredShift", shift != null && !shift.isEmpty() ? shift : null);

        data.put("vacationStart", vacationStartPicker.getValue() != null ?
            vacationStartPicker.getValue().toString() : null);
        data.put("vacationEnd", vacationEndPicker.getValue() != null ?
            vacationEndPicker.getValue().toString() : null);
        data.put("sickLeaveStart", sickLeaveStartPicker.getValue() != null ?
            sickLeaveStartPicker.getValue().toString() : null);
        data.put("sickLeaveEnd", sickLeaveEndPicker.getValue() != null ?
            sickLeaveEndPicker.getValue().toString() : null);

        return data;
    }

    private boolean validateForm() {
        if (firstNameField.getText().trim().isEmpty()) {
            showError("Имя обязательно");
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Фамилия обязательна");
            return false;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Введите корректный email");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        successLabel.setText("");
    }

    private void showSuccess(String message) {
        successLabel.setText("✅ " + message);
        errorLabel.setText("");
    }
}