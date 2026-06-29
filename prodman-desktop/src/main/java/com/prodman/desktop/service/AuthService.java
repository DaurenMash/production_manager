package com.prodman.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.config.ApiConfig;
import com.prodman.desktop.model.AuthResponse;
import com.prodman.desktop.model.User;
import com.prodman.desktop.utils.TokenManager;

public class AuthService {
    private static final ObjectMapper mapper = new ObjectMapper();

    // ✅ ВРЕМЕННО: фейковый логин для разработки
    public static AuthResponse login(String username, String password) {
        System.out.println("DEBUG: Using fake login for development");

        AuthResponse response = new AuthResponse();
        response.setAccessToken("fake-token-" + System.currentTimeMillis());
        response.setRefreshToken("fake-refresh");
        response.setUserId("admin-id");
        response.setUsername("admin");
        response.setEmail("admin@prodman.com");
        response.setRole("ADMIN");
        response.setExpiresIn(86400000L);

        // ✅ Сохраняем фейковый токен
        TokenManager.saveTokens(
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getUsername(),
                response.getUserId(),
                response.getRole()
        );

        return response;
    }

    // ❌ Реальный код закомментирован на время разработки
    /*
    public static AuthResponse login(String username, String password) throws Exception {
        System.out.println("DEBUG: AUTH_LOGIN URL = " + ApiConfig.AUTH_LOGIN);
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        System.out.println("DEBUG: Request JSON = " + json);
        Object requestBody = mapper.readValue(json, Object.class);
        String response = HttpClientService.postPublic(ApiConfig.AUTH_LOGIN, requestBody);
        System.out.println("DEBUG: Raw response = " + response);
        if (response.contains("\"status\":500")) {
            throw new Exception("Server error: " + response);
        }
        AuthResponse authResponse = mapper.readValue(response, AuthResponse.class);
        TokenManager.saveTokens(
            authResponse.getAccessToken(),
            authResponse.getRefreshToken(),
            authResponse.getUsername(),
            authResponse.getUserId(),
            authResponse.getRole()
        );
        return authResponse;
    }
    */

    public static User register(String username, String email, String password, String role) throws Exception {
        String json = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                username, email, password, role
        );
        String response = HttpClientService.postPublic(
                ApiConfig.AUTH_REGISTER,
                mapper.readValue(json, Object.class)
        );
        return mapper.readValue(response, User.class);
    }

    public static void logout() {
        try {
            HttpClientService.post(ApiConfig.AUTH_LOGOUT, null);
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
        } finally {
            TokenManager.clearToken();
        }
    }

    public static User getCurrentUser() throws Exception {
        String username = TokenManager.getUsername();
        if (username == null || username.isEmpty()) {
            throw new Exception("User not logged in");
        }
        String response = HttpClientService.get(ApiConfig.AUTH_ME + "?username=" + username);
        return mapper.readValue(response, User.class);
    }
}