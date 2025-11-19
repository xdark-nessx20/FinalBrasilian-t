package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BaggageRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.BaggageService;
import co.edu.unimagdalena.finalbrasiliant.services.ConfigService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.BaggageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BaggageServiceImpl implements BaggageService {

    private final BaggageRepository baggageRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final BaggageMapper mapper;
    private final ConfigService configService;

    @Override
    @Transactional
    public BaggageResponse create(Long ticketId, BaggageCreateRequest request) {
        var ticket = ticketRepo.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(ticketId)));
        var baggage = mapper.toEntity(request);
        baggage.setTicket(ticket);
        baggage.setTagCode(generateTagCode());

        var weightDiff = baggage.getWeightKg().subtract(configService.getValue("weight.limit"));
        if (weightDiff.compareTo(BigDecimal.ZERO) > 0) {
            baggage.setFee(weightDiff.multiply(configService.getValue("baggage.weight.fee")));
        }

        return mapper.toResponse(baggageRepo.save(baggage));
    }

    private String generateTagCode(){
        var uuid = UUID.randomUUID().toString().replace("-","").toUpperCase();
        return "BAG-" + uuid.substring(0, 8);
    }

    @Override
    public BaggageResponse get(Long id) {
        return baggageRepo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));
    }

    @Override
    @Transactional
    public BaggageResponse update(Long id, BaggageUpdateRequest request) {
        var baggage = baggageRepo.findById(id).orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));
        mapper.patch(baggage, request);
        return mapper.toResponse(baggageRepo.save(baggage));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        baggageRepo.deleteById(id);
    }

    @Override
    public BaggageResponse getByTagCode(String tagCode) {
        return baggageRepo.findByTagCode(tagCode).map(mapper::toResponse).orElseThrow(
                () -> new NotFoundException("Baggage with tag %s not found".formatted(tagCode))
        );
    }

    @Override
    public List<BaggageResponse> listByPassenger(Long passengerId) {
        userRepo.findById(passengerId).orElseThrow(() -> new NotFoundException("Passenger %d not found".formatted(passengerId)));
        return baggageRepo.findByTicket_Passenger_Id(passengerId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<BaggageResponse> listByTicket(Long ticketId) {
        ticketRepo.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(ticketId)));
        return baggageRepo.findByTicket_Id(ticketId).stream().map((mapper::toResponse)).toList();
    }

    @Override
    public Page<BaggageResponse> listByWeightKg(BigDecimal weightKg, Boolean greaterThanEq, Pageable pageable) {
        if (greaterThanEq) return baggageRepo.findByWeightKgGreaterThanEqual(weightKg, pageable).map(mapper::toResponse);
        else return baggageRepo.findByWeightKgLessThanEqual(weightKg, pageable).map(mapper::toResponse);
    }
}
