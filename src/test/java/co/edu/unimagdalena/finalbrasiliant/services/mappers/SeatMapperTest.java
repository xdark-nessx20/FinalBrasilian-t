package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Seat;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class SeatMapperTest {

    private final SeatMapper mapper = Mappers.getMapper(SeatMapper.class);

    @Test
    void toEntity_shouldMapCreateRequestWithStandardSeat() {
        // Given
        var request = new SeatCreateRequest(
                1L,
                "A1",
                SeatType.STANDARD
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getBus()).isNull();
        assertThat(entity.getNumber()).isEqualTo("A1");
        assertThat(entity.getType()).isEqualTo(SeatType.STANDARD);
        assertThat(entity.getId()).isNull(); // Ignored by mapper
    }

    @Test
    void toEntity_shouldMapCreateRequestWithPreferentialSeat() {
        // Given
        var request = new SeatCreateRequest(
                2L,
                "B5",
                SeatType.PREFERENTIAL
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getBus()).isNull();
        assertThat(entity.getNumber()).isEqualTo("B5");
        assertThat(entity.getType()).isEqualTo(SeatType.PREFERENTIAL);
        assertThat(entity.getId()).isNull(); // Ignored by mapper
    }

    @Test
    void toEntity_shouldMapCreateRequestWithDifferentSeatNumber() {
        // Given
        var request = new SeatCreateRequest(
                5L,
                "C12",
                SeatType.STANDARD
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getBus()).isNull();
        assertThat(entity.getNumber()).isEqualTo("C12");
        assertThat(entity.getType()).isEqualTo(SeatType.STANDARD);
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        // When
        var response = mapper.toResponse(seat);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bus_id()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("A1");
        assertThat(response.type()).isEqualTo(SeatType.STANDARD);
    }

    @Test
    void toResponse_shouldMapEntityWithPreferentialType() {
        // Given
        var bus = Bus.builder().id(3L).build();
        var seat = Seat.builder()
                .id(20L)
                .bus(bus)
                .number("D3")
                .type(SeatType.PREFERENTIAL)
                .build();

        // When
        var response = mapper.toResponse(seat);

        // Then
        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.bus_id()).isEqualTo(3L);
        assertThat(response.number()).isEqualTo("D3");
        assertThat(response.type()).isEqualTo(SeatType.PREFERENTIAL);
    }

    @Test
    void toResponse_shouldMapEntityWithDifferentBusId() {
        // Given
        var bus = Bus.builder().id(99L).build();
        var seat = Seat.builder()
                .id(30L)
                .bus(bus)
                .number("E10")
                .type(SeatType.STANDARD)
                .build();

        // When
        var response = mapper.toResponse(seat);

        // Then
        assertThat(response.id()).isEqualTo(30L);
        assertThat(response.bus_id()).isEqualTo(99L);
        assertThat(response.number()).isEqualTo("E10");
    }

    @Test
    void patch_shouldUpdateAllFields() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                "B5",
                SeatType.PREFERENTIAL
        );

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getBus().getId()).isEqualTo(1L);
        assertThat(seat.getNumber()).isEqualTo("B5");
        assertThat(seat.getType()).isEqualTo(SeatType.PREFERENTIAL);
        assertThat(seat.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(null, null);

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getBus().getId()).isEqualTo(1L); // No cambió
        assertThat(seat.getNumber()).isEqualTo("A1"); // No cambió
        assertThat(seat.getType()).isEqualTo(SeatType.STANDARD); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlySeatNumber() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest( "C10", null);

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getNumber()).isEqualTo("C10"); // Cambió
        assertThat(seat.getBus().getId()).isEqualTo(1L); // No cambió
        assertThat(seat.getType()).isEqualTo(SeatType.STANDARD); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlySeatType() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest( null, SeatType.PREFERENTIAL);

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getType()).isEqualTo(SeatType.PREFERENTIAL); // Cambió
        assertThat(seat.getBus().getId()).isEqualTo(1L); // No cambió
        assertThat(seat.getNumber()).isEqualTo("A1"); // No cambió
    }

    @Test
    void patch_shouldUpdateNumberAndType() {
        // Given
        var bus = Bus.builder().id(5L).build();
        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest("D15", SeatType.PREFERENTIAL);

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getNumber()).isEqualTo("D15"); // Cambió
        assertThat(seat.getType()).isEqualTo(SeatType.PREFERENTIAL); // Cambió
        assertThat(seat.getBus().getId()).isEqualTo(5L); // No cambió
    }

    @Test
    void patch_shouldUpdateBusIdAndNumber() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.PREFERENTIAL)
                .build();

        var updateRequest = new SeatUpdateRequest( "F20", null);

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getBus().getId()).isEqualTo(1L); //NO Cambió
        assertThat(seat.getNumber()).isEqualTo("F20"); // Cambió
        assertThat(seat.getType()).isEqualTo(SeatType.PREFERENTIAL); // No cambió
    }

    @Test
    void patch_shouldNotModifyId() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                "B5",
                SeatType.PREFERENTIAL
        );

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldConvertStandardToPreferential() {
        // Given
        var bus = Bus.builder().id(3L).build();
        var seat = Seat.builder()
                .id(15L)
                .bus(bus)
                .number("G8")
                .type(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest( null, SeatType.PREFERENTIAL);

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getType()).isEqualTo(SeatType.PREFERENTIAL); // Cambió de STANDARD a PREFERENTIAL
        assertThat(seat.getNumber()).isEqualTo("G8"); // No cambió
        assertThat(seat.getBus().getId()).isEqualTo(3L); // No cambió
    }

    @Test
    void patch_shouldConvertPreferentialToStandard() {
        // Given
        var bus = Bus.builder().id(4L).build();
        var seat = Seat.builder()
                .id(20L)
                .bus(bus)
                .number("H12")
                .type(SeatType.PREFERENTIAL)
                .build();

        var updateRequest = new SeatUpdateRequest( null, SeatType.STANDARD);

        // When
        mapper.patch(seat, updateRequest);

        // Then
        assertThat(seat.getType()).isEqualTo(SeatType.STANDARD); // Cambió de PREFERENTIAL a STANDARD
        assertThat(seat.getNumber()).isEqualTo("H12"); // No cambió
        assertThat(seat.getBus().getId()).isEqualTo(4L); // No cambió
    }
}