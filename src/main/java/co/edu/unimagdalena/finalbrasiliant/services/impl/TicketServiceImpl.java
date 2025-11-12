package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.*;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.TicketService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepo;
    private final StopRepository stopRepo;
    private final TripRepository tripRepo;
    private final UserRepository userRepo;
    private final SeatRepository seatRepo;
    private final SeatHoldRepository seatHoldRepo;
    private final TicketMapper mapper;

    @Override
    @Transactional
    public TicketResponse create(TicketCreateRequest request) {
        var trip = tripRepo.findById(request.tripId()).orElseThrow(
                () -> new NotFoundException("Trip %d not found".formatted(request.tripId())));
        var fromStop = stopRepo.findById(request.fromStopId()).orElseThrow(
                () -> new NotFoundException("Stop %d not found".formatted(request.fromStopId())));
        var toStop = stopRepo.findById(request.toStopId()).orElseThrow(
                () -> new NotFoundException("Stop %d not found".formatted(request.toStopId())));

        if (ticketRepo.existsOverlap(trip.getId(), request.seatNumber(), fromStop.getStopOrder(), toStop.getStopOrder()))
            throw new AlreadyExistsException("Already exists a Ticket for the %S seat in the trip %d in the stops what you want."
                    .formatted(request.seatNumber(), trip.getId()));

        var passenger = userRepo.findById(request.passengerId()).orElseThrow(
                () -> new NotFoundException("Passenger %d not found".formatted(request.passengerId())));

        if (seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(trip.getId(), request.seatNumber(), SeatHoldStatus.HOLD)
            && seatHoldRepo.findByTrip_IdAndPassenger_IdAndStatus(trip.getId(), request.passengerId(), SeatHoldStatus.HOLD)
                .stream().noneMatch(sh -> sh.getSeatNumber().equals(request.seatNumber())))
            throw new AlreadyExistsException("The seat %s is hold by another passenger.".formatted(request.seatNumber()));

        var ticket = mapper.toEntity(request);
        ticket.setTrip(trip);
        ticket.setFromStop(fromStop);
        ticket.setToStop(toStop);
        ticket.setPassenger(passenger);

        var saved = ticketRepo.save(ticket);
        //Just for local demo in React front-end
        saved.setQrCode("http://localhost:3000/tickets/%d".formatted(saved.getId()));

        return mapper.toResponse(ticketRepo.save(saved));
    }

    @Override
    public TicketResponse get(Long id) {
        return ticketRepo.findById(id).map(mapper::toResponse).orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
    }

    @Override
    @Transactional
    public TicketResponse update(Long id, TicketUpdateRequest request) {
        var ticket = ticketRepo.findById(id).orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        mapper.patch(ticket, request);
        return mapper.toResponse(ticketRepo.save(ticket));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ticketRepo.deleteById(id);
    }

    @Override
    public TicketResponse getByTripSeat(Long tripId, String seatNumber) {
        var trip = tripRepo.findById(tripId).orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));
        var bus = Optional.ofNullable(trip.getBus());
        if (bus.isEmpty()) throw new NotFoundException("Trip %d hasn't bus assigned yet.".formatted(tripId));

        seatRepo.findByNumberAndBus_Id(seatNumber, bus.get().getId()).orElseThrow(
                () -> new NotFoundException("Seat number %s not found".formatted(seatNumber))
        );
        return ticketRepo.findByTrip_IdAndSeatNumber(tripId, seatNumber).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(tripId)));
    }

    @Override
    //This method could be removed
    public TicketResponse getByQRCode(String qrCode) {
        return null;
    }

    @Override
    public Page<TicketResponse> listByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepo.findByStatus(status, pageable).map(mapper::toResponse);
    }

    @Override
    public Page<TicketResponse> listByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable) {
        return ticketRepo.findByPaymentMethod(paymentMethod, pageable).map(mapper::toResponse);
    }

    @Override
    public Page<TicketResponse> listByCreatedAt(OffsetDateTime start, OffsetDateTime end, Pageable pageable) {
        if (end.isBefore(start)) throw new IllegalArgumentException("End time can't be before start time");
        return null;
    }

    @Override
    public List<TicketResponse> listByPassenger(Long passengerId) {
        userRepo.findById(passengerId).orElseThrow(() -> new NotFoundException("Passenger %d not found".formatted(passengerId)));
        return ticketRepo.findByPassenger_Id(passengerId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<TicketResponse> listByTrip(Long tripId) {
        tripRepo.findById(tripId).orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));
        return ticketRepo.findByTrip_Id(tripId).stream().map((mapper::toResponse)).toList();
    }

    @Override
    public List<TicketResponse> listByStretch(Long fromId, Long toId) {
        stopRepo.findById(fromId).orElseThrow(
                () -> new NotFoundException("Stop %d not found".formatted(fromId))
        );
        stopRepo.findById(toId).orElseThrow(
                () -> new NotFoundException("Stop %d not found".formatted(toId))
        );
        return ticketRepo.findAllByStretch(fromId, toId).stream().map((mapper::toResponse)).toList();
    }

    @Override
    @Scheduled(fixedRate = 60000, initialDelay = 30000) //This will execute for each minute after 30 seconds of starting the app
    public void setTicketsNoShow() {
        var noShow = ticketRepo.findByPassengerNoShow();
        if (noShow.isEmpty()) return;
        noShow.forEach(t -> mapper.patch(t, new TicketUpdateRequest(null, null, null, TicketStatus.NO_SHOW)));
        //And... what now?
    }
}
