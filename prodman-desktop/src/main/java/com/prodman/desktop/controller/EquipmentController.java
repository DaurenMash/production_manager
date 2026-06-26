package com.prodman.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class EquipmentController {

    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, Integer> idColumn;
    @FXML private TableColumn<Equipment, String> nameColumn;
    @FXML private TableColumn<Equipment, String> statusColumn;
    @FXML private TableColumn<Equipment, String> currentOrderColumn;
    @FXML private TableColumn<Equipment, Double> efficiencyColumn;
    @FXML private TableColumn<Equipment, String> lastUpdateColumn;
    @FXML private TableColumn<Equipment, String> actionColumn;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private ObservableList<Equipment> equipmentList;
    private FilteredList<Equipment> filteredList;

    @FXML
    public void initialize() {
        // Настройка колонок
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        currentOrderColumn.setCellValueFactory(new PropertyValueFactory<>("currentOrder"));
        efficiencyColumn.setCellValueFactory(new PropertyValueFactory<>("efficiency"));
        lastUpdateColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));

        // Загрузка тестовых данных
        loadMockData();

        // Настройка фильтрации
        filteredList = new FilteredList<>(equipmentList, p -> true);
        equipmentTable.setItems(filteredList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(equipment -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCase = newVal.toLowerCase();
                return equipment.getName().toLowerCase().contains(lowerCase) ||
                       String.valueOf(equipment.getId()).contains(lowerCase);
            });
        });

        statusLabel.setText("Всего: " + equipmentList.size() + " станков");
    }

    private void loadMockData() {
        equipmentList = FXCollections.observableArrayList();
        String[] statuses = {"Работает", "Простой", "Поломка"};
        String[] orders = {"Заказ №1234", "Заказ №5678", "Нет", "Заказ №9012"};

        for (int i = 1; i <= 48; i++) {
            String status = statuses[i % 3];
            equipmentList.add(new Equipment(
                i,
                "Станок-" + i,
                status,
                orders[i % 4],
                Math.round((70 + Math.random() * 25) * 10) / 10.0,
                "Только что"
            ));
        }
    }

    @FXML
    public void handleRefresh() {
        loadMockData();
        equipmentTable.refresh();
    }

    @FXML
    public void filterAll() {
        filteredList.setPredicate(equipment -> true);
    }

    @FXML
    public void filterRunning() {
        filteredList.setPredicate(equipment -> "Работает".equals(equipment.getStatus()));
    }

    @FXML
    public void filterIdle() {
        filteredList.setPredicate(equipment -> "Простой".equals(equipment.getStatus()));
    }

    @FXML
    public void filterBroken() {
        filteredList.setPredicate(equipment -> "Поломка".equals(equipment.getStatus()));
    }

    // Модель данных для таблицы
    public static class Equipment {
        private final int id;
        private final String name;
        private final String status;
        private final String currentOrder;
        private final double efficiency;
        private final String lastUpdate;

        public Equipment(int id, String name, String status, String currentOrder, double efficiency, String lastUpdate) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.currentOrder = currentOrder;
            this.efficiency = efficiency;
            this.lastUpdate = lastUpdate;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public String getCurrentOrder() { return currentOrder; }
        public double getEfficiency() { return efficiency; }
        public String getLastUpdate() { return lastUpdate; }
    }
}