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

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    Page<Event> findByIsActiveTrueAndDateTimeAfter(ZonedDateTime dateTime, Pageable pageable);

    @Modifying
    @Query("UPDATE Event e SET e.seatsAvailable = e.seatsAvailable - :qty WHERE e.eventId = :id AND e.seatsAvailable >= :qty")
    int decreaseSeatQuota(@Param("id") String eventId, @Param("qty") int quantity);

    @Modifying
    @Query("UPDATE Event e SET e.seatsAvailable = e.seatsAvailable + :qty WHERE e.eventId = :id")
    void increaseSeatQuota(@Param("id") String eventId, @Param("qty") int quantity);

    Page<Event> findByCreator_Email(String email, Pageable pageable);

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