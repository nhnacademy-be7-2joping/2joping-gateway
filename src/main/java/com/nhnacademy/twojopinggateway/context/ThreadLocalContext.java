package com.nhnacademy.twojopinggateway.context;

import java.util.concurrent.Callable;

public class ThreadLocalContext {
    private static final ThreadLocal<String> accessTokenHolder = new ThreadLocal<>();

    public static void setAccessToken(String accessToken) {
        accessTokenHolder.set(accessToken);
    }

    public static String getAccessToken() {
        return accessTokenHolder.get();
    }

    public static void clear() {
        accessTokenHolder.remove();
    }


    public static <V> Callable<V> wrap(Callable<V> callable) {
        String accessToken = accessTokenHolder.get();
        return () -> {
            accessTokenHolder.set(accessToken);
            try {
                return callable.call();
            } finally {
                accessTokenHolder.remove();
            }
        };
    }
}
