package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByRoute_Id(Long routeId);
    List<Trip> findAllByRoute_Id(Long routeId);
    List<Trip> findByBus_Id(Long busId);
    List<Trip> findAllByDepartureAtBetween(OffsetDateTime start, OffsetDateTime end);
    List<Trip> findAllByArrivalETABetween(OffsetDateTime start, OffsetDateTime end);
    List<Trip> findAllByStatus(TripStatus status);
    List<Trip> findAllByRoute_IdAndStatus(Long routeId, TripStatus status);
    List<Trip> findAllByDate(LocalDate date);
}
