package co.edu.unimagdalena.finalbrasiliant.services.impl;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ParcelDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.ParcelRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.ParcelService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.ParcelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepo;
    private final StopRepository stopRepo;
    private final ParcelMapper mapper;

    @Override
    @Transactional
    public ParcelResponse create(ParcelCreateRequest request) {
        var fromStop = stopRepo.findById(request.fromStopId()).orElseThrow(
                () -> new NotFoundException("Stop %d not found".formatted(request.fromStopId()))
        );
        var toStop = stopRepo.findById(request.toStopId()).orElseThrow(
                () -> new NotFoundException("Stop %d not found".formatted(request.toStopId()))
        );
        var parcel = mapper.toEntity(request);
        parcel.setFromStop(fromStop);
        parcel.setToStop(toStop);
        parcel.setCode(generateCode());

        return mapper.toResponse(parcelRepo.save(parcel));
    }

    private String generateCode(){
        return "PAR-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
    }

    @Override
    public ParcelResponse get(Long id) {
        return parcelRepo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found.".formatted(id)));
    }

    @Override
    @Transactional
    public ParcelResponse update(Long id, ParcelUpdateRequest request) {
        var parcel = parcelRepo.findById(id).orElseThrow(
                () -> new NotFoundException("Parcel %d not found.".formatted(id))
        );
        mapper.patch(parcel, request);
        if (parcel.getStatus().equals(ParcelStatus.IN_DELIVERY)) //IN_DELIVERY or READY_FOR_PICKUP?
            parcel.setDeliveryOTP(UUID.randomUUID().toString().substring(0, 10).toUpperCase());

        return mapper.toResponse(parcelRepo.save(parcel));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        parcelRepo.deleteById(id);
    }

    @Override
    public ParcelResponse getByCode(String code) {
        return parcelRepo.findByCode(code).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Parcel with code %s not found.".formatted(code)));
    }

    @Override
    public List<ParcelResponse> listBySender(String senderName) {
        return parcelRepo.findBySenderNameIgnoringCase(senderName).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<ParcelResponse> listByReceiver(String senderName) {
        return parcelRepo.findByReceiverNameIgnoringCase(senderName).stream().map(mapper::toResponse).toList();
    }

    @Override
    public ParcelResponse getByDeliveryOTP(String deliveryOTP) {
        return parcelRepo.findByDeliveryOTP(deliveryOTP).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Parcel with DeliveryOTP %s not found.".formatted(deliveryOTP)));
    }

    @Override
    public List<ParcelResponse> listByStretch(Long fromId, Long toId) {
        if (fromId == null && toId == null) throw new IllegalArgumentException("fromId and toId can't be null");

        if (fromId != null) stopRepo.findById(fromId).orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(fromId)));
        if (toId != null) stopRepo.findById(toId).orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(toId)));

        return parcelRepo.findAllByStretch(fromId, toId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public Page<ParcelResponse> listByStatus(ParcelStatus status, Pageable pageable) {
        return parcelRepo.findAllByStatus(status, pageable).map(mapper::toResponse);
    }
}
