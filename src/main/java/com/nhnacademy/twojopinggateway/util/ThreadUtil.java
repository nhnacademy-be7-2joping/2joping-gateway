package com.nhnacademy.twojopinggateway.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtil {
    private ThreadUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> CompletableFuture<T> toCompletableFuture(Future<T> future) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                completableFuture.complete(future.get());
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
    }
}
