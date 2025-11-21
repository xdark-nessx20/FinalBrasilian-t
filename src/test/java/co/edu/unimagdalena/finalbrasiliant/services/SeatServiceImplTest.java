package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Seat;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.SeatRepository;
import co.edu.unimagdalena.finalbrasiliant.services.impl.SeatServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.SeatMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepo;

    @Mock
    private BusRepository busRepo;

    @Spy
    private SeatMapper seatMapper = Mappers.getMapper(SeatMapper.class);

    @InjectMocks
    private SeatServiceImpl service;

    @Test
    void shouldCreateAndReturnResponse() {
        // Given
        var bus = Bus.builder().id(1L).build();

        var request = new SeatCreateRequest(
                1L,
                "A1",
                SeatType.STANDARD
        );

        when(busRepo.findById(1L)).thenReturn(Optional.of(bus));

        when(seatRepo.save(any(Seat.class))).thenAnswer(inv -> {
            Seat s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        // When
        var response = service.create(1L, request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bus_id()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("A1");
        assertThat(response.type()).isEqualTo(SeatType.STANDARD);

        verify(busRepo).findById(1L);
        verify(seatRepo).save(any(Seat.class));
    }

    @Test
    void shouldGetSeatById() {
        // Given
        var bus = Bus.builder().id(1L).build();

        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("B2")
                .type(SeatType.PREFERENTIAL)
                .build();

        when(seatRepo.findById(10L)).thenReturn(Optional.of(seat));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bus_id()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("B2");
        assertThat(response.type()).isEqualTo(SeatType.PREFERENTIAL);
    }

    @Test
    void shouldUpdateSeatViaPatch() {
        // Given
        var bus = Bus.builder().id(1L).build();

        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                "C3",
                SeatType.PREFERENTIAL
        );

        when(seatRepo.findById(10L)).thenReturn(Optional.of(seat));
        when(seatRepo.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.bus_id()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("C3");
        assertThat(response.type()).isEqualTo(SeatType.PREFERENTIAL);
        verify(seatRepo).save(any(Seat.class));
    }

    @Test
    void shouldDeleteSeat() {
        // When
        service.delete(10L);

        // Then
        verify(seatRepo).deleteById(10L);
    }

    @Test
    void shouldGetSeatsByBus() {
        // Given
        var bus = Bus.builder().id(1L).build();

        var seat1 = Seat.builder()
                .id(1L)
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();

        var seat2 = Seat.builder()
                .id(2L)
                .bus(bus)
                .number("A2")
                .type(SeatType.STANDARD)
                .build();

        var seat3 = Seat.builder()
                .id(3L)
                .bus(bus)
                .number("B1")
                .type(SeatType.PREFERENTIAL)
                .build();

        when(seatRepo.findAllByBus_Id(1L)).thenReturn(List.of(seat1, seat2, seat3));

        // When
        var result = service.getSeatsByBus(1L);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(s -> s.bus_id() == 1L);
        assertThat(result.get(0).number()).isEqualTo("A1");
        assertThat(result.get(1).number()).isEqualTo("A2");
        assertThat(result.get(2).number()).isEqualTo("B1");
    }

    @Test
    void shouldGetSeatByNumberAndBus() {
        // Given
        var bus = Bus.builder().id(1L).build();

        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A5")
                .type(SeatType.STANDARD)
                .build();

        when(seatRepo.findByNumberAndBus_Id("A5", 1L)).thenReturn(Optional.of(seat));

        // When
        var response = service.getSeatByNumberAndBus("A5", 1L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bus_id()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("A5");
        assertThat(response.type()).isEqualTo(SeatType.STANDARD);
        verify(seatRepo).findByNumberAndBus_Id("A5", 1L);
    }

    @Test
    void shouldGetSeatsByType() {
        // Given
        var bus1 = Bus.builder().id(1L).build();
        var bus2 = Bus.builder().id(2L).build();

        var seat1 = Seat.builder()
                .id(1L)
                .bus(bus1)
                .number("V1")
                .type(SeatType.PREFERENTIAL)
                .build();

        var seat2 = Seat.builder()
                .id(2L)
                .bus(bus1)
                .number("V2")
                .type(SeatType.PREFERENTIAL)
                .build();

        var seat3 = Seat.builder()
                .id(3L)
                .bus(bus2)
                .number("V1")
                .type(SeatType.PREFERENTIAL)
                .build();

        when(seatRepo.findAllByType(SeatType.PREFERENTIAL)).thenReturn(List.of(seat1, seat2, seat3));

        // When
        var result = service.getSeatsByType(SeatType.PREFERENTIAL);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(s -> s.type() == SeatType.PREFERENTIAL);
        assertThat(result.get(0).number()).isEqualTo("V1");
        assertThat(result.get(1).number()).isEqualTo("V2");
        assertThat(result.get(2).number()).isEqualTo("V1");
    }

}