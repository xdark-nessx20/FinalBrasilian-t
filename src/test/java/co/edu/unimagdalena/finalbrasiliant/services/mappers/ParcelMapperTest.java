package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.ParcelDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Parcel;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ParcelMapperTest {

    private final ParcelMapper mapper = Mappers.getMapper(ParcelMapper.class);

    @Test
    void toEntity_shouldMapAllowedFieldsOnly() {
        // Given
        var request = new ParcelCreateRequest(
                1L,
                2L,
                new BigDecimal("25000.00"),
                "Juan Pérez",
                "3001234567",
                "María García",
                "3007654321"
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(entity.getSenderName()).isEqualTo("Juan Pérez");
        assertThat(entity.getSenderPhone()).isEqualTo("3001234567");
        assertThat(entity.getReceiverName()).isEqualTo("María García");
        assertThat(entity.getReceiverPhone()).isEqualTo("3007654321");

        // Campos ignorados
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getDeliveryOTP()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getFromStop()).isNull();
        assertThat(entity.getToStop()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Given
        var fromStop = Stop.builder()
                .id(1L)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .build();

        var toStop = Stop.builder()
                .id(2L)
                .name("Terminal Medellín")
                .stopOrder(3)
                .build();

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

        // When
        var response = mapper.toResponse(parcel);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo("PARCEL001");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(response.status()).isEqualTo(ParcelStatus.IN_TRANSIT);
        assertThat(response.senderName()).isEqualTo("Juan Pérez");
        assertThat(response.senderPhone()).isEqualTo("3001234567");
        assertThat(response.receiverName()).isEqualTo("María García");
        assertThat(response.receiverPhone()).isEqualTo("3007654321");
        assertThat(response.deliveryOTP()).isEqualTo("123456");

        assertThat(response.fromStop().id()).isEqualTo(1L);
        assertThat(response.fromStop().name()).isEqualTo("Terminal Bogotá");
        assertThat(response.fromStop().stopOrder()).isEqualTo(1);

        assertThat(response.toStop().id()).isEqualTo(2L);
        assertThat(response.toStop().name()).isEqualTo("Terminal Medellín");
        assertThat(response.toStop().stopOrder()).isEqualTo(3);
    }

    @Test
    void toStopSummary_shouldMapStop() {
        // Given
        var stop = Stop.builder()
                .id(5L)
                .name("Peaje La Línea")
                .stopOrder(2)
                .build();

        // When
        var summary = mapper.toStopSummary(stop);

        // Then
        assertThat(summary.id()).isEqualTo(5L);
        assertThat(summary.name()).isEqualTo("Peaje La Línea");
        assertThat(summary.stopOrder()).isEqualTo(2);
    }

    @Test
    void toStopSummary_shouldHandleNull() {
        // When
        var summary = mapper.toStopSummary(null);

        // Then
        assertThat(summary).isNull();
    }

    @Test
    void patch_shouldUpdateAllowedFields() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .code("PARCEL001")
                .senderName("Juan Original")
                .senderPhone("3001234567")
                .receiverName("María Original")
                .receiverPhone("3007654321")
                .price(new BigDecimal("25000.00"))
                .status(ParcelStatus.CREATED)
                .deliveryOTP("123456")
                .build();

        var updateRequest = new ParcelUpdateRequest(
                new BigDecimal("30000.00"),
                ParcelStatus.IN_TRANSIT,
                "3009999999", null, null
        );

        // When
        mapper.patch(parcel, updateRequest);

        // Then
        assertThat(parcel.getPrice()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(parcel.getReceiverName()).isEqualTo("Pedro Actualizado");
        assertThat(parcel.getReceiverPhone()).isEqualTo("3009999999");
        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.IN_TRANSIT);

        // Campos ignorados (no deben cambiar)
        assertThat(parcel.getId()).isEqualTo(10L);
        assertThat(parcel.getCode()).isEqualTo("PARCEL001");
        assertThat(parcel.getDeliveryOTP()).isEqualTo("123456");
        assertThat(parcel.getSenderName()).isEqualTo("Juan Original"); // No cambia
    }

    @Test
    void patch_shouldUpdateOnlyPrice() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .price(new BigDecimal("25000.00"))
                .receiverName("María García")
                .receiverPhone("3007654321")
                .status(ParcelStatus.CREATED)
                .build();

        var updateRequest = new ParcelUpdateRequest(
                new BigDecimal("35000.00"),
                null,
                null,
                null, null
        );

        // When
        mapper.patch(parcel, updateRequest);

        // Then
        assertThat(parcel.getPrice()).isEqualByComparingTo(new BigDecimal("35000.00")); // Cambió
        assertThat(parcel.getReceiverName()).isEqualTo("María García"); // No cambió
        assertThat(parcel.getReceiverPhone()).isEqualTo("3007654321"); // No cambió
        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.CREATED); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyStatus() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .price(new BigDecimal("25000.00"))
                .receiverName("María García")
                .receiverPhone("3007654321")
                .status(ParcelStatus.CREATED)
                .build();

        var updateRequest = new ParcelUpdateRequest(
                null,
                ParcelStatus.DELIVERED,
                null, null, null
        );

        // When
        mapper.patch(parcel, updateRequest);

        // Then
        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.DELIVERED); // Cambió
        assertThat(parcel.getPrice()).isEqualByComparingTo(new BigDecimal("25000.00")); // No cambió
        assertThat(parcel.getReceiverName()).isEqualTo("María García"); // No cambió
        assertThat(parcel.getReceiverPhone()).isEqualTo("3007654321"); // No cambió
    }

    @Test
    void patch_shouldNotModifyIdCodeDeliveryOTPOrSenderName() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        var toStop = Stop.builder().id(2L).build();

        var parcel = Parcel.builder()
                .id(10L)
                .code("PARCEL001")
                .deliveryOTP("123456")
                .senderName("Juan Pérez")
                .senderPhone("3001234567")
                .receiverName("María García")
                .receiverPhone("3007654321")
                .price(new BigDecimal("25000.00"))
                .status(ParcelStatus.CREATED)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        var updateRequest = new ParcelUpdateRequest(
                new BigDecimal("30000.00"),
                ParcelStatus.IN_TRANSIT,
                "3009999999",
                "new receptor", "3131313133"
        );

        // When
        mapper.patch(parcel, updateRequest);

        // Then
        assertThat(parcel.getId()).isEqualTo(10L); // No cambió
        assertThat(parcel.getCode()).isEqualTo("PARCEL001"); // No cambió
        assertThat(parcel.getDeliveryOTP()).isEqualTo("123456"); // No cambió
        assertThat(parcel.getSenderName()).isEqualTo("Juan Pérez"); // No cambió
        assertThat(parcel.getFromStop()).isEqualTo(fromStop); // No cambió
        assertThat(parcel.getToStop()).isEqualTo(toStop); // No cambió
    }
}