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

public class WorkstationController {

    private static final Logger log = LoggerFactory.getLogger(WorkstationController.class);
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML private TableView<Map<String, Object>> workstationTable;
    @FXML private TableColumn<Map<String, Object>, String> colDepartment;
    @FXML private TableColumn<Map<String, Object>, String> colTitle;
    @FXML private TableColumn<Map<String, Object>, String> colCreatedBy;
    @FXML private TableColumn<Map<String, Object>, String> colCreatedAt;
    @FXML private TableColumn<Map<String, Object>, String> colStatus;
    @FXML private TableColumn<Map<String, Object>, String> colEmployees;
    @FXML private TableColumn<Map<String, Object>, Void> colActions;

    private ObservableList<Map<String, Object>> workstationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadWorkstations();
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        colDepartment.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty((String) cell.getValue().get("department")));
        colTitle.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty((String) cell.getValue().get("title")));
        colCreatedBy.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty((String) cell.getValue().get("createdBy")));
        colCreatedAt.setCellValueFactory(cell -> {
            String date = (String) cell.getValue().get("createdAt");
            return new javafx.beans.property.SimpleStringProperty(
                date != null ? date.substring(0, 10) : ""
            );
        });
        colStatus.setCellValueFactory(cell -> {
            Boolean active = (Boolean) cell.getValue().get("isActive");
            return new javafx.beans.property.SimpleStringProperty(
                active != null && active ? "✅ Активно" : "❌ Неактивно"
            );
        });
        colEmployees.setCellValueFactory(cell -> {
            List<String> employees = (List<String>) cell.getValue().get("employeeIds");
            return new javafx.beans.property.SimpleStringProperty(
                employees != null ? String.valueOf(employees.size()) : "0"
            );
        });

        addActionButtons();
    }

    private void addActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button toggleBtn = new Button("🔄");

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                toggleBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 3;");
                editBtn.setPrefWidth(30);
                toggleBtn.setPrefWidth(30);

                editBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    openEditDialog(item);
                });

                toggleBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    toggleActive(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5, editBtn, toggleBtn);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadWorkstations() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен");
            return;
        }

        try {
            Request request = new Request.Builder()
                .url(ApiConfig.WORKSTATIONS)
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
                    workstationList.clear();
                    workstationList.addAll(list);
                    workstationTable.setItems(workstationList);
                } else {
                    showError("Ошибка загрузки: " + response.code());
                }
            }
        } catch (IOException e) {
            log.error("Error loading workstations", e);
            showError("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadWorkstations();
    }

    @FXML
    private void handleCreate() {
        openEditDialog(null);
    }

    private void openEditDialog(Map<String, Object> item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/workstation-dialog.fxml"));
            Parent root = loader.load();

            WorkstationDialogController controller = loader.getController();
            controller.setData(item);
            controller.setOnSaveCallback(this::loadWorkstations);

            Stage stage = new Stage();
            stage.setTitle(item == null ? "Создание рабочего места" : "Редактирование рабочего места");
            stage.setScene(new Scene(root, 450, 350));
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error opening dialog", e);
            showError("Ошибка: " + e.getMessage());
        }
    }

    private void toggleActive(Map<String, Object> item) {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showError("Не найден токен");
            return;
        }

        String id = (String) item.get("id");
        try {
            Request request = new Request.Builder()
                .url(ApiConfig.WORKSTATIONS + "/" + id + "/toggle")
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .header("Authorization", "Bearer " + token)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    loadWorkstations();
                } else {
                    showError("Ошибка: " + response.code());
                }
            }
        } catch (IOException e) {
            log.error("Error toggling workstation", e);
            showError("Ошибка: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}