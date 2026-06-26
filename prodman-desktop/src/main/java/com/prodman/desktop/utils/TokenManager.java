package com.prodman.desktop.utils;

import java.io.*;
import java.nio.file.*;

public class TokenManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.prodman/";
    private static final String TOKEN_FILE = CONFIG_DIR + "token.dat";

    private static String accessToken;
    private static String refreshToken;
    private static String username;
    private static String userId;
    private static String role;

    public static void saveTokens(String access, String refresh, String user, String id, String userRole) {
        accessToken = access;
        refreshToken = refresh;
        username = user;
        userId = id;
        role = userRole;

        System.out.println("DEBUG: Saving token = " + access);
        System.out.println("DEBUG: Saving role = " + userRole);

        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TOKEN_FILE))) {
                oos.writeObject(access);
                oos.writeObject(refresh);
                oos.writeObject(user);
                oos.writeObject(id);
                oos.writeObject(userRole);
            }
            System.out.println("DEBUG: Token saved successfully to " + TOKEN_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save tokens: " + e.getMessage());
        }
    }

    public static void loadTokens() {
        try {
            File file = new File(TOKEN_FILE);
            if (file.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    accessToken = (String) ois.readObject();
                    refreshToken = (String) ois.readObject();
                    username = (String) ois.readObject();
                    userId = (String) ois.readObject();
                    role = (String) ois.readObject();
                    System.out.println("DEBUG: Loaded token = " + accessToken);
                    System.out.println("DEBUG: Loaded role = " + role);
                }
            } else {
                System.out.println("DEBUG: Token file does not exist");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load tokens: " + e.getMessage());
        }
    }

    public static String getAccessToken() {
        if (accessToken == null) {
            System.out.println("DEBUG: accessToken is null, loading from file...");
            loadTokens();
        }
        System.out.println("DEBUG: Returning accessToken = " + accessToken);
        return accessToken;
    }

    public static String getRefreshToken() {
        if (refreshToken == null) loadTokens();
        return refreshToken;
    }

    public static String getUsername() {
        if (username == null) loadTokens();
        return username;
    }

    public static String getUserId() {
        if (userId == null) loadTokens();
        return userId;
    }

    public static String getRole() {
        if (role == null) loadTokens();
        return role;
    }

    public static boolean hasValidToken() {
        loadTokens();
        boolean valid = accessToken != null && !accessToken.isEmpty();
        System.out.println("DEBUG: hasValidToken = " + valid);
        return valid;
    }

    public static void clearToken() {
        accessToken = null;
        refreshToken = null;
        username = null;
        userId = null;
        role = null;

        try {
            Files.deleteIfExists(Paths.get(TOKEN_FILE));
            System.out.println("DEBUG: Token cleared");
        } catch (IOException e) {
            System.err.println("Failed to clear tokens: " + e.getMessage());
        }
    }
}