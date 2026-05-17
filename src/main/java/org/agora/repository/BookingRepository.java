package org.agora.repository;

import org.agora.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Booking} entities.
 * Handles database operations for user reservations, including history retrieval and duplication checks.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    /**
     * Retrieves a paginated list of bookings associated with a specific user email.
     */
    Page<Booking> findByUser_Email(String email, Pageable pageable);

    /**
     * Checks if a user already has a booking for a specific event with a given status.
     * Used primarily to prevent duplicate or double bookings.
     */
    boolean existsByUser_EmailAndEvent_EventIdAndStatus(String email, String eventId, String status);

    /**
     * Checks if a generated booking reference code already exists in the database.
     * Ensures reference uniqueness during the booking creation process.
     */
    boolean existsByBookingReference(String bookingReference);
}