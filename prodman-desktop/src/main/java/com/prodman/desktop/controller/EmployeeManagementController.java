package com.prodman.desktop.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.utils.TokenManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeManagementController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeManagementController.class);

    private static final String API_BASE_URL = "http://localhost:8888/work-calendar/api/v1/employees";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML private TableView<Map<String, Object>> employeeTable;
    @FXML private TableColumn<Map<String, Object>, String> colId;
    @FXML private TableColumn<Map<String, Object>, String> colFirstName;
    @FXML private TableColumn<Map<String, Object>, String> colLastName;
    @FXML private TableColumn<Map<String, Object>, String> colEmail;
    @FXML private TableColumn<Map<String, Object>, String> colPositions;
    @FXML private TableColumn<Map<String, Object>, String> colStatus;
    @FXML private TableColumn<Map<String, Object>, String> colDepartment;
    @FXML private TableColumn<Map<String, Object>, String> colPhone;
    @FXML private TableColumn<Map<String, Object>, Void> colActions;

    @FXML private TextField searchField;
    @FXML private Button refreshButton;

    @FXML private VBox formPanel;
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
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private ObservableList<Map<String, Object>> employeeList = FXCollections.observableArrayList();
    private Map<String, Object> selectedEmployee;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupTableSelection();
        loadEmployees();
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> {
            Map<String, Object> emp = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty((String) emp.get("id"));
        });

        colFirstName.setCellValueFactory(cellData -> {
            Map<String, Object> emp = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty((String) emp.get("firstName"));
        });

        colLastName.setCellValueFactory(cellData -> {
            Map<String, Object> emp = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty((String) emp.get("lastName"));
        });

        colEmail.setCellValueFactory(cellData -> {
            Map<String, Object> emp = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty((String) emp.get("email"));
        });

        colPositions.setCellValueFactory(cellData -> {
            Map<String, Object> emp = cellData.getValue();
            List<String> positions = (List<String>) emp.get("positions");
            return new javafx.beans.property.SimpleStringProperty(
                    positions != null ? String.join(", ", positions) : ""
            );
        });

        colStatus.setCellValueFactory(cellData -> {
            Map<String, Object> emp = cellData.getValue();
            Object status = emp.get("status");
            if (status instanceof String) {
                return new javafx.beans.property.SimpleStringProperty((String) status);
            }
            Map<String, Object> statusMap = (Map<String, Object>) status;
            return new javafx.beans.property.SimpleStringProperty(
                    statusMap != null ? (String) statusMap.get("displayName") : ""
            );
        });

        colDepartment.setCellValueFactory(cellData -> {
            Map<String, Object> emp = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty((String) emp.get("department"));
        });

        colPhone.setCellValueFactory(cellData -> {
            Map<String, Object> emp = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty((String) emp.get("phoneNumber"));
        });

        addActionButtonsToTable();
    }

    private void addActionButtonsToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✎");
            private final Button deleteButton = new Button("✕");

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 12;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 12;");
                editButton.setPrefWidth(30);
                deleteButton.setPrefWidth(30);

                editButton.setOnAction(event -> {
                    Map<String, Object> employee = getTableView().getItems().get(getIndex());
                    selectEmployee(employee);
                });

                deleteButton.setOnAction(event -> {
                    Map<String, Object> employee = getTableView().getItems().get(getIndex());
                    selectedEmployee = employee;
                    deleteEmployee();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5, editButton, deleteButton);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupComboBoxes() {
        statusCombo.setItems(FXCollections.observableArrayList(
                "AVAILABLE", "BUSY", "VACATION", "SICK_LEAVE", "UNAVAILABLE"
        ));
        statusCombo.getSelectionModel().select("AVAILABLE");

        preferredShiftCombo.setItems(FXCollections.observableArrayList(
                "MORNING", "EVENING", "NIGHT", ""
        ));
        preferredShiftCombo.getSelectionModel().select("");
    }

    private void setupTableSelection() {
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectEmployee(newVal);
            }
        });
    }

    private void loadEmployees() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен. Пожалуйста, войдите заново.");
            return;
        }

        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL)
                    .header("Authorization", "Bearer " + token)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    List<Map<String, Object>> employees = mapper.readValue(
                            json,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    employeeList.clear();
                    employeeList.addAll(employees);
                    employeeTable.setItems(employeeList);
                    clearForm();
                    log.info("Loaded {} employees", employees.size());
                } else if (response.code() == 401) {
                    showError("Сессия истекла. Пожалуйста, войдите заново.");
                } else {
                    showError("Ошибка загрузки: " + response.code());
                }
            }
        } catch (IOException e) {
            log.error("Error loading employees", e);
            showError("Ошибка загрузки работников: " + e.getMessage());
        }
    }

    @FXML
    public void refresh() {
        loadEmployees();
    }

    @FXML
    public void searchEmployee() {
        String email = searchField.getText().trim();
        if (email.isEmpty()) {
            loadEmployees();
            return;
        }

        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен. Пожалуйста, войдите заново.");
            return;
        }

        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "?email=" + email)
                    .header("Authorization", "Bearer " + token)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    List<Map<String, Object>> employees = mapper.readValue(
                            json,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    employeeList.clear();
                    if (!employees.isEmpty()) {
                        employeeList.addAll(employees);
                    } else {
                        showError("Работник с email '" + email + "' не найден");
                    }
                    employeeTable.setItems(employeeList);
                } else {
                    showError("Ошибка поиска: " + response.code());
                }
            }
        } catch (IOException e) {
            log.error("Error searching employee", e);
            showError("Ошибка поиска: " + e.getMessage());
        }
    }

    @FXML
    public void clearSearch() {
        searchField.clear();
        loadEmployees();
    }

    @SuppressWarnings("unchecked")
    private void selectEmployee(Map<String, Object> employee) {
        this.selectedEmployee = employee;
        this.isEditMode = true;

        firstNameField.setText((String) employee.get("firstName"));
        lastNameField.setText((String) employee.get("lastName"));
        emailField.setText((String) employee.get("email"));

        List<String> positions = (List<String>) employee.get("positions");
        if (positions != null) {
            positionsField.setText(String.join(", ", positions));
        }

        Object status = employee.get("status");
        if (status instanceof String) {
            statusCombo.setValue((String) status);
        } else {
            Map<String, Object> statusMap = (Map<String, Object>) status;
            statusCombo.setValue((String) statusMap.get("name"));
        }

        departmentField.setText((String) employee.get("department"));
        phoneField.setText((String) employee.get("phoneNumber"));

        Integer maxHours = (Integer) employee.get("maxHoursPerWeek");
        if (maxHours != null) {
            maxHoursField.setText(String.valueOf(maxHours));
        }

        String shift = (String) employee.get("preferredShift");
        preferredShiftCombo.setValue(shift != null ? shift : "");

        try {
            String vacationStart = (String) employee.get("vacationStart");
            if (vacationStart != null && !vacationStart.isEmpty()) {
                vacationStartPicker.setValue(LocalDate.parse(vacationStart));
            }
        } catch (Exception e) {
            vacationStartPicker.setValue(null);
        }

        try {
            String vacationEnd = (String) employee.get("vacationEnd");
            if (vacationEnd != null && !vacationEnd.isEmpty()) {
                vacationEndPicker.setValue(LocalDate.parse(vacationEnd));
            }
        } catch (Exception e) {
            vacationEndPicker.setValue(null);
        }

        saveButton.setText("ОБНОВИТЬ");
        deleteButton.setVisible(true);
        clearMessages();
    }

    @FXML
    public void saveEmployee() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен. Пожалуйста, войдите заново.");
            return;
        }

        try {
            if (!validateForm()) {
                return;
            }

            Map<String, Object> employeeData = buildEmployeeData();

            String url = isEditMode && selectedEmployee != null ?
                    API_BASE_URL + "/" + selectedEmployee.get("id") :
                    API_BASE_URL;

            String method = isEditMode && selectedEmployee != null ? "PUT" : "POST";

            Request request = new Request.Builder()
                    .url(url)
                    .method(method, RequestBody.create(
                            mapper.writeValueAsString(employeeData),
                            MediaType.parse("application/json")
                    ))
                    .header("Authorization", "Bearer " + token)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    showSuccess(isEditMode ? "Работник успешно обновлен!" : "Работник успешно создан!");
                    loadEmployees();
                    clearForm();
                } else if (response.code() == 401) {
                    showError("Сессия истекла. Пожалуйста, войдите заново.");
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    showError("Ошибка сохранения: " + response.code() + " - " + errorBody);
                }
            }
        } catch (Exception e) {
            log.error("Error saving employee", e);
            showError("Ошибка сохранения: " + e.getMessage());
        }
    }

    @FXML
    public void deleteEmployee() {
        if (selectedEmployee == null) {
            showError("Выберите работника для удаления");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление работника");
        alert.setContentText("Вы уверены, что хотите удалить работника " +
                selectedEmployee.get("firstName") + " " + selectedEmployee.get("lastName") + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String token = TokenManager.getAccessToken();
            if (token == null || token.isEmpty()) {
                showError("Не найден токен. Пожалуйста, войдите заново.");
                return;
            }

            try {
                Request request = new Request.Builder()
                        .url(API_BASE_URL + "/" + selectedEmployee.get("id"))
                        .header("Authorization", "Bearer " + token)
                        .delete()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        showSuccess("Работник успешно удален!");
                        loadEmployees();
                        clearForm();
                    } else {
                        showError("Ошибка удаления: " + response.code());
                    }
                }
            } catch (IOException e) {
                log.error("Error deleting employee", e);
                showError("Ошибка удаления: " + e.getMessage());
            }
        }
    }

    @FXML
    public void clearForm() {
        selectedEmployee = null;
        isEditMode = false;

        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        positionsField.clear();
        statusCombo.setValue("AVAILABLE");
        departmentField.clear();
        phoneField.clear();
        maxHoursField.clear();
        preferredShiftCombo.setValue("");
        vacationStartPicker.setValue(null);
        vacationEndPicker.setValue(null);
        sickLeaveStartPicker.setValue(null);
        sickLeaveEndPicker.setValue(null);

        saveButton.setText("СОЗДАТЬ");
        deleteButton.setVisible(false);
        clearMessages();
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
        clearMessages();

        if (firstNameField.getText().trim().isEmpty()) {
            showError("Имя обязательно для заполнения");
            firstNameField.requestFocus();
            return false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            showError("Фамилия обязательна для заполнения");
            lastNameField.requestFocus();
            return false;
        }

        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Введите корректный email");
            emailField.requestFocus();
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

    private void clearMessages() {
        errorLabel.setText("");
        successLabel.setText("");
    }
}


















//package com.prodman.desktop.controller;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.layout.VBox;
//import okhttp3.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Controller;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Controller
//public class EmployeeManagementController {
//
//    private static final Logger log = LoggerFactory.getLogger(EmployeeManagementController.class);
//
//    private static final String API_BASE_URL = "http://localhost:8082/api/employees";
//    private final OkHttpClient client = new OkHttpClient();
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @FXML private TableView<Map<String, Object>> employeeTable;
//    @FXML private TableColumn<Map<String, Object>, String> colId;
//    @FXML private TableColumn<Map<String, Object>, String> colFirstName;
//    @FXML private TableColumn<Map<String, Object>, String> colLastName;
//    @FXML private TableColumn<Map<String, Object>, String> colEmail;
//    @FXML private TableColumn<Map<String, Object>, String> colPositions;
//    @FXML private TableColumn<Map<String, Object>, String> colStatus;
//    @FXML private TableColumn<Map<String, Object>, String> colDepartment;
//    @FXML private TableColumn<Map<String, Object>, String> colPhone;
//    @FXML private TableColumn<Map<String, Object>, Void> colActions;
//
//    @FXML private TextField searchField;
//    @FXML private Button refreshButton;
//
//    @FXML private VBox formPanel;
//    @FXML private TextField firstNameField;
//    @FXML private TextField lastNameField;
//    @FXML private TextField emailField;
//    @FXML private TextField positionsField;
//    @FXML private ComboBox<String> statusCombo;
//    @FXML private TextField departmentField;
//    @FXML private TextField phoneField;
//    @FXML private TextField maxHoursField;
//    @FXML private ComboBox<String> preferredShiftCombo;
//    @FXML private DatePicker vacationStartPicker;
//    @FXML private DatePicker vacationEndPicker;
//    @FXML private DatePicker sickLeaveStartPicker;
//    @FXML private DatePicker sickLeaveEndPicker;
//    @FXML private Button saveButton;
//    @FXML private Button deleteButton;
//    @FXML private Label errorLabel;
//    @FXML private Label successLabel;
//
//    private ObservableList<Map<String, Object>> employeeList = FXCollections.observableArrayList();
//    private Map<String, Object> selectedEmployee;
//    private boolean isEditMode = false;
//
//    @FXML
//    public void initialize() {
//        setupTableColumns();
//        setupComboBoxes();
//        setupTableSelection();
//        loadEmployees();
//    }
//
//    @SuppressWarnings("unchecked")
//    private void setupTableColumns() {
//        colId.setCellValueFactory(cellData -> {
//            Map<String, Object> emp = cellData.getValue();
//            return new javafx.beans.property.SimpleStringProperty((String) emp.get("id"));
//        });
//
//        colFirstName.setCellValueFactory(cellData -> {
//            Map<String, Object> emp = cellData.getValue();
//            return new javafx.beans.property.SimpleStringProperty((String) emp.get("firstName"));
//        });
//
//        colLastName.setCellValueFactory(cellData -> {
//            Map<String, Object> emp = cellData.getValue();
//            return new javafx.beans.property.SimpleStringProperty((String) emp.get("lastName"));
//        });
//
//        colEmail.setCellValueFactory(cellData -> {
//            Map<String, Object> emp = cellData.getValue();
//            return new javafx.beans.property.SimpleStringProperty((String) emp.get("email"));
//        });
//
//        colPositions.setCellValueFactory(cellData -> {
//            Map<String, Object> emp = cellData.getValue();
//            List<String> positions = (List<String>) emp.get("positions");
//            return new javafx.beans.property.SimpleStringProperty(
//                    positions != null ? String.join(", ", positions) : ""
//            );
//        });
//
//        colStatus.setCellValueFactory(cellData -> {
//            Map<String, Object> emp = cellData.getValue();
//            Object status = emp.get("status");
//            if (status instanceof String) {
//                return new javafx.beans.property.SimpleStringProperty((String) status);
//            }
//            Map<String, Object> statusMap = (Map<String, Object>) status;
//            return new javafx.beans.property.SimpleStringProperty(
//                    statusMap != null ? (String) statusMap.get("displayName") : ""
//            );
//        });
//
//        colDepartment.setCellValueFactory(cellData -> {
//            Map<String, Object> emp = cellData.getValue();
//            return new javafx.beans.property.SimpleStringProperty((String) emp.get("department"));
//        });
//
//        colPhone.setCellValueFactory(cellData -> {
//            Map<String, Object> emp = cellData.getValue();
//            return new javafx.beans.property.SimpleStringProperty((String) emp.get("phoneNumber"));
//        });
//
//        addActionButtonsToTable();
//    }
//
//    private void addActionButtonsToTable() {
//        colActions.setCellFactory(param -> new TableCell<>() {
//            private final Button editButton = new Button("✎");
//            private final Button deleteButton = new Button("✕");
//
//            {
//                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 12;");
//                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 12;");
//                editButton.setPrefWidth(30);
//                deleteButton.setPrefWidth(30);
//
//                editButton.setOnAction(event -> {
//                    Map<String, Object> employee = getTableView().getItems().get(getIndex());
//                    selectEmployee(employee);
//                });
//
//                deleteButton.setOnAction(event -> {
//                    Map<String, Object> employee = getTableView().getItems().get(getIndex());
//                    selectedEmployee = employee;
//                    deleteEmployee();
//                });
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty) {
//                    setGraphic(null);
//                } else {
//                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5, editButton, deleteButton);
//                    box.setAlignment(javafx.geometry.Pos.CENTER);
//                    setGraphic(box);
//                }
//            }
//        });
//    }
//
//    private void setupComboBoxes() {
//        statusCombo.setItems(FXCollections.observableArrayList(
//                "AVAILABLE", "BUSY", "VACATION", "SICK_LEAVE", "UNAVAILABLE"
//        ));
//        statusCombo.getSelectionModel().select("AVAILABLE");
//
//        preferredShiftCombo.setItems(FXCollections.observableArrayList(
//                "MORNING", "EVENING", "NIGHT", ""
//        ));
//        preferredShiftCombo.getSelectionModel().select("");
//    }
//
//    private void setupTableSelection() {
//        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal != null) {
//                selectEmployee(newVal);
//            }
//        });
//    }
//
//    private void loadEmployees() {
//        try {
//            Request request = new Request.Builder()
//                    .url(API_BASE_URL)
//                    .get()
//                    .build();
//
//            try (Response response = client.newCall(request).execute()) {
//                if (response.isSuccessful()) {
//                    String json = response.body().string();
//                    List<Map<String, Object>> employees = mapper.readValue(
//                            json,
//                            new TypeReference<List<Map<String, Object>>>() {}
//                    );
//                    employeeList.clear();
//                    employeeList.addAll(employees);
//                    employeeTable.setItems(employeeList);
//                    clearForm();
//                    log.info("Loaded {} employees", employees.size());
//                } else {
//                    showError("Ошибка загрузки: " + response.code());
//                }
//            }
//        } catch (IOException e) {
//            log.error("Error loading employees", e);
//            showError("Ошибка загрузки работников: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    public void refresh() {
//        loadEmployees();
//    }
//
//    @FXML
//    public void searchEmployee() {
//        String email = searchField.getText().trim();
//        if (email.isEmpty()) {
//            loadEmployees();
//            return;
//        }
//
//        try {
//            Request request = new Request.Builder()
//                    .url(API_BASE_URL + "?email=" + email)
//                    .get()
//                    .build();
//
//            try (Response response = client.newCall(request).execute()) {
//                if (response.isSuccessful()) {
//                    String json = response.body().string();
//                    List<Map<String, Object>> employees = mapper.readValue(
//                            json,
//                            new TypeReference<List<Map<String, Object>>>() {}
//                    );
//                    employeeList.clear();
//                    if (!employees.isEmpty()) {
//                        employeeList.addAll(employees);
//                    } else {
//                        showError("Работник с email '" + email + "' не найден");
//                    }
//                    employeeTable.setItems(employeeList);
//                } else {
//                    showError("Ошибка поиска: " + response.code());
//                }
//            }
//        } catch (IOException e) {
//            log.error("Error searching employee", e);
//            showError("Ошибка поиска: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    public void clearSearch() {
//        searchField.clear();
//        loadEmployees();
//    }
//
//    @SuppressWarnings("unchecked")
//    private void selectEmployee(Map<String, Object> employee) {
//        this.selectedEmployee = employee;
//        this.isEditMode = true;
//
//        firstNameField.setText((String) employee.get("firstName"));
//        lastNameField.setText((String) employee.get("lastName"));
//        emailField.setText((String) employee.get("email"));
//
//        List<String> positions = (List<String>) employee.get("positions");
//        if (positions != null) {
//            positionsField.setText(String.join(", ", positions));
//        }
//
//        Object status = employee.get("status");
//        if (status instanceof String) {
//            statusCombo.setValue((String) status);
//        } else {
//            Map<String, Object> statusMap = (Map<String, Object>) status;
//            statusCombo.setValue((String) statusMap.get("name"));
//        }
//
//        departmentField.setText((String) employee.get("department"));
//        phoneField.setText((String) employee.get("phoneNumber"));
//
//        Integer maxHours = (Integer) employee.get("maxHoursPerWeek");
//        if (maxHours != null) {
//            maxHoursField.setText(String.valueOf(maxHours));
//        }
//
//        String shift = (String) employee.get("preferredShift");
//        preferredShiftCombo.setValue(shift != null ? shift : "");
//
//        // Date parsing simplified
//        try {
//            String vacationStart = (String) employee.get("vacationStart");
//            if (vacationStart != null && !vacationStart.isEmpty()) {
//                vacationStartPicker.setValue(LocalDate.parse(vacationStart));
//            }
//        } catch (Exception e) {
//            vacationStartPicker.setValue(null);
//        }
//
//        try {
//            String vacationEnd = (String) employee.get("vacationEnd");
//            if (vacationEnd != null && !vacationEnd.isEmpty()) {
//                vacationEndPicker.setValue(LocalDate.parse(vacationEnd));
//            }
//        } catch (Exception e) {
//            vacationEndPicker.setValue(null);
//        }
//
//        saveButton.setText("ОБНОВИТЬ");
//        deleteButton.setVisible(true);
//        clearMessages();
//    }
//
//    @FXML
//    public void saveEmployee() {
//        try {
//            if (!validateForm()) {
//                return;
//            }
//
//            Map<String, Object> employeeData = buildEmployeeData();
//
//            String url = isEditMode && selectedEmployee != null ?
//                    API_BASE_URL + "/" + selectedEmployee.get("id") :
//                    API_BASE_URL;
//
//            String method = isEditMode && selectedEmployee != null ? "PUT" : "POST";
//
//            Request request = new Request.Builder()
//                    .url(url)
//                    .method(method, RequestBody.create(
//                            mapper.writeValueAsString(employeeData),
//                            MediaType.parse("application/json")
//                    ))
//                    .build();
//
//            try (Response response = client.newCall(request).execute()) {
//                if (response.isSuccessful()) {
//                    showSuccess(isEditMode ? "Работник успешно обновлен!" : "Работник успешно создан!");
//                    loadEmployees();
//                    clearForm();
//                } else {
//                    String errorBody = response.body() != null ? response.body().string() : "";
//                    showError("Ошибка сохранения: " + response.code() + " - " + errorBody);
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error saving employee", e);
//            showError("Ошибка сохранения: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    public void deleteEmployee() {
//        if (selectedEmployee == null) {
//            showError("Выберите работника для удаления");
//            return;
//        }
//
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Подтверждение удаления");
//        alert.setHeaderText("Удаление работника");
//        alert.setContentText("Вы уверены, что хотите удалить работника " +
//                selectedEmployee.get("firstName") + " " + selectedEmployee.get("lastName") + "?");
//
//        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
//            try {
//                Request request = new Request.Builder()
//                        .url(API_BASE_URL + "/" + selectedEmployee.get("id"))
//                        .delete()
//                        .build();
//
//                try (Response response = client.newCall(request).execute()) {
//                    if (response.isSuccessful()) {
//                        showSuccess("Работник успешно удален!");
//                        loadEmployees();
//                        clearForm();
//                    } else {
//                        showError("Ошибка удаления: " + response.code());
//                    }
//                }
//            } catch (IOException e) {
//                log.error("Error deleting employee", e);
//                showError("Ошибка удаления: " + e.getMessage());
//            }
//        }
//    }
//
//    @FXML
//    public void clearForm() {
//        selectedEmployee = null;
//        isEditMode = false;
//
//        firstNameField.clear();
//        lastNameField.clear();
//        emailField.clear();
//        positionsField.clear();
//        statusCombo.setValue("AVAILABLE");
//        departmentField.clear();
//        phoneField.clear();
//        maxHoursField.clear();
//        preferredShiftCombo.setValue("");
//        vacationStartPicker.setValue(null);
//        vacationEndPicker.setValue(null);
//        sickLeaveStartPicker.setValue(null);
//        sickLeaveEndPicker.setValue(null);
//
//        saveButton.setText("СОЗДАТЬ");
//        deleteButton.setVisible(false);
//        clearMessages();
//    }
//
//    private Map<String, Object> buildEmployeeData() {
//        Map<String, Object> data = new HashMap<>();
//        data.put("firstName", firstNameField.getText().trim());
//        data.put("lastName", lastNameField.getText().trim());
//        data.put("email", emailField.getText().trim());
//
//        String positionsText = positionsField.getText().trim();
//        if (!positionsText.isEmpty()) {
//            List<String> positions = Arrays.stream(positionsText.split(","))
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .collect(Collectors.toList());
//            data.put("positions", positions);
//        }
//
//        data.put("status", statusCombo.getValue());
//        data.put("department", departmentField.getText().trim());
//        data.put("phoneNumber", phoneField.getText().trim());
//
//        String maxHours = maxHoursField.getText().trim();
//        if (!maxHours.isEmpty()) {
//            data.put("maxHoursPerWeek", Integer.parseInt(maxHours));
//        }
//
//        String shift = preferredShiftCombo.getValue();
//        data.put("preferredShift", shift != null && !shift.isEmpty() ? shift : null);
//
//        data.put("vacationStart", vacationStartPicker.getValue() != null ?
//                vacationStartPicker.getValue().toString() : null);
//        data.put("vacationEnd", vacationEndPicker.getValue() != null ?
//                vacationEndPicker.getValue().toString() : null);
//        data.put("sickLeaveStart", sickLeaveStartPicker.getValue() != null ?
//                sickLeaveStartPicker.getValue().toString() : null);
//        data.put("sickLeaveEnd", sickLeaveEndPicker.getValue() != null ?
//                sickLeaveEndPicker.getValue().toString() : null);
//
//        return data;
//    }
//
//    private boolean validateForm() {
//        clearMessages();
//
//        if (firstNameField.getText().trim().isEmpty()) {
//            showError("Имя обязательно для заполнения");
//            firstNameField.requestFocus();
//            return false;
//        }
//
//        if (lastNameField.getText().trim().isEmpty()) {
//            showError("Фамилия обязательна для заполнения");
//            lastNameField.requestFocus();
//            return false;
//        }
//
//        String email = emailField.getText().trim();
//        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
//            showError("Введите корректный email");
//            emailField.requestFocus();
//            return false;
//        }
//
//        return true;
//    }
//
//    private void showError(String message) {
//        errorLabel.setText("❌ " + message);
//        successLabel.setText("");
//    }
//
//    private void showSuccess(String message) {
//        successLabel.setText("✅ " + message);
//        errorLabel.setText("");
//    }
//
//    private void clearMessages() {
//        errorLabel.setText("");
//        successLabel.setText("");
//    }
//}