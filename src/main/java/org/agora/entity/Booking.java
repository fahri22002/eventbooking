package org.agora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "\"Booking\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @Column(name = "\"bookingId\"", nullable = false)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"eventId\"", referencedColumnName = "\"eventId\"", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"", referencedColumnName = "\"userId\"", nullable = false)
    private User user;

    @Column(name = "\"createAt\"")
    private ZonedDateTime createAt;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "\"bookingReference\"", nullable = false, unique = true)
    private String bookingReference;

    @Column(name = "status")
    private String status;
}