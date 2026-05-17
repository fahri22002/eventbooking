package org.agora.service;

import lombok.RequiredArgsConstructor;
import org.agora.dto.BookingRequest;
import org.agora.dto.BookingResponse;
import org.agora.entity.Booking;
import org.agora.entity.Event;
import org.agora.entity.User;
import org.agora.exception.DuplicateResourceException;
import org.agora.exception.ForbiddenAccessException;
import org.agora.exception.ResourceNotFoundException;
import org.agora.repository.BookingRepository;
import org.agora.repository.EventRepository;
import org.agora.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Service layer responsible for handling ticket booking business logic.
 * Manages the creation, retrieval, and cancellation of bookings while enforcing
 * strict concurrency controls, duplicate checks, and cancellation policies.
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new booking reservation. Enforces atomic seat deduction to prevent overselling,
     * ensures the user does not have an existing active booking, and generates a unique reference code.
     *
     * @param request the booking payload containing event ID and requested quantity.
     * @return the saved booking mapped to a response DTO.
     * @throws ResourceNotFoundException if the user or event is not found.
     * @throws DuplicateResourceException if the user already has a confirmed booking for the event.
     * @throws RuntimeException if the event is inactive or seats are insufficient.
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getIsActive()) {
            throw new RuntimeException("Event is no longer active");
        }

        boolean hasActiveBooking = bookingRepository.existsByUser_EmailAndEvent_EventIdAndStatus(
                currentUserEmail, request.eventId(), "CONFIRMED"
        );

        if (hasActiveBooking) {
            throw new DuplicateResourceException("You already have a confirmed booking for this event.");
        }

        int updatedRows = eventRepository.decreaseSeatQuota(event.getEventId(), request.quantity());

        if (updatedRows == 0) {
            throw new RuntimeException("Booking failed: Not enough seats available or event not found.");
        }

        String refCode;
        boolean isUnique = false;
        // Format: Using TimeStamp
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss");

        do {
            String timestamp = ZonedDateTime.now().format(formatter);
            String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            refCode = "BKG-" + timestamp + "-" + randomPart;

            if (!bookingRepository.existsByBookingReference(refCode)) {
                isUnique = true;
            }
        } while (!isUnique);

        Booking booking = Booking.builder()
                .bookingId(UUID.randomUUID().toString())
                .event(event)
                .user(user)
                .quantity(request.quantity())
                .bookingReference(refCode)
                .status("CONFIRMED")
                .createAt(ZonedDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponse(savedBooking, event);
    }

    /**
     * Retrieves a paginated list of all bookings associated with the currently authenticated user.
     *
     * @param pageable pagination and sorting instructions.
     * @return a paginated result of the user's bookings.
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(Pageable pageable) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return bookingRepository.findByUser_Email(currentUserEmail, pageable)
                .map(booking -> mapToResponse(booking, booking.getEvent()));
    }

    /**
     * Cancels an existing booking, provided it is requested by the booking owner
     * and is done at least 24 hours before the event's start time.
     * Restores the canceled seats back to the event's available quota.
     *
     * @param bookingId the unique identifier of the booking to cancel.
     * @throws ResourceNotFoundException if the booking does not exist.
     * @throws ForbiddenAccessException if the authenticated user does not own the booking.
     * @throws RuntimeException if the booking is already canceled or the 24-hour deadline has passed.
     */
    @Transactional
    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!booking.getUser().getEmail().equals(currentUserEmail)) {
            throw new ForbiddenAccessException("You are not authorized to cancel this booking");
        }

        if ("CANCELED".equals(booking.getStatus())) {
            throw new RuntimeException("This booking is already canceled");
        }

        ZonedDateTime eventTime = booking.getEvent().getDateTime();
        ZonedDateTime cancelDeadline = eventTime.minusHours(24);

        if (ZonedDateTime.now().isAfter(cancelDeadline)) {
            throw new RuntimeException("Cancellation is only allowed up to 24 hours before the event starts.");
        }

        booking.setStatus("CANCELED");
        bookingRepository.save(booking);

        eventRepository.increaseSeatQuota(booking.getEvent().getEventId(), booking.getQuantity());
    }

    /**
     * Helper method to map a {@link Booking} entity to a {@link BookingResponse} DTO.
     * Dynamically calculates the total price based on the event price and booked quantity.
     *
     * @param booking the booking entity.
     * @param event the associated event entity.
     * @return the mapped response object.
     */
    private BookingResponse mapToResponse(Booking booking, Event event) {
        BigDecimal totalHarga = event.getPrice().multiply(BigDecimal.valueOf(booking.getQuantity()));
        return new BookingResponse(
                booking.getBookingId(),
                event.getEventId(),
                event.getTitle(),
                booking.getBookingReference(),
                booking.getQuantity(),
                totalHarga,
                booking.getStatus(),
                booking.getCreateAt(),
                event.getDateTime()
        );
    }

}