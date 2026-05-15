package org.agora.service;

import org.agora.dto.BookingRequest;
import org.agora.dto.BookingResponse;
import org.agora.entity.Booking;
import org.agora.entity.Event;
import org.agora.entity.User;
import org.agora.repository.BookingRepository;
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
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for BookingService
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

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
     * FR-10 : Create Booking
     * FR-13 : Booking Confirmation
     * Success Case
     */
    @Test
    void createBookingSuccess() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        User mockUser = new User();
        mockUser.setEmail(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        Event mockEvent = new Event();
        mockEvent.setEventId("event-123");
        mockEvent.setTitle("Tech Conference");
        mockEvent.setIsActive(true);
        mockEvent.setPrice(new BigDecimal("50000.00"));

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(mockEvent));
        when(bookingRepository.existsByUser_EmailAndEvent_EventIdAndStatus(userEmail, "event-123", "CONFIRMED"))
                .thenReturn(false);
        when(eventRepository.decreaseSeatQuota("event-123", 2)).thenReturn(1);

        // Request buy 2 tickets
        BookingRequest request = mock(BookingRequest.class);
        when(request.eventId()).thenReturn("event-123");
        when(request.quantity()).thenReturn(2);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        BookingResponse response = bookingService.createBooking(request);

        // Assert
        assertNotNull(response);
        assertEquals("CONFIRMED", response.status());
        assertEquals(2, response.quantity());
        // totalPrice = price * quantity
        assertEquals(new BigDecimal("100000.00"), response.totalPrice());
        // FR-13 : Booking Confirmation after successful booking
        assertNotNull(response.bookingReference());

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    /**
     * FR-10 : Create Booking
     * Fail Case : Not enough seats
     */
    @Test
    void createBookingNotEnoughSeatsThrowsException() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        User mockUser = new User();
        mockUser.setEmail(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        Event mockEvent = new Event();
        mockEvent.setEventId("event-123");
        mockEvent.setIsActive(true);
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(mockEvent));

        BookingRequest request = mock(BookingRequest.class);
        when(request.eventId()).thenReturn("event-123");
        when(request.quantity()).thenReturn(5);

        // decreaseSeatQuota return how many updated row
        when(eventRepository.decreaseSeatQuota("event-123", 5)).thenReturn(0);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(request);
        });

        assertEquals("Booking failed: Not enough seats available or event not found.", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    /**
     * FR-11 : My Bookings
     * Success Case
     */
    @Test
    void getUserBookingsSuccess() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        Pageable pageable = PageRequest.of(0, 10);

        Event mockEvent = new Event();
        mockEvent.setTitle("Workshop Spring Boot");
        mockEvent.setPrice(new BigDecimal("150000.00"));

        Booking mockBooking = new Booking();
        mockBooking.setBookingId("booking-123");
        mockBooking.setBookingReference("BKG-260515182300-A1B2C3");
        mockBooking.setQuantity(2);
        mockBooking.setStatus("CONFIRMED");
        mockBooking.setEvent(mockEvent);

        Page<Booking> bookingPage = new PageImpl<>(List.of(mockBooking));

        when(bookingRepository.findByUser_Email(userEmail, pageable)).thenReturn(bookingPage);

        // Act
        Page<BookingResponse> response = bookingService.getUserBookings(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        BookingResponse firstResponse = response.getContent().get(0);
        assertEquals("booking-123", firstResponse.bookingId());
        assertEquals("Workshop Spring Boot", firstResponse.eventTitle());
        assertEquals("BKG-260515182300-A1B2C3", firstResponse.bookingReference());
        assertEquals(2, firstResponse.quantity());
        // Harga tiket 150rb * 2 kuantitas = 300rb
        assertEquals(new BigDecimal("300000.00"), firstResponse.totalPrice());

        verify(bookingRepository, times(1)).findByUser_Email(userEmail, pageable);
    }

    /**
     * FR-12 : Cancel Booking
     * Success Case
     */
    @Test
    void cancelBookingSuccess() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        User mockUser = new User();
        mockUser.setEmail(userEmail);

        Event mockEvent = new Event();
        mockEvent.setEventId("event-123");

        Booking mockBooking = new Booking();
        mockBooking.setBookingId("booking-999");
        mockBooking.setUser(mockUser);
        mockBooking.setEvent(mockEvent);
        mockBooking.setQuantity(2);
        mockBooking.setStatus("CONFIRMED");

        when(bookingRepository.findById("booking-999")).thenReturn(Optional.of(mockBooking));

        // Act
        bookingService.cancelBooking("booking-999");

        // Assert
        assertEquals("CANCELED", mockBooking.getStatus());
        verify(bookingRepository, times(1)).save(mockBooking);
        // increase quota after sucessful cancelation
        verify(eventRepository, times(1)).increaseSeatQuota("event-123", 2);
    }

    /**
     * FR-12 : Cancel Bookinf
     * Fail Case : Prevent unauthorize booking
     */
    @Test
    void cancelBookingUnauthorizedThrowsException() {
        // Arrange
        mockSecurityContext("hacker@agora.com");

        User realOwner = new User();
        realOwner.setEmail("realowner@agora.com");

        Booking mockBooking = new Booking();
        mockBooking.setBookingId("booking-999");
        mockBooking.setUser(realOwner);

        when(bookingRepository.findById("booking-999")).thenReturn(Optional.of(mockBooking));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.cancelBooking("booking-999");
        });

        assertEquals("You are not authorized to cancel this booking", exception.getMessage());

        // Make sure nothing change in database
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventRepository, never()).increaseSeatQuota(anyString(), anyInt());
    }

    /**
     * FR-14 : Prevent Double Booking
     * Fail Case : User attempts to book the same event while already having an active booking
     */
    @Test
    void createBookingPreventDoubleBookingThrowsException() {
        // Arrange
        String userEmail = "fahri@agora.com";
        mockSecurityContext(userEmail);

        User mockUser = new User();
        mockUser.setEmail(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        Event mockEvent = new Event();
        mockEvent.setEventId("event-123");
        mockEvent.setIsActive(true);
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(mockEvent));

        // Simulation when user already have valid ticket for event.
        when(bookingRepository.existsByUser_EmailAndEvent_EventIdAndStatus(userEmail, "event-123", "CONFIRMED"))
                .thenReturn(true);

        BookingRequest request = mock(BookingRequest.class);
        when(request.eventId()).thenReturn("event-123");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(request);
        });

        assertEquals("You already have a confirmed booking for this event.", exception.getMessage());

        // Make sure nothing changed
        verify(eventRepository, never()).decreaseSeatQuota(anyString(), anyInt());
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}