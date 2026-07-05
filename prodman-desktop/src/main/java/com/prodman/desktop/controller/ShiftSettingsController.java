package com.prodman.desktop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.config.ApiConfig;
import com.prodman.desktop.utils.TokenManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftSettingsController {

    private static final Logger log = LoggerFactory.getLogger(ShiftSettingsController.class);
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML private RadioButton twoShiftsRadio;
    @FXML private RadioButton threeShiftsRadio;
    @FXML private ToggleGroup shiftToggleGroup;
    @FXML private TableView<ShiftRow> shiftsTable;
    @FXML private TableColumn<ShiftRow, Integer> colOrder;
    @FXML private TableColumn<ShiftRow, String> colName;
    @FXML private TableColumn<ShiftRow, String> colStart;
    @FXML private TableColumn<ShiftRow, String> colEnd;
    @FXML private TableColumn<ShiftRow, String> colColor;
    @FXML private Label messageLabel;

    private ObservableList<ShiftRow> shiftRows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Создаем ToggleGroup для радиокнопок
        ToggleGroup shiftToggleGroup = new ToggleGroup();
        twoShiftsRadio.setToggleGroup(shiftToggleGroup);
        threeShiftsRadio.setToggleGroup(shiftToggleGroup);

        // Настройка колонок таблицы
        colOrder.setCellValueFactory(new PropertyValueFactory<>("order"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));

        // Обработчики выбора количества смен
        twoShiftsRadio.setOnAction(e -> loadPreset(2));
        threeShiftsRadio.setOnAction(e -> loadPreset(3));

        // Загружаем текущую конфигурацию
        loadCurrentConfig();
    }

    private void loadPreset(int shiftCount) {
        shiftRows.clear();
        if (shiftCount == 2) {
            shiftRows.add(new ShiftRow(1, "День", "08:00", "20:00", "#2ecc71"));
            shiftRows.add(new ShiftRow(2, "Ночь", "20:00", "08:00", "#3498db"));
        } else {
            shiftRows.add(new ShiftRow(1, "Утро", "06:00", "14:00", "#f1c40f"));
            shiftRows.add(new ShiftRow(2, "День", "14:00", "22:00", "#2ecc71"));
            shiftRows.add(new ShiftRow(3, "Ночь", "22:00", "06:00", "#3498db"));
        }
        shiftsTable.setItems(shiftRows);
    }

    private void loadCurrentConfig() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showMessage("Не найден токен", true);
            return;
        }

        try {
            Request request = new Request.Builder()
                    .url(ApiConfig.SHIFT_CONFIGS_ACTIVE)
                    .header("Authorization", "Bearer " + token)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Map<String, Object> config = mapper.readValue(json, Map.class);

                    int shiftCount = (Integer) config.get("shiftCount");
                    if (shiftCount == 2) {
                        twoShiftsRadio.setSelected(true);
                    } else {
                        threeShiftsRadio.setSelected(true);
                    }

                    List<Map<String, Object>> shifts = (List<Map<String, Object>>) config.get("shifts");
                    shiftRows.clear();
                    for (Map<String, Object> s : shifts) {
                        shiftRows.add(new ShiftRow(
                                (Integer) s.get("displayOrder"),
                                (String) s.get("name"),
                                (String) s.get("startTime"),
                                (String) s.get("endTime"),
                                (String) s.get("color")
                        ));
                    }
                    shiftsTable.setItems(shiftRows);
                }
            }
        } catch (Exception e) {
            log.error("Error loading config", e);
            showMessage("Ошибка загрузки настроек", true);
        }
    }

    @FXML
    private void handleSave() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            showMessage("Не найден токен", true);
            return;
        }

        try {
            int shiftCount = twoShiftsRadio.isSelected() ? 2 : 3;
            Map<String, Object> config = new HashMap<>();
            config.put("name", shiftCount == 2 ? "2 смены (День/Ночь)" : "3 смены (Утро/День/Ночь)");
            config.put("shiftCount", shiftCount);

            List<Map<String, Object>> shifts = shiftRows.stream()
                    .map(row -> {
                        Map<String, Object> s = new HashMap<>();
                        s.put("name", row.getName());
                        s.put("startTime", row.getStartTime());
                        s.put("endTime", row.getEndTime());
                        s.put("displayOrder", row.getOrder());
                        s.put("color", row.getColor());
                        return s;
                    })
                    .collect(java.util.stream.Collectors.toList());
            config.put("shifts", shifts);

            RequestBody body = RequestBody.create(
                    mapper.writeValueAsString(config),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(ApiConfig.SHIFT_CONFIGS)
                    .post(body)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    showMessage("Настройки успешно сохранены!", false);
                    loadCurrentConfig();
                } else {
                    showMessage("Ошибка сохранения: " + response.code(), true);
                }
            }
        } catch (Exception e) {
            log.error("Error saving config", e);
            showMessage("Ошибка сохранения: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleReset() {
        loadCurrentConfig();
        showMessage("Настройки сброшены", false);
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
    }

    public static class ShiftRow {
        private final int order;
        private final String name;
        private final String startTime;
        private final String endTime;
        private final String color;

        public ShiftRow(int order, String name, String startTime, String endTime, String color) {
            this.order = order;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.color = color;
        }

        public int getOrder() { return order; }
        public String getName() { return name; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getColor() { return color; }
    }
}