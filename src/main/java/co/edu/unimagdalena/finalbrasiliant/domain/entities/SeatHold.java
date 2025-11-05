package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "seats_holded")

public class SeatHold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_hold_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(nullable = false, name = "seat_number")
    private String seatNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "passenger_id")
    private User passenger;

    @Column(nullable = false, name = "expires_at")
    private OffsetDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatHoldStatus status;

}
