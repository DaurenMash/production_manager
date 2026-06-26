package com.prodman.userservice.model;

public enum Role {
    ADMIN,      // Полный доступ
    OPERATOR,   // Управление станками, просмотр отчётов
    ANALYST,    // Только аналитика и отчёты
    VISITOR     // Демо-доступ, только ознакомительные страницы
}