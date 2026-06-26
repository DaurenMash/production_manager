package com.prodman.workcalendar.model;

public enum Position {
    OPERATOR("Оператор станка"),
    TECHNICIAN("Техник"),
    ENGINEER("Инженер"),
    SHIFT_SUPERVISOR("Сменный мастер"),
    QUALITY_CONTROL("Контролёр качества"),
    MAINTENANCE("Ремонтник"),
    LOGISTICIAN("Логист"),
    MANAGER("Менеджер");

    private final String displayName;

    Position(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}