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

    public static final String WORK_EMPLOYEES = BASE_URL + WORK_PATH + "/employees";  // ← переименовать
    public static final String SCHEDULES = BASE_URL + WORK_PATH + "/schedules";
    public static final String SHIFTS = BASE_URL + WORK_PATH + "/shifts";
    public static final String EQUIPMENT = BASE_URL + WORK_PATH + "/equipment";
    public static final String EQUIPMENT_SCHEDULES = BASE_URL + WORK_PATH + "/equipment/schedules";

    // ===== Employee Service =====
    private static final String EMPLOYEE_PATH = "/employee-service";

    public static final String EMPLOYEES = BASE_URL + EMPLOYEE_PATH + "/api/v1/employees";  // ← основной
    public static final String EMPLOYEES_BY_EMAIL = EMPLOYEES + "/email/%s";
    public static final String EMPLOYEES_BY_USER = EMPLOYEES + "/user/%s";
    public static final String EMPLOYEES_BY_STATUS = EMPLOYEES + "/status/%s";

    // ===== Shift Configs =====
    public static final String SHIFT_CONFIGS = BASE_URL + USER_PATH + API_V1 + "/shift-configs";
    public static final String SHIFT_CONFIGS_ACTIVE = SHIFT_CONFIGS + "/active";
    public static final String SHIFT_CONFIGS_SHIFTS = SHIFT_CONFIGS + "/%s/shifts";
    public static final String SHIFT_CONFIGS_ACTIVATE = SHIFT_CONFIGS + "/%s/activate";

    // ===== Workstation Service =====
    private static final String WORKSTATION_PATH = "/workstation";
    public static final String WORKSTATIONS = BASE_URL + WORKSTATION_PATH + "/api/v1/workstations";
    public static final String WORKSTATIONS_ACTIVE = WORKSTATIONS + "/active";
    public static final String WORKSTATIONS_EMPLOYEES = WORKSTATIONS + "/%s/employees";

    // ===== Test Service =====
    private static final String TEST_PATH = "/test-service";

    public static final String TESTS = BASE_URL + TEST_PATH + "/api/v1/tests";
    public static final String TEST_RESULTS = BASE_URL + TEST_PATH + "/api/v1/test-results";
    public static final String TEST_RESULTS_SUBMIT = TEST_RESULTS + "/submit";
    public static final String TEST_RESULTS_BY_TEST = TEST_RESULTS + "/test/%s";
    public static final String TEST_RESULTS_BY_EMPLOYEE = TEST_RESULTS + "/employee/%s";

    // ===== Demo =====
    public static final String DEMO_DASHBOARD = BASE_URL + USER_PATH + API_V1 + "/demo/dashboard";
    public static final String DEMO_FEATURES = BASE_URL + USER_PATH + API_V1 + "/demo/features";
    public static final String DEMO_EQUIPMENT = BASE_URL + USER_PATH + API_V1 + "/demo/equipment-overview";

}
