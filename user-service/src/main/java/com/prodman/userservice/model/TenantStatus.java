package com.prodman.userservice.model;

/**
 * Статус тенанта (организации-клиента).
 *
 * TRIAL     — триальный период (по умолчанию 30 дней).
 * ACTIVE    — оплачен и работает.
 * SUSPENDED — отключён (нет оплаты, закончился триал и т.п.).
 * CANCELLED — расторгнут.
 */
public enum TenantStatus {
    TRIAL,
    ACTIVE,
    SUSPENDED,
    CANCELLED
}