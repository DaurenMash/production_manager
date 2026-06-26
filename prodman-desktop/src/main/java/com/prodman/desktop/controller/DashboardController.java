package com.prodman.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class DashboardController {

    @FXML private Text totalMachines;
    @FXML private Text activeMachines;
    @FXML private Text idleMachines;
    @FXML private Text brokenMachines;
    @FXML private Text oeeValue;
    @FXML private PieChart statusPieChart;
    @FXML private LineChart<String, Number> downtimeChart;
    @FXML private VBox topProblemsContainer;

    @FXML
    public void initialize() {
        // Установка значений
        totalMachines.setText("48");
        activeMachines.setText("42");
        idleMachines.setText("4");
        brokenMachines.setText("2");
        oeeValue.setText("87%");

        // Настройка Pie Chart
        statusPieChart.getData().addAll(
            new PieChart.Data("Работает", 42),
            new PieChart.Data("Простой", 4),
            new PieChart.Data("Поломка", 2)
        );

        // Настройка Line Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Простои (часы)");
        series.getData().add(new XYChart.Data<>("Пн", 2.5));
        series.getData().add(new XYChart.Data<>("Вт", 3.1));
        series.getData().add(new XYChart.Data<>("Ср", 1.8));
        series.getData().add(new XYChart.Data<>("Чт", 4.2));
        series.getData().add(new XYChart.Data<>("Пт", 2.9));
        downtimeChart.getData().add(series);
    }
}