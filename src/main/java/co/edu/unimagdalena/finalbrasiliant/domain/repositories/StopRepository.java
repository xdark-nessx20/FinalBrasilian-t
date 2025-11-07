package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StopRepository extends JpaRepository<Stop, Long> {
    List<Stop> findByRoute_IdOrderByStopOrderAsc(Long routeId);
    List<Stop> findByNameContainingIgnoreCase(String name);
    Optional<Stop> findFirstByRoute_IdOrderByStopOrderAsc(Long routeId);
    Optional<Stop> findFirstByRoute_IdOrderByStopOrderDesc(Long routeId);
    long countByRoute_Id(Long routeId);
    boolean existsByRouteIdAndNameIgnoreCase(Long routeId, String name);


    Optional<Stop> findByRouteIdAndStopOrder(Long routeId, Integer stopOrder);
    List<Stop> findByRouteIdAndStopOrderBetween(Long routeId, Integer startOrder, Integer endOrder);

    // Obtener parada con detalles de la ruta
    @Query("SELECT s FROM Stop s JOIN FETCH s.route WHERE s.id = :stopId")
    Optional<Stop> findByIdWithRoute(@Param("stopId") Long stopId);

}
