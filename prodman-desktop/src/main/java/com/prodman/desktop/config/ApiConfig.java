package com.prodman.desktop.config;

public class ApiConfig {
    // Измените на ваш API Gateway URL
    public static final String BASE_URL = "http://localhost:8081/api/v1";
    public static final String AUTH_LOGIN = BASE_URL + "/auth/login";
    public static final String AUTH_REGISTER = BASE_URL + "/auth/register";
    public static final String AUTH_LOGOUT = BASE_URL + "/auth/logout";
    public static final String AUTH_ME = BASE_URL + "/auth/me";
    public static final String USERS = BASE_URL + "/users";
    public static final String DEMO_DASHBOARD = BASE_URL + "/demo/dashboard";
    public static final String DEMO_FEATURES = BASE_URL + "/demo/features";
    public static final String DEMO_EQUIPMENT = BASE_URL + "/demo/equipment.fxml-overview";
}