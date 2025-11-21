package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.PaymentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TicketStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final TicketRepository ticketRepo;

    @Override
    @Transactional
    public PaymentResponse confirmPayment(PaymentRequest request) {
        var ticket = ticketRepo.findByIdWithPassenger(request.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(request.ticketId())));
        ticket.setStatus(TicketStatus.SOLD);
        ticketRepo.save(ticket);
        return new PaymentResponse(
                generatePaymentId(), request.ticketId(), request.paymentMethod(), request.amount(),
                OffsetDateTime.now(), ticket.getPassenger().getUserName()
        );
    }

    private String generatePaymentId() {
        return UUID.randomUUID().toString().toUpperCase().substring(0, 10);
    }
}
