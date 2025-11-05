package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "seats")
public class Seat {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "seat_id")
    private Long id;
	
	@ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;
	
	@Column(nullable = false)
	private String number;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SeatType type;
}
