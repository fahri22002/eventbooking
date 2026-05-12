package org.agora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

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

    @Column(name = "datetime")
    private ZonedDateTime datetime;

    @Column(name = "\"seatQuota\"")
    private Integer seatQuota;

    @Column(name = "\"seatsAvailable\"")
    private Integer seatsAvailable;

    @Column(name = "price")
    private BigDecimal price; // BigDecimal lebih akurat untuk nominal uang daripada Double/Float

    @Column(name = "\"isActive\"")
    private Boolean isActive;

    @Column(name = "\"createAt\"")
    private ZonedDateTime createAt;
}