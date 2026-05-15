package org.agora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.agora.dto.BookingRequest;
import org.agora.dto.UserProfileResponse;
import org.agora.dto.LoginRequest;
import org.agora.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for User Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String validJwtToken;

    /**
     * Setup authentication
     * @throws Exception
     */
    @BeforeEach
    void setupAuth() throws Exception {
        // Arrange Registration
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setName("fahritest3");
        regRequest.setEmail("fahri@agora.com");
        regRequest.setPassword("rahasia123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)));

        // Arrange Login to get JWT
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("fahri@agora.com");
        loginRequest.setPassword("rahasia123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // extract token from JSON response
        validJwtToken = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    /**
     * FR-03 : Get User Profile
     * Success Case
     * @throws Exception
     */
    @Test
    void getProfileCurrentUser () throws Exception {
        // Act
        mockMvc.perform(get("/api/user")
                        .header("Authorization", "Bearer " + validJwtToken))
        // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fahri@agora.com"))
                .andExpect(jsonPath("$.name").value("fahritest3"));
    }
}
