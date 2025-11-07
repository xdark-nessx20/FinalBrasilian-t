package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Baggage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BaggageRepository extends JpaRepository<Baggage,Long> {
    Optional<Baggage> findByTagCode(String tagCode);
    Page<Baggage> findByWeightKgGreaterThanEqual(BigDecimal weightKg, Pageable pageable);
    Page<Baggage> findByWeightKgLessThanEqual(BigDecimal weightKg, Pageable pageable);
    List<Baggage> findByTicket_Passenger_Id(Long passengerId);

    @Query("SELECT B FROM Baggage B JOIN FETCH B.ticket WHERE B.id = :baggageId")
    Optional<Baggage> findByIdWithAllDetails(Long baggageId);
}
