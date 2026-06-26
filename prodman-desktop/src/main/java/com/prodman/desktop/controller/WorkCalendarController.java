package com.prodman.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class WorkCalendarController {

    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private ComboBox<String> shiftConfigCombo;
    @FXML private ListView<String> conflictsListView;

    private YearMonth currentYearMonth = YearMonth.now();
    private Map<LocalDate, List<ShiftInfo>> shiftsMap = new HashMap<>();

    @FXML
    public void initialize() {
        shiftConfigCombo.setItems(FXCollections.observableArrayList(
            "2 смены (12ч)", "3 смены (8ч)"
        ));
        shiftConfigCombo.setValue("3 смены (8ч)");
        
        loadCalendar(currentYearMonth);
        loadConflicts();
    }

    @FXML
    public void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        loadCalendar(currentYearMonth);
    }

    @FXML
    public void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        loadCalendar(currentYearMonth);
    }

    @FXML
    public void today() {
        currentYearMonth = YearMonth.now();
        loadCalendar(currentYearMonth);
    }

    @FXML
    public void refresh() {
        loadCalendar(currentYearMonth);
        loadConflicts();
    }

    private void loadCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();
        monthLabel.setText(yearMonth.getMonth().getDisplayName(
            java.time.format.TextStyle.FULL, Locale.getDefault()) + " " + yearMonth.getYear());

        // Заголовки дней недели
        String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #aaa;");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Получение данных о сменах (заглушка)
        shiftsMap = generateMockShifts(yearMonth);

        // Заполнение дней
        LocalDate firstDay = yearMonth.atDay(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = yearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            VBox dayBox = createDayBox(date);
            calendarGrid.add(dayBox, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayBox(LocalDate date) {
        VBox box = new VBox(2);
        box.setStyle("-fx-background-color: #2d2d3f; -fx-background-radius: 5; -fx-padding: 5;");
        box.setPrefSize(100, 80);

        Text dayText = new Text(String.valueOf(date.getDayOfMonth()));
        dayText.setFill(Color.WHITE);

        // Проверка конфликтов
        boolean hasConflict = checkConflicts(date);

        // Смены
        List<ShiftInfo> shifts = shiftsMap.getOrDefault(date, new ArrayList<>());
        for (ShiftInfo shift : shifts) {
            Label shiftLabel = new Label(shift.employeeName + " - " + shift.shiftType);
            shiftLabel.setStyle("-fx-font-size: 8; -fx-text-fill: " + 
                (shift.isAvailable ? "#2ecc71" : "#e74c3c") + ";");
            box.getChildren().add(shiftLabel);
        }

        // Если есть конфликт - красная рамка
        if (hasConflict) {
            box.setStyle("-fx-background-color: #2d2d3f; -fx-background-radius: 5; -fx-padding: 5; -fx-border-color: #e74c3c; -fx-border-width: 2;");
        }

        return box;
    }

    private boolean checkConflicts(LocalDate date) {
        // Проверка: отпуска, больничные, перекрытия
        return false; // Заглушка
    }

    private Map<LocalDate, List<ShiftInfo>> generateMockShifts(YearMonth yearMonth) {
        Map<LocalDate, List<ShiftInfo>> map = new HashMap<>();
        // Заглушка с тестовыми данными
        Random random = new Random();
        String[] employees = {"Иванов И.", "Петров П.", "Сидоров С.", "Козлов К."};
        String[] shiftTypes = {"Утро", "Вечер", "Ночь"};

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            List<ShiftInfo> shifts = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                shifts.add(new ShiftInfo(
                    employees[random.nextInt(employees.length)],
                    shiftTypes[random.nextInt(3)],
                    random.nextBoolean()
                ));
            }
            map.put(date, shifts);
        }
        return map;
    }

    private void loadConflicts() {
        // Загрузка конфликтов из API
        conflictsListView.getItems().clear();
        conflictsListView.getItems().addAll(
            "⚠ Иванов И. - В отпуске 15-30 июня",
            "⚠ Петров П. - Больничный 20-25 июня",
            "⚠ Перекрытие смен 22 июня"
        );
    }

    @Data
    @AllArgsConstructor
    private static class ShiftInfo {
        String employeeName;
        String shiftType;
        boolean isAvailable;
    }
}