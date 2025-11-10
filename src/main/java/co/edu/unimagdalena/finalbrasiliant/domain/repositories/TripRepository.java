package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByRoute_Id(Long routeId);
    Page<Trip> findAllByRoute_Id(Long routeId, Pageable page);
    Optional<Trip> findByBus_Id(Long busId);
    Page<Trip> findAllByBus_Id(Long busId, Pageable page);
    Page<Trip> findAllByDepartureAtBetween(OffsetDateTime start, OffsetDateTime end, Pageable page);
    Page<Trip> findAllByArrivalETABetween(OffsetDateTime start, OffsetDateTime end, Pageable page);
    Page<Trip> findAllByStatus(TripStatus status, Pageable page);
    List<Trip> findAllByRoute_IdAndStatus(Long routeId, TripStatus status);
    Page<Trip> findAllByDate(LocalDate date, Pageable page);
}
