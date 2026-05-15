package org.agora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.agora.dto.LoginRequest;
import org.agora.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for Auth Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
// Auto rollback after test
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * FR-01 : User Registration Test
     * Success Case
     * @throws Exception
     */
    @Test
    void registerE2ESuccess() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setName("Pengguna Test E2E");
        request.setEmail("e2etest@agora.com");
        request.setPassword("passwordKuat123");

        // Act
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
        // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.createAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    /**
     * FR-01 : User Registration Test
     * Fail Case : Email already exist
     * @throws Exception
     */
    @Test
    void registerE2EFailEmailAlreadyExists() throws Exception {
        // Arrange
        registerE2ESuccess();
        RegisterRequest request = new RegisterRequest();
        request.setName("User Test Same Email");
        request.setEmail("e2etest@agora.com");
        request.setPassword("pasword0986");

        // Act
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }


    /**
     * FR-02 : User Login
     * Sucess Case
     * @throws Exception
     */
    @Test
    void loginE2ESuccess() throws Exception {
        // Arrange
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setName("Test Login");
        regRequest.setEmail("testlogin@agora.com");
        regRequest.setPassword("rahasia123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testlogin@agora.com");
        loginRequest.setPassword("rahasia123");

        // Act
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

        // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    /**
     * FR-02 : User Login
     * Fail Case : Password incorrect
     * @throws Exception
     */
    @Test
    void loginE2EFailPasswordIncorrect() throws Exception {
        // Arrange
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setName("Test Login");
        regRequest.setEmail("testlogin@agora.com");
        regRequest.setPassword("rahasia123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testlogin@agora.com");
        loginRequest.setPassword("rahasia1234");

        // Act
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                // Assert
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}