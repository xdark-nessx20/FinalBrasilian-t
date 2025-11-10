package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BaggageService {
    BaggageResponse create(BaggageCreateRequest request);
    BaggageResponse get(Long id);
    BaggageResponse update(Long id, BaggageUpdateRequest request);
    void delete(Long id);

    BaggageResponse getByTagCode(String tagCode);
    List<BaggageResponse> listByPassenger(Long passengerId);
    Page<BaggageResponse> listByWeightKg(BigDecimal weightKg, Boolean greaterThanEq, Pageable pageable);
}
