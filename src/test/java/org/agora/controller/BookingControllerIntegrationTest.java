package org.agora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.agora.dto.BookingRequest;
import org.agora.dto.EventRequest;
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


import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Test class for Booking Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String validJwtToken;
    private String validEventId;

    /**
     * Setup authentication
     * @throws Exception
     */
    @BeforeEach
    void setupData() throws Exception {
        // 1. Register
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setName("Booking User");
        regRequest.setEmail("booking.buyer@agora.com");
        regRequest.setPassword("rahasia123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)));

        // 2. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("booking.buyer@agora.com");
        loginRequest.setPassword("rahasia123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // Extract JWT
        validJwtToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

        // 3. Make an Event
        EventRequest eventRequest = new EventRequest(
                "Seminar IoT & AI",
                "Implementasi Sistem Cerdas",
                "Cirebon",
                ZonedDateTime.now().plusDays(14),
                50,
                BigDecimal.valueOf(100000.0)
        );

        MvcResult eventResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andReturn();

        // Extract eventId
        validEventId = JsonPath.read(eventResult.getResponse().getContentAsString(), "$.eventId");
    }

    /**
     * Helper to login with another email
     * @throws Exception
     * @return JWT
     */
    String loginAnotherEmail() throws Exception {
        // Register
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setName("fahritest3");
        regRequest.setEmail("another@agora.com");
        regRequest.setPassword("rahasia123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)));

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("another@agora.com");
        loginRequest.setPassword("rahasia123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // Extract token
        String newJwt = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        return newJwt;
    }

    /**
     * FR-10 : Create Booking
     * FR-13 : Booking Confirmation
     * Success Case
     * @throws Exception
     */
    @Test
    void createBookingE2ESuccess() throws Exception {
        // Arrange
        BookingRequest request = new BookingRequest(validEventId, 2);

        // Act and Assert
        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(200000.0))
                .andExpect(jsonPath("$.bookingReference").exists());
    }

    /**
     * FR-11 : My Bookings
     * Success Case
     * @throws Exception
     */
    @Test
    void getUserBookingsE2ESuccess() throws Exception {
        //Arrange
        BookingRequest request = new BookingRequest(validEventId, 1);
        mockMvc.perform(post("/api/bookings")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Act and Assert
        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", "Bearer " + validJwtToken))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].eventTitle").value("Seminar IoT & AI"));
    }

    /**
     * FR-12 : Cancel Booking
     * Success Case
     * @throws Exception
     */
    @Test
    void cancelBookingE2ESuccess() throws Exception {
        // Arrange
        BookingRequest request = new BookingRequest(validEventId, 3);
        MvcResult bookingResult = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Extract bookingId
        String bookingId = JsonPath.read(bookingResult.getResponse().getContentAsString(), "$.bookingId");

        // Act and Assert
        mockMvc.perform(delete("/api/bookings/" + bookingId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNoContent());
    }

    /**
     * FR-12 : Cancel Booking
     * Fail Case : Try to cancel other's booking
     * @throws Exception
     */
    @Test
    void cancelBookingE2EFailForbidden() throws Exception {
        // Arrange
        BookingRequest request = new BookingRequest(validEventId, 3);
        MvcResult bookingResult = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Login new email
        String newJwt = loginAnotherEmail();
        // Extract bookingId
        String bookingId = JsonPath.read(bookingResult.getResponse().getContentAsString(), "$.bookingId");

        // Act and Assert
        mockMvc.perform(delete("/api/bookings/" + bookingId)
                        .header("Authorization", "Bearer " + newJwt))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /**
     * FR-12 : Cancel Booking
     * Fail Case : Booking not found
     * @throws Exception
     */
    @Test
    void cancelBookingE2EFailNotFound() throws Exception {
        // Arrange
        BookingRequest request = new BookingRequest(validEventId, 3);
        MvcResult bookingResult = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Extract bookingId
        String bookingId = JsonPath.read(bookingResult.getResponse().getContentAsString(), "$.bookingId");

        // Act and Assert
        mockMvc.perform(delete("/api/bookings/" + bookingId+"wrong") // wrong bookingId
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /**
     * FR-14 : Prevent Double Booking Booking
     * Fail Case : User attempts to book the same event while already having an active booking
     * @throws Exception
     */
    @Test
    void createBookingPreventDoubleBookingThrowsException() throws Exception {
        createBookingE2ESuccess();
        // Arrange
        BookingRequest request = new BookingRequest(validEventId, 2);

        // Act and Assert
        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You already have a confirmed booking for this event."));
    }
}