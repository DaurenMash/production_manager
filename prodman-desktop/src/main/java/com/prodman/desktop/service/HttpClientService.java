package com.prodman.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodman.desktop.utils.TokenManager;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClientService {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String get(String url) throws IOException {
        String token = TokenManager.getAccessToken();
        System.out.println("DEBUG: Sending GET request to " + url + " with token: " + token);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        }
    }
    
    public static String post(String url, Object body) throws IOException {
        String json = mapper.writeValueAsString(body);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json, JSON))
                .header("Authorization", "Bearer " + TokenManager.getAccessToken())
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        }
    }
    
    public static String postPublic(String url, Object body) throws IOException {
        String json = mapper.writeValueAsString(body);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json, JSON))
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        }
    }
    
    public static String postWithToken(String url, String token, Object body) throws IOException {
        String json = mapper.writeValueAsString(body);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json, JSON))
                .header("Authorization", "Bearer " + token)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        }
    }
}