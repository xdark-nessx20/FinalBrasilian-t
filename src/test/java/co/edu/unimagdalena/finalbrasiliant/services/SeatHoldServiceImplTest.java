package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.SeatHoldDTOs.*;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.SeatHold;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.TripStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.SeatHoldRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.SeatHoldServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.SeatHoldMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SeatHoldServiceImplTest {

    @Mock
    private SeatHoldRepository seatHoldRepo;

    @Mock
    private TripRepository tripRepo;

    @Mock
    private UserRepository userRepo;

    @Spy
    private SeatHoldMapper mapper = Mappers.getMapper(SeatHoldMapper.class);

    @InjectMocks
    private SeatHoldServiceImpl service;

    @Test
    void shouldCreateSeatHoldSuccessfully() {
        // Given
        var trip = Trip.builder().id(1L).departureAt(OffsetDateTime.now()).status(TripStatus.SCHEDULED).build();
        var passenger = User.builder().id(2L).userName("Juan").build();

        var request = new SeatHoldCreateRequest(1L, "A12", 2L, SeatHoldStatus.HOLD);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD))
                .thenReturn(false);
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.SOLD))
                .thenReturn(false);
        when(seatHoldRepo.save(any(SeatHold.class))).thenAnswer(inv -> {
            SeatHold sh = inv.getArgument(0);
            sh.setId(10L);
            return sh;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
        assertThat(response.passenger().id()).isEqualTo(2L);
        assertThat(response.expiresAt()).isNotNull();

        verify(seatHoldRepo).save(any(SeatHold.class));
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenSeatIsHeld() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var passenger = User.builder().id(2L).build();

        var request = new SeatHoldCreateRequest(1L, "A12", 2L, SeatHoldStatus.HOLD);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD))
                .thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Seat A12 already reserved");

        verify(seatHoldRepo, never()).save(any());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenSeatIsSold() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var passenger = User.builder().id(2L).build();

        var request = new SeatHoldCreateRequest(1L, "A12", 2L, SeatHoldStatus.HOLD);

        when(tripRepo.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD))
                .thenReturn(false);
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.SOLD))
                .thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Seat A12 sold on trip 1");

        verify(seatHoldRepo, never()).save(any());
    }

    @Test
    void shouldGetSeatHoldById() {
        // Given
        var trip = Trip.builder().id(1L).departureAt(OffsetDateTime.now()).status(TripStatus.SCHEDULED).build();
        var passenger = User.builder().id(2L).userName("Juan").build();

        var seatHold = SeatHold.builder()
                .id(10L)
                .trip(trip)
                .seatNumber("A12")
                .passenger(passenger)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .status(SeatHoldStatus.HOLD)
                .build();

        when(seatHoldRepo.findById(10L)).thenReturn(Optional.of(seatHold));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A12");
    }

    @Test
    void shouldUpdateSeatHold() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var passenger = User.builder().id(2L).build();

        var seatHold = SeatHold.builder()
                .id(10L)
                .trip(trip)
                .seatNumber("A12")
                .passenger(passenger)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .status(SeatHoldStatus.HOLD)
                .build();

        var updateRequest = new SeatHoldUpdateRequest(null, "B15", null, SeatHoldStatus.SOLD);

        when(seatHoldRepo.findById(10L)).thenReturn(Optional.of(seatHold));
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "B15", SeatHoldStatus.HOLD))
                .thenReturn(false);
        when(seatHoldRepo.save(any(SeatHold.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.seatNumber()).isEqualTo("B15");
        assertThat(response.status()).isEqualTo(SeatHoldStatus.SOLD);

        verify(seatHoldRepo).save(any(SeatHold.class));
    }

    @Test
    void shouldDeleteSeatHold() {
        // Given
        when(seatHoldRepo.existsById(10L)).thenReturn(true);

        // When
        service.delete(10L);

        // Then
        verify(seatHoldRepo).deleteById(10L);
    }

    @Test
    void shouldListSeatHoldsByPassenger() {
        // Given
        var passenger = User.builder().id(2L).userName("Juan").build();
        var trip = Trip.builder().id(1L).departureAt(OffsetDateTime.now()).status(TripStatus.SCHEDULED).build();

        var seatHold1 = SeatHold.builder()
                .id(1L)
                .trip(trip)
                .seatNumber("A12")
                .passenger(passenger)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .status(SeatHoldStatus.HOLD)
                .build();

        when(userRepo.findById(2L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepo.findByPassenger_Id(2L)).thenReturn(List.of(seatHold1));

        // When
        var result = service.listByPassenger(2L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).passenger().id()).isEqualTo(2L);
    }

    @Test
    void shouldCheckIfSeatHoldExists() {
        // Given
        when(seatHoldRepo.existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD))
                .thenReturn(true);

        // When
        boolean exists = service.existsActiveSeatHold(1L, "A12");

        // Then
        assertThat(exists).isTrue();
        verify(seatHoldRepo).existsByTripIdAndSeatNumberAndStatus(1L, "A12", SeatHoldStatus.HOLD);
    }
}
