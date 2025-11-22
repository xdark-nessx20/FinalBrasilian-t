package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.*;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.ItCantBeException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.ConfigService;
import co.edu.unimagdalena.finalbrasiliant.services.NotificationService;
import co.edu.unimagdalena.finalbrasiliant.services.TicketService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepo;
    private final StopRepository stopRepo;
    private final TripRepository tripRepo;
    private final UserRepository userRepo;
    private final SeatRepository seatRepo;
    private final SeatHoldRepository seatHoldRepo;
    private final FareRuleRepository fareRuleRepo;
    private final TicketMapper mapper;
    //private final NotificationService notif;
    private final ConfigService configs;

    @Override
    @Transactional
    public TicketResponse create(Long tripId, TicketCreateRequest request) {
        var trip = tripRepo.findById(tripId).orElseThrow(
                () -> new NotFoundException("Trip %d not found".formatted(tripId)));
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

        var fareRule = fareRuleRepo.findByRouteIdAndFromStopIdAndToStopId(
            trip.getRoute().getId(), fromStop.getId(), toStop.getId()).orElseThrow(
            () -> new NotFoundException("There's no fare rule from '%s' to '%s'.".formatted(fromStop.getName(), toStop.getName()))
        );

        var ticket = mapper.toEntity(request);
        ticket.setTrip(trip);
        ticket.setFromStop(fromStop);
        ticket.setToStop(toStop);
        ticket.setPassenger(passenger);
        ticket.setPrice(fareRule.getBasePrice().multiply(
                BigDecimal.ONE.subtract(fareRule.getDiscounts().getOrDefault(request.type(), BigDecimal.ZERO))
        ));
        ticket.setQrCode(generateQRCode());

        if (ticketRepo.wasNoShowTicket(tripId, ticket.getSeatNumber(),  fromStop.getStopOrder(), toStop.getStopOrder()))
            ticket.setPrice(ticket.getPrice().multiply(
                    configs.getValue("ticket.no-show.fee").add(BigDecimal.valueOf(1))
            ));

        var saved = ticketRepo.save(ticket);
        //notif.sendTicketConfirmation(passenger.getPhone(), passenger.getUserName(), saved.getId(), saved.getSeatNumber(), saved.getQrCode());

        return mapper.toResponse(saved, true);
    }

    private String generateQRCode(){
        return "TKT-" + UUID.randomUUID().toString().substring(0, 11).toUpperCase();
    }

    @Override
    public TicketResponse get(Long id) {
        var ticket = ticketRepo.findById(id).orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        return mapper.toResponse(ticket, true);
    }

    @Override
    @Transactional
    public TicketResponse update(Long id, TicketUpdateRequest request) {
        var ticket = ticketRepo.findByIdWithAll(id).orElseThrow(
                () -> new NotFoundException("Ticket %d not found".formatted(id))
        );
        mapper.patch(ticket, request);
        var updated = ticketRepo.save(ticket);
        var passenger = updated.getPassenger();

        /*if (updated.getStatus().equals(TicketStatus.USED))
            notif.sendTicketUsed(passenger.getPhone(), passenger.getUserName(), id, updated.getTrip().getRoute().getRouteName());*/

        return mapper.toResponse(updated, false);
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
        var ticket = ticketRepo.findByTrip_IdAndSeatNumber(tripId, seatNumber)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(tripId)));
        return mapper.toResponse(ticket, false);

    }

    @Override
    public TicketResponse getByQRCode(String qrCode) {
        var ticket = ticketRepo.findByQrCode(qrCode)
                .orElseThrow(() -> new NotFoundException("Ticket '%s' not found".formatted(qrCode)));
        return mapper.toResponse(ticket, false);
    }

    @Override
    public Page<TicketResponse> listByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepo.findByStatus(status, pageable).map(ticket -> mapper.toResponse(ticket, false));
    }

    @Override
    public Page<TicketResponse> listByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable) {
        return ticketRepo.findByPaymentMethod(paymentMethod, pageable).map(ticket -> mapper.toResponse(ticket, false));
    }

    @Override
    public Page<TicketResponse> listByCreatedAt(OffsetDateTime start, OffsetDateTime end, Pageable pageable) {
        if (end.isBefore(start)) throw new IllegalArgumentException("End time can't be before start time");
        return ticketRepo.findByCreatedAtBetween(start, end, pageable).map(ticket -> mapper.toResponse(ticket, false));
    }

    @Override
    public List<TicketResponse> listByPassenger(Long passengerId) {
        userRepo.findById(passengerId).orElseThrow(() -> new NotFoundException("Passenger %d not found".formatted(passengerId)));
        return ticketRepo.findByPassenger_Id(passengerId).stream().map(ticket -> mapper.toResponse(ticket, false)).toList();
    }

    @Override
    public List<TicketResponse> listByTrip(Long tripId) {
        tripRepo.findById(tripId).orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));
        return ticketRepo.findByTrip_Id(tripId).stream().map(ticket -> mapper.toResponse(ticket, false)).toList();
    }

    @Override
    public List<TicketResponse> listByStretch(Long fromId, Long toId) {
        if (fromId == null && toId == null) throw new IllegalArgumentException("From id and To id can't be null");

        if (fromId != null) stopRepo.findById(fromId).orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(fromId)));
        if (toId != null) stopRepo.findById(toId).orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(toId)));

        return ticketRepo.findAllByStretch(fromId, toId).stream().map(ticket -> mapper.toResponse(ticket, false)).toList();
    }

    @Override
    //This will execute for each minute after 30 seconds of starting the app
    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    @Transactional
    public void setTicketsNoShow() {
        var now = OffsetDateTime.now();
        var noShow = ticketRepo.findAll().stream().filter(ticket -> ticket.getStatus().equals(TicketStatus.SOLD) &&
                (parseToMinutes(ticket.getTrip().getDepartureAt()) - parseToMinutes(now)) <= 5).toList();

        if (noShow.isEmpty()) return;

        noShow.forEach(t -> t.setStatus(TicketStatus.NO_SHOW));
        ticketRepo.saveAll(noShow);
    }

    private long parseToMinutes(OffsetDateTime date) {
        return date.toEpochSecond()/60;
    }

    @Override
    @Transactional
    public TicketResponse cancelTicket(Long id) {
        var ticket = ticketRepo.findByIdWithAll(id).orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        if (ticket.getTrip().getStatus().equals(TripStatus.DEPARTED))
            throw new ItCantBeException("You can't cancel your ticket 'cause the trip already departed.");
        if (ticket.getStatus().equals(TicketStatus.CANCELLED))
            throw new AlreadyExistsException("Ticket %d is already cancelled");

        ticket.setStatus(TicketStatus.CANCELLED);
        var updated = ticketRepo.save(ticket);
        var passenger = updated.getPassenger();

        var tripDeparture = updated.getTrip().getDepartureAt();
        var diff = Duration.between(OffsetDateTime.now(), tripDeparture).toHours();
        var percentToRefund = refundPolicy(diff);
        /*notif.sendTicketCancellation(passenger.getPhone(), passenger.getUserName(),
                id, updated.getPrice().multiply(percentToRefund), updated.getPaymentMethod());*/
        return mapper.toResponse(updated, false);
    }

    private BigDecimal refundPolicy(long hoursBeforeDepart){
        if (hoursBeforeDepart < 3) return configs.getValue("last.refund.percent");
        else if (hoursBeforeDepart < 12) return configs.getValue("third.refund.percent");
        else if (hoursBeforeDepart < 24) return configs.getValue("second.refund.percent");
        else return configs.getValue("initial.refund.percent");
    }
}
