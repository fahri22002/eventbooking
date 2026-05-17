package org.agora.service;

import org.agora.dto.EventRequest;
import org.agora.dto.EventResponse;
import org.agora.entity.Event;
import org.agora.entity.User;
import org.agora.repository.EventRepository;
import org.agora.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageImpl;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for EventService
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    /**
     * Cleanup data from previous test
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Mock security
     * @param email
     */
    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * FR-04 : Create Event
     * Success Case
     */
    @Test
    void createEventSuccess() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        User creator = new User();
        creator.setEmail(userEmail);
        creator.setName("Fahri Nizar Argubi");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(creator));

        EventRequest request = mock(EventRequest.class);
        when(request.title()).thenReturn("Webinar Computer Vision");
        when(request.location()).thenReturn("Cirebon");
        when(request.seatQuota()).thenReturn(100);
        when(request.price()).thenReturn(BigDecimal.valueOf(50000.0));

        // Returns the same Event object as the saved object.
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EventResponse response = eventService.createEvent(request);

        // Assert
        assertNotNull(response);
        assertEquals("Webinar Computer Vision", response.title());
        assertEquals("Cirebon", response.location());
        assertEquals(100, response.seatQuota());
        assertEquals(100, response.seatsAvailable());
        assertTrue(response.isActive());
        assertEquals("Fahri Nizar Argubi", response.creatorName());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    /**
     * FR-04 : Create Event
     * Fail Case : Unauthenticate creator
     */
    @Test
    void createEventUnauthenticateThrowsException() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        EventRequest request = mock(EventRequest.class);

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.createEvent(request);
        });

        // Assert
        assertEquals("User not found", exception.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
    }

    /**
     * FR-05 : List Events
     * Success Case
     */
    @Test
    void getAllEventsSuccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User creator = new User();
        creator.setName("Fahri Nizar Argubi");

        Event event = new Event();
        event.setEventId("event-123");
        event.setTitle("Tech Meetup");
        event.setPrice(BigDecimal.ZERO);
        event.setIsActive(true);
        event.setSeatsAvailable(100);
        event.setSeatQuota(100);
        event.setCreator(creator);
        event.setDateTime(ZonedDateTime.now().plusDays(2));

        Page<Event> eventPage = new PageImpl<>(List.of(event));
        when(eventRepository.findByIsActiveTrueAndDateTimeAfter(
                any(ZonedDateTime.class),
                eq(pageable)
        )).thenReturn(eventPage);

        // Act
        Page<EventResponse> response = eventService.getAllEvents(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Tech Meetup", response.getContent().get(0).title());
        verify(eventRepository, times(1)).findByIsActiveTrueAndDateTimeAfter(
                any(ZonedDateTime.class),
                eq(pageable)
        );
    }

    /**
     * FR-06 : Get Event Details
     * Success Case
     */
    @Test
    void getDetailEventSuccess() {
        // Arrange
        User creator = new User();
        creator.setName("Fahri Nizar Argubi");

        Event event = new Event();
        event.setEventId("event-123");
        event.setTitle("Webinar AI");
        event.setPrice(BigDecimal.valueOf(100000));
        event.setIsActive(true);
        event.setSeatsAvailable(100);
        event.setSeatQuota(100);
        event.setCreator(creator);

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(event));

        // Act
        EventResponse response = eventService.getDetailEvent("event-123");

        // Assert
        assertNotNull(response);
        assertEquals("event-123", response.eventId());
        assertEquals("Webinar AI", response.title());
        verify(eventRepository, times(1)).findById("event-123");
    }

    /**
     * FR-06 : Get Event Details
     * Fail Case : Event not found
     */
    @Test
    void getDetailEventNotFoundThrowsException() {
        // Arrange
        when(eventRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.getDetailEvent("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Event not found"));
    }

    /**
     * FR-07 : Update Event
     * Success Case
     */
    @Test
    void updateEventSuccess() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        User creator = new User();
        creator.setEmail(userEmail);
        creator.setName("Fahri Nizar Argubi");

        Event existingEvent = new Event();
        existingEvent.setEventId("event-123");
        existingEvent.setTitle("Judul Lama");
        existingEvent.setCreator(creator);
        existingEvent.setSeatQuota(100);
        existingEvent.setSeatsAvailable(100);
        existingEvent.setIsActive(true);
        existingEvent.setDateTime(ZonedDateTime.now().plusDays(2));
        existingEvent.setPrice(BigDecimal.valueOf(50000)); // Harga tidak berubah di DTO

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(existingEvent));

        EventRequest request = mock(EventRequest.class);
        when(request.title()).thenReturn("Judul Baru");
        when(request.location()).thenReturn("Jakarta");
        when(request.seatQuota()).thenReturn(200);

        // return same object
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        EventResponse response = eventService.updateEvent("event-123", request);

        // Assert
        assertNotNull(response);
        assertEquals("Judul Baru", response.title());
        assertEquals("Jakarta", response.location());
        assertEquals(200, response.seatQuota());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    /**
     * FR-07 : Update Event
     * Fail Case : Unauthorized request
     */
    @Test
    void updateEventUnauthorizedThrowsException() {
        // Arrange
        mockSecurityContext("hacker@agora.com");

        User creator = new User();
        creator.setEmail("fahri@agora.com");

        Event existingEvent = new Event();
        existingEvent.setEventId("event-123");
        existingEvent.setCreator(creator);

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(existingEvent));

        EventRequest request = mock(EventRequest.class);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.updateEvent("event-123", request);
        });

        assertEquals("You are not authorized to modify this event", exception.getMessage());

        // make sure the data is never stored in the database
        verify(eventRepository, never()).save(any(Event.class));
    }

    /**
     * FR-08 : Delete Event
     * Success Case
     */
    @Test
    void deactivateEventSuccess() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        User creator = new User();
        creator.setEmail(userEmail);

        Event existingEvent = new Event();
        existingEvent.setEventId("event-123");
        existingEvent.setIsActive(true);
        existingEvent.setCreator(creator);

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(existingEvent));

        // Act
        eventService.deactivateEvent("event-123");

        // Assert
        assertFalse(existingEvent.getIsActive());
        verify(eventRepository, times(1)).save(existingEvent);
    }

    /**
     * FR-08 : Delete Event
     * Fail Case : Event already inactive
     */
    @Test
    void deactivateEventAlreadyInactiveThrowsException() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        User creator = new User();
        creator.setEmail(userEmail);

        Event existingEvent = new Event();
        existingEvent.setEventId("event-123");
        existingEvent.setIsActive(false); // already inactive
        existingEvent.setCreator(creator);

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(existingEvent));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.deactivateEvent("event-123");
        });

        assertEquals("Event is already inactive", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    /**
     * FR-08 : Delete Event
     * Fail Case : Unauthorized request (Current user is not the creator)
     */
    @Test
    void deactivateEventUnauthorizedThrowsException() {
        // Arrange
        mockSecurityContext("hacker@agora.com");

        User originalCreator = new User();
        originalCreator.setEmail("fahri@agora.com");

        Event existingEvent = new Event();
        existingEvent.setEventId("event-123");
        existingEvent.setCreator(originalCreator);

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(existingEvent));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.deactivateEvent("event-123");
        });

        assertEquals("You are not authorized to modify this event", exception.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
    }

    /**
     * FR-09 : Search Events
     * Success Case
     */
    @Test
    void searchEventsSuccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User creator = new User();
        creator.setName("Fahri");

        Event event = new Event();
        event.setEventId("event-123");
        event.setTitle("Webinar Spring Boot");
        event.setLocation("Cirebon");
        event.setPrice(BigDecimal.ZERO);
        event.setSeatQuota(100);
        event.setSeatsAvailable(100);
        event.setIsActive(true);
        event.setCreator(creator);

        Page<Event> eventPage = new PageImpl<>(List.of(event));

        when(eventRepository.searchEvents(
                any(String.class), any(String.class), any(String.class), anyBoolean(),
                any(ZonedDateTime.class), any(ZonedDateTime.class), any(ZonedDateTime.class), eq(pageable)
        )).thenReturn(eventPage);

        // Act
        Page<EventResponse> response = eventService.searchEvents(
                "Webinar", "Cirebon", null, null, null, true, pageable
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Webinar Spring Boot", response.getContent().get(0).title());
    }

    /**
     * Get My Events
     * Success Case
     */
    @Test
    void getMyEventsSuccess() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);
        Pageable pageable = PageRequest.of(0, 10);

        User creator = new User();
        creator.setEmail(userEmail);
        creator.setName("Fahri");

        Event event = new Event();
        event.setEventId("event-123");
        event.setTitle("My Private Event");
        event.setPrice(BigDecimal.ZERO);
        event.setSeatQuota(100);
        event.setSeatsAvailable(100);
        event.setIsActive(true);
        event.setCreator(creator);

        Page<Event> eventPage = new PageImpl<>(List.of(event));
        when(eventRepository.findByCreator_Email(userEmail, pageable)).thenReturn(eventPage);

        // Act
        Page<EventResponse> response = eventService.getMyEvents(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("My Private Event", response.getContent().get(0).title());
        verify(eventRepository, times(1)).findByCreator_Email(userEmail, pageable);
    }
}