package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.SeatHold;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    List<SeatHold> findByUser_Id(Long userId);
    List<SeatHold> findByTrip_Id(Long tripId);
    List<SeatHold> findByStatus(SeatHoldStatus status);
    List<SeatHold> findByTripIdAndUserIdAndStatus(Long tripId, Long userId, SeatHoldStatus status);
    boolean existsByTripIdAndSeatNumberAndStatus(Long tripId, String seatNumber, SeatHoldStatus status);
    long countByTripIdAndStatus(Long tripId, SeatHoldStatus status);

    // Buscar holds expirados
    @Query("""
        SELECT sh FROM SeatHold sh 
        WHERE sh.status = 'HOLD' 
        AND sh.expiresAt < :now
    """)
    List<SeatHold> findExpiredHolds(@Param("now") OffsetDateTime now);

}
