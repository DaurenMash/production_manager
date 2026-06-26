package com.prodman.workcalendar.model;

public enum ShiftType {
    MORNING("Утро", 6, 14),
    EVENING("Вечер", 14, 22),
    NIGHT("Ночь", 22, 6),
    DAY_12("Дневная 12ч", 8, 20),
    NIGHT_12("Ночная 12ч", 20, 8);

    private final String displayName;
    private final int startHour;
    private final int endHour;

    ShiftType(String displayName, int startHour, int endHour) {
        this.displayName = displayName;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public String getDisplayName() { return displayName; }
    public int getStartHour() { return startHour; }
    public int getEndHour() { return endHour; }
}