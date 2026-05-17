package org.agora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import static org.hamcrest.Matchers.*;

/**
 * Test class for Event Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventControllerIntegrationTest {

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
        // Register
        RegisterRequest regRequest = new RegisterRequest("fahritest3", "fahri@agora.com", "rahasia123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)));

        // Login
        LoginRequest loginRequest = new LoginRequest("fahri@agora.com", "rahasia123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // Extract token
        validJwtToken = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    /**
     * Helper to login with another email
     * @throws Exception
     * @return JWT
     */
    String loginAnotherEmail() throws Exception {
        // Register
        RegisterRequest regRequest = new RegisterRequest("fahritest3", "another@agora.com", "rahasia123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)));

        // Login
        LoginRequest loginRequest = new LoginRequest("another@agora.com", "rahasia123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // Extract token
        String newJwt = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        return newJwt;
    }

    /**
     * Helper to create dummy event
     * @return eventId
     * @throws Exception
     */
    private String createDummyEventAndGetId(int i) throws Exception {
        EventRequest request = new EventRequest(
                "Dummy Webinar Computer Vision "+i,
                "Deteksi Objek Menggunakan YOLO",
                "Cirebon",
                ZonedDateTime.now().plusDays(5),
                100,
                BigDecimal.valueOf(50000.0)
        );

        MvcResult result = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.eventId");
    }

    /**
     * Helper to create dummy event
     * @return eventId
     * @throws Exception
     */
    private String createDummyEventAndGetId(ZonedDateTime dateTime) throws Exception {
        EventRequest request = new EventRequest(
                "Dummy Webinar Computer Vision ",
                "Deteksi Objek Menggunakan YOLO",
                "Cirebon",
                dateTime,
                100,
                BigDecimal.valueOf(50000.0)
        );

        MvcResult result = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.eventId");
    }

    /**
     * Helper to get total data in event table
     * @return total
     * @throws Exception
     */
    private int getTotalElementsFromApi() throws Exception {
        String response = mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.totalElements");
    }

    /**
     * FR-04 : Create Event
     * Success Case
     */
    @Test
    void createEventE2ESuccess() throws Exception {
        // Arrange
        EventRequest request = new EventRequest(
                "Workshop AI", "Belajar RAG", "Bandung",
                ZonedDateTime.now().plusDays(10), 50, BigDecimal.valueOf(100000.0)
        );
        // Act
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Workshop AI"))
                .andExpect(jsonPath("$.creatorName").value("fahritest3"));
    }

    /**
     * FR-05 : List Events
     * Success Case
     * @throws Exception
     */
    @Test
    void getAllEventsE2ESuccess() throws Exception {
        // Arrange
        for(int i = 0; i <= 20; i++){
            createDummyEventAndGetId(i);
        }
        int totalData = getTotalElementsFromApi();
        int totalPages = (int)Math.ceil(((double) totalData)/10.0);
        // Act and Assert
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken))
                // Assert: Status
                .andExpect(status().isOk())

                // Assert: Content Array
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))

                // Assert: Pagination Metadata
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(totalData))
                .andExpect(jsonPath("$.totalPages").value(totalPages));
    }

    /**
     * FR-05 : List Events
     * Success Case : Prevent past event to show up
     * @throws Exception
     */
    @Test
    void getAllEventsE2ESuccessPreventPastEvent() throws Exception {
        // Arrange
        int totalData = getTotalElementsFromApi();
        int totalPages = (int)Math.ceil(((double) totalData)/10.0);

        createDummyEventAndGetId(ZonedDateTime.now().plusSeconds(1)); // Make event with dateTime event 1 sec later
        Thread.sleep(1200); // So the event is in the past.
        // Act and Assert
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken))
                // Assert: Status
                .andExpect(status().isOk())

                // Assert: Content Array
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(Math.min(10,totalData)))

                // Assert: Pagination Metadata
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(totalData))
                .andExpect(jsonPath("$.totalPages").value(totalPages));
    }

    /**
     * FR-05 : List Events
     * Fail Case : Try to access withput JWT
     * @throws Exception
     */
    @Test
    void getAllEventsE2EFailWithoutLogin() throws Exception {
        // Arrange
        for(int i = 0; i <= 20; i++){
            createDummyEventAndGetId(i);
        }
        int totalData = getTotalElementsFromApi();
        int totalPages = (int)Math.ceil(((double) totalData)/10.0);
        // Act and Assert
        mockMvc.perform(get("/api/events"))
                // Assert: Status
                .andExpect(status().isUnauthorized())

                // Assert: error
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /**
     * FR-06 : Get Event Detail
     * Success Case
     * @throws Exception
     */
    @Test
    void getDetailE2ESuccess() throws Exception {
        // Arrange
        String eventId = createDummyEventAndGetId(1);
        // Act and Assert
        mockMvc.perform(get("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.title").value("Dummy Webinar Computer Vision 1"))
                .andExpect(jsonPath("$.creatorName").value("fahritest3"))
                .andExpect(jsonPath("$.location").value("Cirebon"));
    }

    /**
     * FR-06 : Get Event Detail
     * Fail Case : Event not found
     * @throws Exception
     */
    @Test
    void getDetailE2EFailNotFound() throws Exception {
        // Arrange
        String eventId = createDummyEventAndGetId(1);
        // Act and Assert
        mockMvc.perform(get("/api/events/" + eventId+"wrong")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /**
     * FR-07 : Update Event
     * Success Case
     * @throws Exception
     */
    @Test
    void updateEventE2ESuccess() throws Exception {
        // Arrange
        String eventId = createDummyEventAndGetId(1);

        EventRequest updateRequest = new EventRequest(
                "Webinar Computer Vision (Updated)", "Deskripsi baru", "Jakarta",
                ZonedDateTime.now().plusDays(20), 200, BigDecimal.valueOf(75000.0)
        );

        // Act and Assert
        mockMvc.perform(put("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Webinar Computer Vision (Updated)"))
                .andExpect(jsonPath("$.location").value("Jakarta"));
    }

    /**
     * FR-07 : Update Event
     * Fail Case : Update other's event
     * @throws Exception
     */
    @Test
    void updateEventE2EFailForbidden() throws Exception {
        // Arrange
        String eventId = createDummyEventAndGetId(1);

        String newJwt = loginAnotherEmail();

        EventRequest updateRequest = new EventRequest(
                "Webinar Computer Vision (Updated)", "Deskripsi baru", "Jakarta",
                ZonedDateTime.now().plusDays(20), 200, BigDecimal.valueOf(75000.0)
        );

        // Act and Assert
        mockMvc.perform(put("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + newJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /**
     * FR-07 : Update Event
     * Fail Case : Event not found
     * @throws Exception
     */
    @Test
    void updateEventE2EFailEventNotFound() throws Exception {
        // Arrange
        String eventId = createDummyEventAndGetId(1);

        EventRequest updateRequest = new EventRequest(
                "Webinar Computer Vision (Updated)", "Deskripsi baru", "Jakarta",
                ZonedDateTime.now().plusDays(20), 200, BigDecimal.valueOf(75000.0)
        );

        // Act and Assert
        mockMvc.perform(put("/api/events/" + eventId + "wrong") // wrong eventId
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /**
     * FR-07 : Update Event
     * Fail Case : Event has passed
     * @throws Exception
     */
    @Test
    void updateEventE2EFailPastEvent() throws Exception {
        // Arrange
        String eventId = createDummyEventAndGetId(ZonedDateTime.now().plusSeconds(1)); // Make event with dateTime event 1 sec later
        Thread.sleep(1200); // So the event is in the past.

        EventRequest updateRequest = new EventRequest(
                "Webinar Computer Vision (Updated)", "Deskripsi baru", "Jakarta",
                ZonedDateTime.now().plusDays(20), 200, BigDecimal.valueOf(75000.0)
        );

        // Act and Assert
        mockMvc.perform(put("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    /**
     * FR-08 : Delete Event
     * Success Case
     * @throws Exception
     */
    @Test
    void deactivateEventE2ESuccess() throws Exception {
        String eventId = createDummyEventAndGetId(1);

        mockMvc.perform(delete("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNoContent());
    }

    /**
     * FR-08 : Delete Event
     * Fail Case : Try to deactivate/delete other's event
     * @throws Exception
     */
    @Test
    void deactivateEventE2EFailForbidden() throws Exception {
        String eventId = createDummyEventAndGetId(1);

        String newJwt = loginAnotherEmail();

        mockMvc.perform(delete("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + newJwt))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /**
     * FR-08 : Delete Event
     * Fail Case : Event not found
     * @throws Exception
     */
    @Test
    void deactivateEventE2EFailNotFound() throws Exception {
        String eventId = createDummyEventAndGetId(1);

        mockMvc.perform(delete("/api/events/" + eventId + "wrong")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /**
     * Get My Event
     * Success Case
     * @throws Exception
     */
    @Test
    void getMyEventsE2ESuccess() throws Exception {
        createDummyEventAndGetId(1);

        mockMvc.perform(get("/api/events/my-events")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].creatorName").value("fahritest3"));
    }

    /**
     * FR-09 : Search Events
     * Success Case
     * URL: /api/events/search?title=Webinar&location=Cirebon&creatorName=fahritest3&showPast=True&startDate=2026-05-01T00:00:00Z&endDate=2036-05-31T23:59:59Z
     * @throws Exception
     */
    @Test
    void searchEventsE2ESuccess() throws Exception {
        // Arrange
        createDummyEventAndGetId(ZonedDateTime.now().plusDays(5));

        // Act and Assert
        mockMvc.perform(get("/api/events/search")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .param("title", "Webinar")
                        .param("location", "Cirebon")
                        .param("creatorName", "fahritest3")
                        .param("showPast", "True")
                        .param("startDate", ZonedDateTime.now().minusDays(5).toString())
                        .param("endDate", ZonedDateTime.now().plusDays(6).toString()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].title", everyItem(containsStringIgnoringCase("Webinar"))))
                .andExpect(jsonPath("$.content[*].location", everyItem(is("Cirebon"))))
                .andExpect(jsonPath("$.content[*].creatorName", everyItem(is("fahritest3"))))
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }
}