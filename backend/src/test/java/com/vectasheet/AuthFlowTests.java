package com.vectasheet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AuthFlowTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerLoginAndAccessProtectedRoute() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@example.com";

        String registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Test User",
                "email", email,
                "password", "SecurePass123"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.email").value(email));

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "SecurePass123"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Test User",
                        "email", email,
                        "password", "SecurePass123"
                ))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "WrongPassword"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/workspaces"))
                .andExpect(status().isUnauthorized());
    }
}
