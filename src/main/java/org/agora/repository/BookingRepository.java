package org.agora.repository;

import org.agora.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Page<Booking> findByUser_Email(String email, Pageable pageable);
    boolean existsByUser_EmailAndEvent_EventIdAndStatus(String email, String eventId, String status);
}