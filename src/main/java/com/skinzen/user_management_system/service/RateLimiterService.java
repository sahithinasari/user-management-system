package com.skinzen.user_management_system.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {
        Attempt attempt = attempts.computeIfAbsent(key, k -> new Attempt());

        synchronized (attempt) {
            long now = System.currentTimeMillis();

            if (now - attempt.windowStart > WINDOW_MS) {
                attempt.windowStart = now;
                attempt.count = 0;
            }

            attempt.count++;
            return attempt.count <= MAX_ATTEMPTS;
        }
    }

    private static class Attempt {
        int count = 0;
        long windowStart = System.currentTimeMillis();
    }
}
