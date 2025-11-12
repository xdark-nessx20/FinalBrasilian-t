package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ParcelDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Parcel;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.ParcelRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.ParcelServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.ParcelMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    @Mock
    private ParcelRepository parcelRepo;

    @Mock
    private StopRepository stopRepo;

    @Spy
    private ParcelMapper mapper = Mappers.getMapper(ParcelMapper.class);

    @InjectMocks
    private ParcelServiceImpl service;

    @Test
    void shouldCreateParcelSuccessfully() {
        // Given
        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(2L).name("Medellín").stopOrder(3).build();

        var request = new ParcelCreateRequest(
                1L,
                2L,
                new BigDecimal("25000.00"),
                "Juan Pérez",
                "3001234567",
                "María García",
                "3007654321"
        );

        when(stopRepo.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(2L)).thenReturn(Optional.of(toStop));

        when(parcelRepo.save(any(Parcel.class))).thenAnswer(inv -> {
            Parcel p = inv.getArgument(0);
            p.setId(10L);
            p.setCode("PARCEL001");
            p.setDeliveryOTP("123456");
            p.setStatus(ParcelStatus.CREATED);
            return p;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo("PARCEL001");
        assertThat(response.senderName()).isEqualTo("Juan Pérez");
        assertThat(response.senderPhone()).isEqualTo("3001234567");
        assertThat(response.receiverName()).isEqualTo("María García");
        assertThat(response.receiverPhone()).isEqualTo("3007654321");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(response.fromStop().id()).isEqualTo(1L);
        assertThat(response.toStop().id()).isEqualTo(2L);

        verify(parcelRepo).save(any(Parcel.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFromStopNotExists() {
        // Given
        var request = new ParcelCreateRequest(
                99L, 2L, new BigDecimal("25000.00"),
                "Juan", "3001234567", "María", "3007654321"
        );

        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 99 not found");

        verify(parcelRepo, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenToStopNotExists() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        var request = new ParcelCreateRequest(
                1L, 99L, new BigDecimal("25000.00"),
                "Juan", "3001234567", "María", "3007654321"
        );

        when(stopRepo.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 99 not found");

        verify(parcelRepo, never()).save(any());
    }

    @Test
    void shouldGetParcelById() {
        // Given
        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(2L).name("Medellín").stopOrder(3).build();

        var parcel = Parcel.builder()
                .id(10L)
                .code("PARCEL001")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("25000.00"))
                .status(ParcelStatus.IN_TRANSIT)
                .senderName("Juan Pérez")
                .senderPhone("3001234567")
                .receiverName("María García")
                .receiverPhone("3007654321")
                .deliveryOTP("123456")
                .build();

        when(parcelRepo.findById(10L)).thenReturn(Optional.of(parcel));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo("PARCEL001");
        assertThat(response.senderName()).isEqualTo("Juan Pérez");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentParcel() {
        // Given
        when(parcelRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Parcel 99 not found");
    }

    @Test
    void shouldUpdateParcel() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .code("PARCEL001")
                .price(new BigDecimal("25000.00"))
                .receiverName("María García")
                .receiverPhone("3007654321")
                .status(ParcelStatus.CREATED)
                .build();

        var updateRequest = new ParcelUpdateRequest(
                new BigDecimal("30000.00"),
                ParcelStatus.IN_TRANSIT,
                "3009999999",
                "Nuevo Receptor", "3101112133"
        );

        when(parcelRepo.findById(10L)).thenReturn(Optional.of(parcel));
        when(parcelRepo.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(response.receiverName()).isEqualTo("Nuevo Receptor");
        assertThat(response.receiverPhone()).isEqualTo("3009999999");
        assertThat(response.status()).isEqualTo(ParcelStatus.IN_TRANSIT);

        verify(parcelRepo).save(any(Parcel.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentParcel() {
        // Given
        var updateRequest = new ParcelUpdateRequest(
                new BigDecimal("30000.00"), null, null, null, null
        );
        when(parcelRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Parcel 99 not found");

        verify(parcelRepo, never()).save(any());
    }

    @Test
    void shouldDeleteParcel() {
        // When
        service.delete(10L);

        // Then
        verify(parcelRepo).deleteById(10L);
    }

    @Test
    void shouldGetParcelByCode() {
        // Given
        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(2L).name("Medellín").stopOrder(3).build();

        var parcel = Parcel.builder()
                .id(10L)
                .code("PARCEL001")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("25000.00"))
                .status(ParcelStatus.IN_TRANSIT)
                .senderName("Juan Pérez")
                .senderPhone("3001234567")
                .receiverName("María García")
                .receiverPhone("3007654321")
                .deliveryOTP("123456")
                .build();

        when(parcelRepo.findByCode("PARCEL001")).thenReturn(Optional.of(parcel));

        // When
        var response = service.getByCode("PARCEL001");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo("PARCEL001");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCodeNotExists() {
        // Given
        when(parcelRepo.findByCode("INVALID")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByCode("INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Parcel with code INVALID not found");
    }

    @Test
    void shouldListParcelsBySenderName() {
        // Given
        var parcel1 = Parcel.builder()
                .id(1L)
                .code("PARCEL001")
                .senderName("Juan Pérez")
                .fromStop(Stop.builder().id(1L).name("Bogotá").stopOrder(1).build())
                .toStop(Stop.builder().id(2L).name("Medellín").stopOrder(3).build())
                .build();

        var parcel2 = Parcel.builder()
                .id(2L)
                .code("PARCEL002")
                .senderName("Juan Pérez")
                .fromStop(Stop.builder().id(1L).name("Bogotá").stopOrder(1).build())
                .toStop(Stop.builder().id(2L).name("Medellín").stopOrder(3).build())
                .build();

        when(parcelRepo.findBySenderNameIgnoringCase("Juan Pérez"))
                .thenReturn(List.of(parcel1, parcel2));

        // When
        var result = service.listBySender("Juan Pérez");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.senderName().equalsIgnoreCase("Juan Pérez"));
    }

    @Test
    void shouldReturnEmptyListWhenNoParcelsBySender() {
        // Given
        when(parcelRepo.findBySenderNameIgnoringCase("Nadie"))
                .thenReturn(List.of());

        // When
        var result = service.listBySender("Nadie");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldListParcelsByReceiverName() {
        // Given
        var parcel1 = Parcel.builder()
                .id(1L)
                .code("PARCEL001")
                .receiverName("María García")
                .fromStop(Stop.builder().id(1L).name("Bogotá").stopOrder(1).build())
                .toStop(Stop.builder().id(2L).name("Medellín").stopOrder(3).build())
                .build();

        when(parcelRepo.findByReceiverNameIgnoringCase("María García"))
                .thenReturn(List.of(parcel1));

        // When
        var result = service.listByReceiver("María García");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).receiverName()).isEqualTo("María García");
    }

    @Test
    void shouldGetParcelByDeliveryOTP() {
        // Given
        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(2L).name("Medellín").stopOrder(3).build();

        var parcel = Parcel.builder()
                .id(10L)
                .code("PARCEL001")
                .fromStop(fromStop)
                .toStop(toStop)
                .deliveryOTP("123456")
                .senderName("Juan")
                .receiverName("María")
                .build();

        when(parcelRepo.findByDeliveryOTP("123456")).thenReturn(Optional.of(parcel));

        // When
        var response = service.getByDeliveryOTP("123456");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.deliveryOTP()).isEqualTo("123456");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeliveryOTPNotExists() {
        // Given
        when(parcelRepo.findByDeliveryOTP("INVALID")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByDeliveryOTP("INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Parcel with DeliveryOTP INVALID not found");
    }

    @Test
    void shouldListParcelsByStretch() {
        // Given
        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(2L).name("Medellín").stopOrder(3).build();

        var parcel1 = Parcel.builder()
                .id(1L)
                .code("PARCEL001")
                .fromStop(fromStop)
                .toStop(toStop)
                .senderName("Juan")
                .receiverName("María")
                .build();

        var parcel2 = Parcel.builder()
                .id(2L)
                .code("PARCEL002")
                .fromStop(fromStop)
                .toStop(toStop)
                .senderName("Pedro")
                .receiverName("Ana")
                .build();

        when(stopRepo.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelRepo.findAllByStretch(1L, 2L)).thenReturn(List.of(parcel1, parcel2));

        // When
        var result = service.listByStretch(1L, 2L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ParcelResponse::id)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFromStopNotExistsForStretch() {
        // Given
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.listByStretch(99L, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 99 not found");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenToStopNotExistsForStretch() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        when(stopRepo.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.listByStretch(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 99 not found");
    }

    @Test
    void shouldListParcelsByStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(2L).name("Medellín").stopOrder(3).build();

        var parcel1 = Parcel.builder()
                .id(1L)
                .code("PARCEL001")
                .status(ParcelStatus.IN_TRANSIT)
                .fromStop(fromStop)
                .toStop(toStop)
                .senderName("Juan")
                .receiverName("María")
                .build();

        var parcel2 = Parcel.builder()
                .id(2L)
                .code("PARCEL002")
                .status(ParcelStatus.IN_TRANSIT)
                .fromStop(fromStop)
                .toStop(toStop)
                .senderName("Pedro")
                .receiverName("Ana")
                .build();

        var page = new PageImpl<>(List.of(parcel1, parcel2));
        when(parcelRepo.findAllByStatus(ParcelStatus.IN_TRANSIT, pageable)).thenReturn(page);

        // When
        var result = service.listByStatus(ParcelStatus.IN_TRANSIT, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.status() == ParcelStatus.IN_TRANSIT);
    }

    @Test
    void shouldReturnEmptyPageWhenNoParcelsByStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        var emptyPage = new PageImpl<Parcel>(List.of());
        when(parcelRepo.findAllByStatus(ParcelStatus.DELIVERED, pageable)).thenReturn(emptyPage);

        // When
        var result = service.listByStatus(ParcelStatus.DELIVERED, pageable);

        // Then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldHandleCaseInsensitiveSearchForSender() {
        // Given
        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(2L).name("Medellín").stopOrder(3).build();

        var parcel = Parcel.builder()
                .id(1L)
                .code("PARCEL001")
                .senderName("Juan Pérez")
                .fromStop(fromStop)
                .toStop(toStop)
                .receiverName("María")
                .build();

        when(parcelRepo.findBySenderNameIgnoringCase("JUAN PÉREZ"))
                .thenReturn(List.of(parcel));

        // When
        var result = service.listBySender("JUAN PÉREZ");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).senderName()).isEqualTo("Juan Pérez");
        verify(parcelRepo).findBySenderNameIgnoringCase("JUAN PÉREZ");
    }

    @Test
    void shouldHandleCaseInsensitiveSearchForReceiver() {
        // Given
        var fromStop = Stop.builder().id(1L).name("Bogotá").stopOrder(1).build();
        var toStop = Stop.builder().id(2L).name("Medellín").stopOrder(3).build();

        var parcel = Parcel.builder()
                .id(1L)
                .code("PARCEL001")
                .receiverName("María García")
                .fromStop(fromStop)
                .toStop(toStop)
                .senderName("Juan")
                .build();

        when(parcelRepo.findByReceiverNameIgnoringCase("maría garcía"))
                .thenReturn(List.of(parcel));

        // When
        var result = service.listByReceiver("maría garcía");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).receiverName()).isEqualTo("María García");
        verify(parcelRepo).findByReceiverNameIgnoringCase("maría garcía");
    }
}