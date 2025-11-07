package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "routes")
public class Route {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "route_id")
    private Long id;
	
	@Column(nullable = false, name = "route_code", unique = true)
	private String code;
	
	@Column(nullable = false, name = "route_name", unique = true)
	private String routeName;
	
	@Column(nullable = false)
	private String origin;
	
	@Column(nullable = false)
	private String destination;
	
	@Column(nullable = false, name = "distance_km", precision = 6, scale = 2)
	private BigDecimal distanceKM;

	@Column(nullable = false, name = "duration_min")
	private Integer durationMin;
}
