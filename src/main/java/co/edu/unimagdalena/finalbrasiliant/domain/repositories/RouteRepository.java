package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;

import java.util.List;
import java.math.BigDecimal;



public interface RouteRepository extends JpaRepository<Route, Long> {
	Optional<Route> findByCode(String code);
	List<Route> findAllByDestinationIgnoreCase(String destination);
	List<Route> findAllByOriginIgnoreCase(String origin);
	Optional<Route> findByRouteName(String routeName);
	List<Route> findAllByDurationMinGreaterThan(Integer minDuration);
	List<Route> findAllByDurationMinBetween(Integer minDuration, Integer maxDuration);
	List<Route> findAllByDistanceKMLessThan(BigDecimal maxDistance);
	List<Route> findAllByDistanceKMBetween(BigDecimal minDistance, BigDecimal maxDistance);
	Page<Route> findAllByOriginAndDestination(String origin, String destination, Pageable pageable);
}
