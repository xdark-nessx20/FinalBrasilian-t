package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Seat;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;

import java.util.List;


public interface SeatRepository extends JpaRepository<Seat, Long>{
	List<Seat> findAllByBus_Id(Long busId);
	Optional<Seat> findByNumberAndBus_Id(String number, Long bus_id);
	List<Seat> findAllByType(SeatType type);
}
