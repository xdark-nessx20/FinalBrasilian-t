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
    Optional<Assignment> findByTripId(Long tripId);
    List<Assignment> findAllByDriverId(Long driverId);
    Page<Assignment> findByAssignedAtBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    List<Assignment> findByCheckListOk(Boolean checkListOk);

    @Query("""
        SELECT A FROM Assignment A
        JOIN FETCH A.trip WHERE A.id = :assignmentId
        """)
    Optional<Assignment> findByIdWithAllDetails(@Param("assignmentId") Long assignmentId);
}
