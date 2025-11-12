package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Ticket;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    Optional<Ticket> findByTrip_IdAndSeatNumber(Long tripId, String seatNumber);

    @EntityGraph(attributePaths = {"fromStop", "toStop", "passenger", "trip"})
    @Query("SELECT T FROM Ticket T WHERE T.qrCode = :qrCode")
    Optional<Ticket> findByQrCode(String qrCode);

    Page<Ticket> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    Page<Ticket> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    @Query("""
        SELECT T FROM Ticket T
        WHERE (:fromId IS NULL OR T.fromStop.id = :fromId) AND (:toId IS NULL OR T.toStop.id = :toId)
    """)
    List<Ticket> findAllByStretch(@Param("fromId") Long fromId, @Param("toId") Long toId);

    List<Ticket> findByPassenger_Id(Long passengerId);
    List<Ticket> findByTrip_Id(Long tripId);

    @Query("""
        SELECT COUNT(DISTINCT t) FROM Ticket t
        WHERE t.status = :status AND (COALESCE(:start, NULL) IS NULL OR t.createdAt >= :start)
            AND (COALESCE(:end, NULL) IS NULL OR t.createdAt <= :end)
    """)
    long countByStatusAndOptionalDateRange(@Param("status") TicketStatus status, @Param("start") OffsetDateTime start,
                                           @Param("end") OffsetDateTime end);

    @Query("SELECT SUM(T.price) FROM Ticket T WHERE T.status = :status")
    BigDecimal sumPriceByStatus(@Param("status") TicketStatus status);

    @Query("""
        SELECT SUM(T.price) FROM Ticket T
        WHERE T.passenger.id = :passengerId AND T.status = 'SOLD'
    """)
    BigDecimal sumPriceByPassenger_Id(@Param("passengerId") Long passengerId);

    @Query("""
        SELECT CASE WHEN COUNT(T) > 0 THEN TRUE ELSE FALSE END
        FROM Ticket T LEFT JOIN Stop FS ON FS.id = T.fromStop.id LEFT JOIN Stop TS ON TS.id = T.toStop.id
        WHERE T.trip.id = :tripId AND T.seatNumber = :seatNumber AND T.status IN ('SOLD', 'USED')
            AND ((:fromStopOrder BETWEEN FS.stopOrder AND TS.stopOrder) OR (:toStopOrder BETWEEN FS.stopOrder AND TS.stopOrder))
    """)
    boolean existsOverlap(@Param("tripId") Long tripId, @Param("seatNumber") String seatNumber,
                          @Param("fromStopOrder") Integer fromStopOrder, @Param("toStopOrder") Integer toStopOrder);

    @Query("""
        SELECT T FROM Ticket T JOIN Trip Tr ON Tr.id = T.id
        WHERE T.status = 'SOLD' AND (FUNCTION('EXTRACT', 'EPOCH', (CURRENT_TIMESTAMP - Tr.departureAt)) /60) <= 5
    """)
    List<Ticket> findByPassengerNoShow();
}
