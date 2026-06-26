package com.prodman.workcalendar.model;

public enum EmployeeStatus {
    AVAILABLE("Доступен"),
    ON_VACATION("В отпуске"),
    SICK_LEAVE("На больничном"),
    BUSINESS_TRIP("В командировке"),
    REMOTE("Удаленно"),
    NOT_AVAILABLE("Недоступен");

    private final String displayName;

    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}