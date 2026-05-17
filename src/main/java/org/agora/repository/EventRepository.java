package org.agora.repository;

import org.agora.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

/**
 * Repository interface for {@link Event} entities.
 * Provides custom database queries including dynamic searching and atomic operations for seat management.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    /**
     * Retrieves a paginated list of active events scheduled after a specific date (upcoming events).
     */
    Page<Event> findByIsActiveTrueAndDateTimeAfter(ZonedDateTime dateTime, Pageable pageable);

    /**
     * Atomically decreases the available seat quota for an event.
     * Prevents overselling by enforcing the condition that available seats must be >= requested quantity.
     * @param eventId  the ID of the event.
     * @param quantity the number of seats to deduct.
     * @return the number of affected rows (0 if insufficient seats or event not found).
     */
    @Modifying
    @Query("UPDATE Event e SET e.seatsAvailable = e.seatsAvailable - :qty WHERE e.eventId = :id AND e.seatsAvailable >= :qty")
    int decreaseSeatQuota(@Param("id") String eventId, @Param("qty") int quantity);

    /**
     * Atomically restores the available seat quota for an event.
     * Typically used when a booking is canceled.
     * @param eventId  the ID of the event.
     * @param quantity the number of seats to add back.
     */
    @Modifying
    @Query("UPDATE Event e SET e.seatsAvailable = e.seatsAvailable + :qty WHERE e.eventId = :id")
    void increaseSeatQuota(@Param("id") String eventId, @Param("qty") int quantity);

    /**
     * Retrieves a paginated list of events created by a specific user email.
     */
    Page<Event> findByCreator_Email(String email, Pageable pageable);

    /**
     * Performs a complex, dynamic search to filter active events based on multiple optional parameters.
     * @param title       keyword to match against the event title (case-insensitive).
     * @param location    keyword to match against the event location (case-insensitive).
     * @param creatorName keyword to match against the creator's name (case-insensitive).
     * @param showPast    if true, includes events that have already passed; otherwise, only upcoming.
     * @param now         the current timestamp used for the 'showPast' logic.
     * @param startDate   the start of the date range filter.
     * @param endDate     the end of the date range filter.
     * @param pageable    pagination instructions.
     * @return a paginated list of events matching the dynamic criteria.
     */
    @Query("SELECT e FROM Event e WHERE " +
            "(:title = '' OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location = '' OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:creatorName = '' OR LOWER(e.creator.name) LIKE LOWER(CONCAT('%', :creatorName, '%'))) AND " +
            "(:showPast = true OR e.dateTime >= :now) AND " +
            "(e.dateTime BETWEEN :startDate AND :endDate) AND " +
            "(e.isActive = true)")
    Page<Event> searchEvents(
            @Param("title") String title,
            @Param("location") String location,
            @Param("creatorName") String creatorName,
            @Param("showPast") boolean showPast,
            @Param("now") ZonedDateTime now,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate,
            Pageable pageable
    );
}