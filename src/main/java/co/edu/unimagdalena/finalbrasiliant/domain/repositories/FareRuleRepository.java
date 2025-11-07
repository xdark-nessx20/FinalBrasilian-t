package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.FareRule;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {
    Optional<FareRule> findByRouteIdAndFromStopIdAndToStopId(Long routeId, Long fromStopId, Long toStopId);
    List<FareRule> findByRoute_Id(Long routeId);
    List<FareRule> findByFromStop_Id(Long fromStopId);
    List<FareRule> findByToStop_Id(Long toStopId);
    List<FareRule> findByDynamicPricing(DynamicPricing dynamicPricing);
    List<FareRule> findByRouteIdAndDynamicPricing(Long routeId, DynamicPricing dynamicPricing);
    boolean existsByRouteIdAndFromStopIdAndToStopId(Long routeId, Long fromStopId, Long toStopId);

    // Buscar tarifas con detalles completos
    @EntityGraph(attributePaths = {"route", "fromStop", "toStop"})
    @Query("""
        SELECT f FROM FareRule f 
        WHERE f.id = :fareRuleId
    """)
    Optional<FareRule> findByIdWithAllDetails(@Param("fareRuleId") Long fareRuleId);
}

