package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    Optional<Assignment> findByTrip_Id(Long tripId);
    List<Assignment> findAllByDriver_Id(Long driverId);
    Page<Assignment> findByAssignedAtBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    List<Assignment> findByCheckListOk(Boolean checkListOk);

    @Query("""
        SELECT A FROM Assignment A
        JOIN FETCH A.trip WHERE A.id = :assignmentId
        """)
    Optional<Assignment> findByIdWithAllDetails(@Param("assignmentId") Long assignmentId);

    @Query("""
        SELECT CASE WHEN COUNT(A) > 0 THEN TRUE ELSE FALSE END
        FROM Assignment A JOIN Trip T ON T.id = A.trip.id
        WHERE A.driver.id = :driverId AND :departureNewTrip BETWEEN T.departureAt AND T.arrivalETA
    """)
    boolean driverHasAnotherAssignment(@Param("driverId")  Long driverId, @Param("departureNewTrip") OffsetDateTime departureNewTrip);

    @Query("""
        SELECT CASE WHEN COUNT(A) > 0 THEN TRUE ELSE FALSE END
        FROM Assignment A JOIN Trip T ON T.id = A.trip.id
        WHERE A.dispatcher.id = :dispatcherId AND :departureNewTrip BETWEEN T.departureAt AND T.arrivalETA
    """)
    boolean dispatcherHasAnotherAssignment(@Param("driverId") Long dispatcherId, @Param("departureNewTrip") OffsetDateTime departureNewTrip);
}
