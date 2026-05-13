package org.agora.repository;

import org.agora.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    @Modifying
    @Query("UPDATE Event e SET e.seatsAvailable = e.seatsAvailable - :qty WHERE e.eventId = :id AND e.seatsAvailable >= :qty")
    int decreaseSeatQuota(@Param("id") String eventId, @Param("qty") int quantity);
    @Modifying
    @Query("UPDATE Event e SET e.seatsAvailable = e.seatsAvailable + :qty WHERE e.eventId = :id")
    int increaseSeatQuota(@Param("id") String eventId, @Param("qty") int quantity);
}