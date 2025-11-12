package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "trips")
public class Trip {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "trip_id")
    private Long id;
	
	@ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;
	
	@Column(nullable = false)
	private LocalDate date;
	
	@Column(nullable = false, name = "departure_at")
	private OffsetDateTime departureAt;
	
	@Column(nullable = false, name = "arrival_eta")
	private OffsetDateTime arrivalETA;

	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private TripStatus status = TripStatus.SCHEDULED;

}
