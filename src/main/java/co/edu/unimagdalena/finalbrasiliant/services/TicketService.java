package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.TicketDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.PaymentMethod;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface TicketService {
    TicketResponse create(TicketCreateRequest request);
    TicketResponse get(Long id);
    TicketResponse update(Long id, TicketUpdateRequest request);
    void delete(Long id);

    TicketResponse getByTripSeat(Long tripId, String seatNumber);
    TicketResponse getByQRCode(String qrCode);
    Page<TicketResponse> listByStatus(TicketStatus status, Pageable pageable);
    Page<TicketResponse> listByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    Page<TicketResponse> listByCreatedAt(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    List<TicketResponse> listByPassenger(Long passengerId);
    List<TicketResponse> listByTrip(Long tripId);
    List<TicketResponse> listByStretch(Long fromId, Long toId);

    void setTicketsNoShow();
}
