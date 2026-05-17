package org.agora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Entity representing an event available for booking.
 * Stores event details, scheduling, ticket pricing, dynamic capacity tracking, and a soft-deletion flag.
 */
@Entity
@Table(name = "\"Event\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @Column(name = "\"eventId\"", nullable = false)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"creatorId\"", referencedColumnName = "\"userId\"", nullable = false)
    private User creator;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "\"dateTime\"")
    private ZonedDateTime dateTime;

    @Column(name = "\"seatQuota\"")
    private Integer seatQuota;

    @Column(name = "\"seatsAvailable\"")
    private Integer seatsAvailable;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "\"isActive\"")
    private Boolean isActive;

    @Column(name = "\"createAt\"")
    private ZonedDateTime createAt;
}