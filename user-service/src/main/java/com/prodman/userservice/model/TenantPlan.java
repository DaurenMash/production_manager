package com.prodman.userservice.model;

/**
 * План тенанта.
 *
 * FREE  — триальный, доступ ко всем сервисам.
 * BASIC — платный, доступ только к: расписание/станции, сотрудники, пользователи, справочники.
 * UNLIM — платный, доступ ко всем сервисам.
 */
public enum TenantPlan {
    FREE,
    BASIC,
    UNLIM
}