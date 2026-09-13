package com.skinzen.user_management_system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterServiceTest {

    private final RateLimiterService rateLimiterService = new RateLimiterService();

    @Test
    void shouldAllowFiveRequests() {

        String key = "127.0.0.1";

        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.isLoginAllowed(key));
        }
    }

    @Test
    void shouldRejectSixthRequest() {

        String key = "127.0.0.1";

        for (int i = 0; i < 5; i++) {
            rateLimiterService.isLoginAllowed(key);
        }

        assertFalse(
                rateLimiterService.isLoginAllowed(key)
        );
    }
}