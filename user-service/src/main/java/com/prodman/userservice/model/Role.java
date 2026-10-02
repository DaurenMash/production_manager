package com.prodman.userservice.model;

public enum Role {
    PLATFORM_ADMIN, // Админ платформы (tenant_id = NULL)
    ADMIN,          // Полный доступ внутри тенанта
    OPERATOR,       // Управление станками, просмотр отчётов
    ANALYST,        // Аналитика и отчёты
    VISITOR         // Демо-доступ
}