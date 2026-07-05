package com.prodman.desktop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.utils.TokenManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class UserManagementController {

    @FXML private TableView<UserRow> userTable;
    @FXML private TableColumn<UserRow, String> colId;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colEmail;
    @FXML private TableColumn<UserRow, String> colRole;
    @FXML private TableColumn<UserRow, Boolean> colEnabled;
    @FXML private TableColumn<UserRow, String> colCreatedAt;
    @FXML private TableColumn<UserRow, Void> colActions;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox enabledCheck;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button saveButton;
    @FXML private Button refreshButton;
    @FXML private VBox formPanel;
    @FXML private TextField searchEmailField;

    private final ObservableList<UserRow> allUsers = FXCollections.observableArrayList();
    private UserRow selectedUser = null;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String API_BASE_URL = "http://localhost:8888/user-service/api/v1";

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        colUsername.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        colEmail.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        colRole.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
        colEnabled.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isEnabled()));
        colCreatedAt.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedAt()));

        addActionButtonsToTable();

        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "OPERATOR", "ANALYST", "VISITOR"));
        roleCombo.setValue("VISITOR");

        searchEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterUsersByEmail(newVal);
        });

        loadUsers();

        saveButton.setOnAction(e -> saveUser());
        refreshButton.setOnAction(e -> loadUsers());

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) selectUser(selected);
        });
    }

    private void addActionButtonsToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10;");

                editBtn.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    selectUser(user);
                });

                deleteBtn.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void filterUsersByEmail(String filterText) {
        if (filterText == null || filterText.trim().isEmpty()) {
            userTable.setItems(allUsers);
            return;
        }

        String lowerFilter = filterText.toLowerCase().trim();
        ObservableList<UserRow> filtered = allUsers.filtered(user ->
                user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerFilter)
        );
        userTable.setItems(filtered);
    }

    private void loadUsers() {
        allUsers.clear();
        String token = TokenManager.getAccessToken();

        System.out.println("DEBUG: Token = " + token);

        if (token == null || token.isEmpty()) {
            javafx.application.Platform.runLater(() -> {
                showError("No token found. Please login again.");
            });
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/users"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    System.out.println("DEBUG: Response status = " + response.statusCode());
                    System.out.println("DEBUG: Response body = " + response.body());

                    if (response.statusCode() == 200) {
                        try {
                            UserRow[] users = mapper.readValue(response.body(), UserRow[].class);
                            javafx.application.Platform.runLater(() -> {
                                allUsers.addAll(Arrays.asList(users));
                                userTable.setItems(allUsers);
                                clearForm();
                                errorLabel.setText("");
                            });
                        } catch (Exception e) {
                            showError("Parse error: " + e.getMessage());
                        }
                    } else if (response.statusCode() == 401) {
                        showError("Session expired. Please login again.");
                    } else {
                        showError("Failed to load users: " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    showError("Network error: " + e.getMessage());
                    return null;
                });
    }

    @FXML
    private void searchUserByEmail() {
        String email = searchEmailField.getText().trim();
        if (email.isEmpty()) {
            loadUsers();
            return;
        }

        String lowerFilter = email.toLowerCase();
        ObservableList<UserRow> filtered = allUsers.filtered(user ->
                user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerFilter)
        );
        userTable.setItems(filtered);
    }

    private void saveUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();
        boolean enabled = enabledCheck.isSelected();

        if (username.isEmpty() || email.isEmpty()) {
            showError("Username and email are required");
            return;
        }

        String token = TokenManager.getAccessToken();

        if (selectedUser == null) {
            String json = String.format(
                    "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"enabled\":%b}",
                    username, email, password.isEmpty() ? "default123" : password, role, enabled
            );

            System.out.println("DEBUG: Sending JSON = " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        System.out.println("DEBUG: Response status = " + response.statusCode());
                        System.out.println("DEBUG: Response body = " + response.body());
                        javafx.application.Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                showSuccess("User created successfully");
                                loadUsers();
                            } else {
                                showError("Failed to create user: " + response.statusCode());
                            }
                        });
                    })
                    .exceptionally(e -> {
                        showError("Network error: " + e.getMessage());
                        return null;
                    });
        } else {
            String json = String.format("{\"role\":\"%s\",\"enabled\":%b}", role, enabled);

            System.out.println("DEBUG: Update JSON = " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/users/" + selectedUser.getId()))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        System.out.println("DEBUG: Update response status = " + response.statusCode());
                        System.out.println("DEBUG: Update response body = " + response.body());
                        javafx.application.Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                showSuccess("User updated successfully");
                                loadUsers();
                            } else {
                                showError("Failed to update user: " + response.statusCode());
                            }
                        });
                    })
                    .exceptionally(e -> {
                        showError("Network error: " + e.getMessage());
                        return null;
                    });
        }
    }

    private void deleteUser(UserRow user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Delete user: " + user.getUsername());
        confirm.setContentText("Are you sure?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String token = TokenManager.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/users/" + user.getId()))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        javafx.application.Platform.runLater(() -> {
                            if (response.statusCode() == 204) {
                                showSuccess("User deleted successfully");
                                loadUsers();
                            } else {
                                showError("Failed to delete user: " + response.statusCode());
                            }
                        });
                    })
                    .exceptionally(e -> {
                        showError("Network error: " + e.getMessage());
                        return null;
                    });
        }
    }

    private void selectUser(UserRow user) {
        selectedUser = user;
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        roleCombo.setValue(user.getRole());
        enabledCheck.setSelected(user.isEnabled());
        passwordField.clear();
        passwordField.setPromptText("Leave empty to keep current password");
        saveButton.setText("UPDATE");
        errorLabel.setText("");
        successLabel.setText("");
    }

    public void clearForm() {
        selectedUser = null;
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        roleCombo.setValue("VISITOR");
        enabledCheck.setSelected(true);
        saveButton.setText("CREATE");
        errorLabel.setText("");
        successLabel.setText("");
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            errorLabel.setText(message);
            successLabel.setText("");
        });
    }

    private void showSuccess(String message) {
        javafx.application.Platform.runLater(() -> {
            successLabel.setText(message);
            errorLabel.setText("");
        });
    }

    public static class UserRow {
        private String id;
        private String username;
        private String email;
        private String role;
        private boolean enabled;
        private String createdAt;

        public UserRow() {}

        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public boolean isEnabled() { return enabled; }
        public String getCreatedAt() { return createdAt; }

        public void setId(String id) { this.id = id; }
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
        public void setRole(String role) { this.role = role; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}













//package com.prodman.desktop.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.prodman.desktop.utils.TokenManager;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.layout.VBox;
//import javafx.scene.layout.HBox;
//import org.springframework.beans.factory.annotation.Value;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.Arrays;
//
//public class UserManagementController {
//
//    @FXML private TableView<UserRow> userTable;
//    @FXML private TableColumn<UserRow, String> colId;
//    @FXML private TableColumn<UserRow, String> colUsername;
//    @FXML private TableColumn<UserRow, String> colEmail;
//    @FXML private TableColumn<UserRow, String> colRole;
//    @FXML private TableColumn<UserRow, Boolean> colEnabled;
//    @FXML private TableColumn<UserRow, String> colCreatedAt;
//    @FXML private TableColumn<UserRow, Void> colActions;
//
//    @FXML private TextField usernameField;
//    @FXML private TextField emailField;
//    @FXML private PasswordField passwordField;
//    @FXML private ComboBox<String> roleCombo;
//    @FXML private CheckBox enabledCheck;
//    @FXML private Label errorLabel;
//    @FXML private Label successLabel;
//    @FXML private Button saveButton;
//    @FXML private Button refreshButton;
//    @FXML private VBox formPanel;
//    @FXML private TextField searchEmailField;
//
//    private final ObservableList<UserRow> allUsers = FXCollections.observableArrayList();
//    private UserRow selectedUser = null;
//    private final HttpClient client = HttpClient.newHttpClient();
//    private final ObjectMapper mapper = new ObjectMapper();
//    private final String API_URL = System.getenv().getOrDefault("API_URL", "http://localhost:8081/api/v1");
//
//    @FXML
//    public void initialize() {
//        // Настройка колонок
//        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
//        colUsername.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
//        colEmail.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
//        colRole.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
//        colEnabled.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isEnabled()));
//        colCreatedAt.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedAt()));
//
//        addActionButtonsToTable();
//
//        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "OPERATOR", "ANALYST", "VISITOR"));
//        roleCombo.setValue("VISITOR");
//
//        // Добавляем слушатель на поле поиска (фильтрация по мере ввода)
//        searchEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
//            filterUsersByEmail(newVal);
//        });
//
//        loadUsers();
//
//        saveButton.setOnAction(e -> saveUser());
//        refreshButton.setOnAction(e -> loadUsers());
//
//        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
//            if (selected != null) selectUser(selected);
//        });
//    }
//
//    private void addActionButtonsToTable() {
//        colActions.setCellFactory(param -> new TableCell<>() {
//            private final Button editBtn = new Button("✏️");
//            private final Button deleteBtn = new Button("🗑️");
//            private final HBox buttons = new HBox(5, editBtn, deleteBtn);
//
//            {
//                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10;");
//                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10;");
//
//                editBtn.setOnAction(e -> {
//                    UserRow user = getTableView().getItems().get(getIndex());
//                    selectUser(user);
//                });
//
//                deleteBtn.setOnAction(e -> {
//                    UserRow user = getTableView().getItems().get(getIndex());
//                    deleteUser(user);
//                });
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                setGraphic(empty ? null : buttons);
//            }
//        });
//    }
//
//    private void filterUsersByEmail(String filterText) {
//        if (filterText == null || filterText.trim().isEmpty()) {
//            userTable.setItems(allUsers);
//            return;
//        }
//
//        String lowerFilter = filterText.toLowerCase().trim();
//        ObservableList<UserRow> filtered = allUsers.filtered(user ->
//                user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerFilter)
//        );
//        userTable.setItems(filtered);
//    }
//
//    private void loadUsers() {
//        allUsers.clear();
//        String token = TokenManager.getAccessToken();
//
//        System.out.println("DEBUG: Token = " + token);
//
//        if (token == null || token.isEmpty()) {
//            javafx.application.Platform.runLater(() -> {
//                showError("No token found. Please login again.");
//            });
//            return;
//        }
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(API_URL + "/users"))
//                .header("Authorization", "Bearer " + token)
//                .GET()
//                .build();
//
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenAccept(response -> {
//                    System.out.println("DEBUG: Response status = " + response.statusCode());
//                    System.out.println("DEBUG: Response body = " + response.body());
//
//                    if (response.statusCode() == 200) {
//                        try {
//                            UserRow[] users = mapper.readValue(response.body(), UserRow[].class);
//                            javafx.application.Platform.runLater(() -> {
//                                allUsers.addAll(Arrays.asList(users));
//                                userTable.setItems(allUsers);
//                                clearForm();
//                                errorLabel.setText("");
//                            });
//                        } catch (Exception e) {
//                            showError("Parse error: " + e.getMessage());
//                        }
//                    } else {
//                        showError("Failed to load users: " + response.statusCode());
//                    }
//                })
//                .exceptionally(e -> {
//                    showError("Network error: " + e.getMessage());
//                    return null;
//                });
//    }
//    @FXML
//    private void searchUserByEmail() {
//        String email = searchEmailField.getText().trim();
//        if (email.isEmpty()) {
//            loadUsers();
//            return;
//        }
//
//        String lowerFilter = email.toLowerCase();
//        ObservableList<UserRow> filtered = allUsers.filtered(user ->
//                user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerFilter)
//        );
//        userTable.setItems(filtered);
//    }
//
//    private void saveUser() {
//        String username = usernameField.getText().trim();
//        String email = emailField.getText().trim();
//        String password = passwordField.getText();
//        String role = roleCombo.getValue();
//        boolean enabled = enabledCheck.isSelected();
//
//        if (username.isEmpty() || email.isEmpty()) {
//            showError("Username and email are required");
//            return;
//        }
//
//        String token = TokenManager.getAccessToken();
//
//        if (selectedUser == null) {
//            String json = String.format(
//                    "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"enabled\":%b}",
//                    username, email, password.isEmpty() ? "default123" : password, role, enabled
//            );
//
//            System.out.println("DEBUG: Sending JSON = " + json);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(API_URL + "/auth/register"))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(json))
//                    .build();
//
//            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                    .thenAccept(response -> {
//                        System.out.println("DEBUG: Response status = " + response.statusCode());
//                        System.out.println("DEBUG: Response body = " + response.body());
//                        javafx.application.Platform.runLater(() -> {
//                            if (response.statusCode() == 200) {
//                                showSuccess("User created successfully");
//                                loadUsers();
//                            } else {
//                                showError("Failed to create user: " + response.statusCode());
//                            }
//                        });
//                    })
//                    .exceptionally(e -> {
//                        showError("Network error: " + e.getMessage());
//                        return null;
//                    });
//        } else {
//            String json = String.format("{\"role\":\"%s\",\"enabled\":%b}", role, enabled);
//
//            System.out.println("DEBUG: Update JSON = " + json);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(API_URL + "/users/" + selectedUser.getId()))
//                    .header("Authorization", "Bearer " + token)
//                    .header("Content-Type", "application/json")
//                    .PUT(HttpRequest.BodyPublishers.ofString(json))
//                    .build();
//
//            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                    .thenAccept(response -> {
//                        System.out.println("DEBUG: Update response status = " + response.statusCode());
//                        System.out.println("DEBUG: Update response body = " + response.body());
//                        javafx.application.Platform.runLater(() -> {
//                            if (response.statusCode() == 200) {
//                                showSuccess("User updated successfully");
//                                loadUsers();
//                            } else {
//                                showError("Failed to update user: " + response.statusCode());
//                            }
//                        });
//                    })
//                    .exceptionally(e -> {
//                        showError("Network error: " + e.getMessage());
//                        return null;
//                    });
//        }
//    }
//
//    private void deleteUser(UserRow user) {
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Delete User");
//        confirm.setHeaderText("Delete user: " + user.getUsername());
//        confirm.setContentText("Are you sure?");
//
//        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
//            String token = TokenManager.getAccessToken();
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(API_URL + "/users/" + user.getId()))
//                    .header("Authorization", "Bearer " + token)
//                    .DELETE()
//                    .build();
//
//            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                    .thenAccept(response -> {
//                        javafx.application.Platform.runLater(() -> {
//                            if (response.statusCode() == 204) {
//                                showSuccess("User deleted successfully");
//                                loadUsers();
//                            } else {
//                                showError("Failed to delete user: " + response.statusCode());
//                            }
//                        });
//                    })
//                    .exceptionally(e -> {
//                        showError("Network error: " + e.getMessage());
//                        return null;
//                    });
//        }
//    }
//
//    private void selectUser(UserRow user) {
//        selectedUser = user;
//        usernameField.setText(user.getUsername());
//        emailField.setText(user.getEmail());
//        roleCombo.setValue(user.getRole());
//        enabledCheck.setSelected(user.isEnabled());
//        passwordField.clear();
//        passwordField.setPromptText("Leave empty to keep current password");
//        saveButton.setText("UPDATE");
//        errorLabel.setText("");
//        successLabel.setText("");
//    }
//
//    public void clearForm() {
//        selectedUser = null;
//        usernameField.clear();
//        emailField.clear();
//        passwordField.clear();
//        roleCombo.setValue("VISITOR");
//        enabledCheck.setSelected(true);
//        saveButton.setText("CREATE");
//        errorLabel.setText("");
//        successLabel.setText("");
//    }
//
//    private void showError(String message) {
//        javafx.application.Platform.runLater(() -> {
//            errorLabel.setText(message);
//            successLabel.setText("");
//        });
//    }
//
//    private void showSuccess(String message) {
//        javafx.application.Platform.runLater(() -> {
//            successLabel.setText(message);
//            errorLabel.setText("");
//        });
//    }
//
//    // UserRow model class
//    public static class UserRow {
//        private String id;
//        private String username;
//        private String email;
//        private String role;
//        private boolean enabled;
//        private String createdAt;
//
//        public UserRow() {}
//
//        public String getId() { return id; }
//        public String getUsername() { return username; }
//        public String getEmail() { return email; }
//        public String getRole() { return role; }
//        public boolean isEnabled() { return enabled; }
//        public String getCreatedAt() { return createdAt; }
//
//        public void setId(String id) { this.id = id; }
//        public void setUsername(String username) { this.username = username; }
//        public void setEmail(String email) { this.email = email; }
//        public void setRole(String role) { this.role = role; }
//        public void setEnabled(boolean enabled) { this.enabled = enabled; }
//        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
//    }
//}