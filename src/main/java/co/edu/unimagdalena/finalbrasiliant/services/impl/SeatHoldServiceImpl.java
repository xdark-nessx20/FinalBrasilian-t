package co.edu.unimagdalena.finalbrasiliant.services.impl;

import  co.edu.unimagdalena.finalbrasiliant.api.dto.SeatHoldDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.SeatHoldRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.SeatHoldService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.SeatHoldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {

    private final SeatHoldRepository seatHoldRepo;
    private final TripRepository tripRepo;
    private final UserRepository userRepo;
    private final SeatHoldMapper mapper;

    private static final int DEFAULT_HOLD_MINUTES = 10;


    @Override
    @Transactional
    public SeatHoldResponse create(SeatHoldCreateRequest request) {
        var trip = tripRepo.findById(request.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(request.tripId())));

        var passenger = userRepo.findById(request.passengerId())
                .orElseThrow(() -> new NotFoundException("Passenger %d not found".formatted(request.passengerId())));

        if (seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(request.tripId(), request.seatNumber(), SeatHoldStatus.HOLD)) {
            throw new AlreadyExistsException("Seat %s already reserved by another passenger on trip %d"
                    .formatted(request.seatNumber(), request.tripId()));
        }

        if (seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(request.tripId(), request.seatNumber(), SeatHoldStatus.SOLD)) {
            throw new AlreadyExistsException("Seat %s sold on trip %d"
                    .formatted(request.seatNumber(), request.tripId()));
        }

        var seatHold = mapper.toEntity(request);
        seatHold.setTrip(trip);
        seatHold.setPassenger(passenger);

        // calcular tiempo de expiración como 10 minutos desde ahora
        seatHold.setExpiresAt(OffsetDateTime.now().plusMinutes(DEFAULT_HOLD_MINUTES));

        //seatHold.setStatus(SeatHoldStatus.HOLD);

        var saved = seatHoldRepo.save(seatHold);
        return mapper.toResponse(saved);
    }

    @Override
    public SeatHoldResponse get(Long id) {
        return seatHoldRepo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Reservation %d not found".formatted(id)));
    }

    @Override
    @Transactional
    public SeatHoldResponse update(Long id, SeatHoldUpdateRequest request) {
        var seatHold = seatHoldRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation %d not found".formatted(id)));

        if (request.seatNumber() != null && !request.seatNumber().equals(seatHold.getSeatNumber())) {
            if (seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(
                    seatHold.getTrip().getId(),
                    request.seatNumber(),
                    SeatHoldStatus.HOLD)) {
                throw new AlreadyExistsException("Seat %s already reserved".formatted(request.seatNumber()));
            }
        }

        if (request.tripId() != null && !request.tripId().equals(seatHold.getTrip().getId())) {
            var newTrip = tripRepo.findById(request.tripId())
                    .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(request.tripId())));
            seatHold.setTrip(newTrip);
        }

        if (request.passengerId() != null && !request.passengerId().equals(seatHold.getPassenger().getId())) {
            var newPassenger = userRepo.findById(request.passengerId())
                    .orElseThrow(() -> new NotFoundException("Passenger %d not found".formatted(request.passengerId())));
            seatHold.setPassenger(newPassenger);
        }

        mapper.patch(seatHold, request);
        return mapper.toResponse(seatHoldRepo.save(seatHold));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!seatHoldRepo.existsById(id)) {
            throw new NotFoundException("Reservation %d not found".formatted(id));
        }
        seatHoldRepo.deleteById(id);
    }

    @Override
    public List<SeatHoldResponse> listByPassenger(Long passengerId) {
        userRepo.findById(passengerId)
                .orElseThrow(() -> new NotFoundException("Passenger %d not found".formatted(passengerId)));

        return seatHoldRepo.findByPassenger_Id(passengerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<SeatHoldResponse> listByTrip(Long tripId) {
        tripRepo.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

        return seatHoldRepo.findByTrip_Id(tripId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<SeatHoldResponse> listByStatus(SeatHoldStatus status) {
        return seatHoldRepo.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<SeatHoldResponse> listByTripAndPassenger(Long tripId, Long passengerId, SeatHoldStatus status) {
        tripRepo.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

        userRepo.findById(passengerId)
                .orElseThrow(() -> new NotFoundException("Passenger %d not found".formatted(passengerId)));

        return seatHoldRepo.findByTrip_IdAndPassenger_IdAndStatus(tripId, passengerId, status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public boolean existsActiveSeatHold(Long tripId, String seatNumber) {
        return seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(tripId, seatNumber, SeatHoldStatus.HOLD);
    }

    @Override
    public List<SeatHoldResponse> listExpiredHolds() {
        var now = OffsetDateTime.now();
        return seatHoldRepo.findExpiredHolds(now)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    @Transactional
    public void expireHolds() {
        var now = OffsetDateTime.now();
        var expiredHolds = seatHoldRepo.findExpiredHolds(now);

        if (expiredHolds.isEmpty()) return;

        expiredHolds.forEach(hold -> hold.setStatus(SeatHoldStatus.EXPIRED));
        seatHoldRepo.saveAll(expiredHolds);
    }
}
