package org.agora.repository;

import org.agora.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    // Nanti kita akan tambahkan method Pagination di sini untuk FR-05
}