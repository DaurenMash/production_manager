package com.prodman.employeeservice.model;

public enum MonthCrossingMode {
    /** Все часы смены → в день начала (даже если смена перешла в следующий месяц). */
    KEEP_START_DAY,
    /** Разделить по календарным дням: часы до полуночи → в один день, после → в следующий. */
    SPLIT
}