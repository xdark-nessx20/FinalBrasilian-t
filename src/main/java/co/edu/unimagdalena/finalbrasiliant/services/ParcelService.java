package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ParcelDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParcelService {
    ParcelResponse create(ParcelCreateRequest request);
    ParcelResponse get(Long id);
    ParcelResponse update(Long id, ParcelUpdateRequest request);
    void delete(Long id);

    ParcelResponse getByCode(String code);
    List<ParcelResponse> listBySender(String senderName);
    List<ParcelResponse> listByReceiver(String senderName);
    ParcelResponse getByDeliveryOTP(String deliveryOTP);
    List<ParcelResponse> listByStretch(Long fromId, Long toId);
    Page<ParcelResponse> listByStatus(ParcelStatus status, Pageable pageable);
}
