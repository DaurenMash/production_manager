package com.prodman.desktop.config;

public class ApiConfig {
    // ===== API Gateway =====
    public static final String BASE_URL = "http://localhost:8888";

    // ===== User Service =====
    private static final String USER_PATH = "/user-service";
    private static final String API_V1 = "/api/v1";

    public static final String AUTH_LOGIN = BASE_URL + USER_PATH + API_V1 + "/auth/login";
    public static final String AUTH_REFRESH = BASE_URL + USER_PATH + API_V1 + "/auth/refresh";
    public static final String AUTH_REGISTER = BASE_URL + USER_PATH + API_V1 + "/auth/register";
    public static final String AUTH_LOGOUT = BASE_URL + USER_PATH + API_V1 + "/auth/logout";
    public static final String AUTH_ME = BASE_URL + USER_PATH + API_V1 + "/auth/me";
    public static final String USERS = BASE_URL + USER_PATH + API_V1 + "/users";

    // ===== Work Calendar =====
    private static final String WORK_PATH = "/work-calendar";

    public static final String EMPLOYEES = BASE_URL + WORK_PATH + "/employees";
    public static final String SCHEDULES = BASE_URL + WORK_PATH + "/schedules";
    public static final String SHIFTS = BASE_URL + WORK_PATH + "/shifts";
    public static final String EQUIPMENT = BASE_URL + WORK_PATH + "/equipment";
    public static final String EQUIPMENT_SCHEDULES = BASE_URL + WORK_PATH + "/equipment/schedules";

    // ===== Demo =====
    public static final String DEMO_DASHBOARD = BASE_URL + USER_PATH + API_V1 + "/demo/dashboard";
    public static final String DEMO_FEATURES = BASE_URL + USER_PATH + API_V1 + "/demo/features";
    public static final String DEMO_EQUIPMENT = BASE_URL + USER_PATH + API_V1 + "/demo/equipment-overview";
}