package com.prodman.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.config.ApiConfig;
import com.prodman.desktop.model.AuthResponse;
import com.prodman.desktop.model.User;

public class AuthService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static AuthResponse login(String username, String password) {
        // Просто возвращаем фиктивный ответ без запроса к серверу
        AuthResponse response = new AuthResponse();
        response.setAccessToken("fake-token");
        response.setRefreshToken("fake-refresh");
        response.setUserId("admin-id");
        response.setUsername("admin");
        response.setEmail("admin@prodman.com");
        response.setRole("ADMIN");
        response.setExpiresIn(86400000L);
        return response;
    }

//    public static AuthResponse login(String username, String password) throws Exception {
//        System.out.println("DEBUG: AUTH_LOGIN URL = " + ApiConfig.AUTH_LOGIN);
//
//        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
//        System.out.println("DEBUG: Request JSON = " + json);
//
//        Object requestBody = mapper.readValue(json, Object.class);
//        String response = HttpClientService.postPublic(ApiConfig.AUTH_LOGIN, requestBody);
//
//        System.out.println("DEBUG: Raw response = " + response);
//
//        return mapper.readValue(response, AuthResponse.class);
//    }

    public static User register(String username, String email, String password, String role) throws Exception {
        String json = String.format("{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                username, email, password, role);
        String response = HttpClientService.postPublic(ApiConfig.AUTH_REGISTER,
                mapper.readValue(json, Object.class));

        return mapper.readValue(response, User.class);
    }

    public static void logout() throws Exception {
        HttpClientService.post(ApiConfig.AUTH_LOGOUT, null);
    }

    public static User getCurrentUser(String username) throws Exception {
        String response = HttpClientService.get(ApiConfig.AUTH_ME + "?username=" + username);
        return mapper.readValue(response, User.class);
    }
}