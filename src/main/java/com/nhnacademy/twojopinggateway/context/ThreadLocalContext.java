package com.nhnacademy.twojopinggateway.context;

public class ThreadLocalContext {
    private static final ThreadLocal<String> accessTokenHolder = new ThreadLocal<>();

    public static String getAccessToken() {
        return accessTokenHolder.get();
    }

    public static void setAccessToken(String accessToken) {
        accessTokenHolder.set(accessToken);
    }

    public static void clear() {
        accessTokenHolder.remove();
    }
}
