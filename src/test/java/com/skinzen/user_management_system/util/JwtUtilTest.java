package com.skinzen.user_management_system.util;

import com.skinzen.user_management_system.enums.Role;
import com.skinzen.user_management_system.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtUtilTest {

    @Test
    void generateAccessToken_shouldContainUserEmail() {

        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "my-test-secret-key-32-bytes-long-12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 3600L);
        jwtUtil.init();

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setRole(Role.USER);

        String token = jwtUtil.generateAccessToken(user);

        assertEquals(
                "test@gmail.com",
                jwtUtil.extractSubject(token)
        );
    }

}