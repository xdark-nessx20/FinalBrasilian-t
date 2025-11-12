package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BusDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.BusServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.BusMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BusServiceImplTest {

    @Mock
    private BusRepository busRepo;

    @Spy
    private BusMapper mapper = Mappers.getMapper(BusMapper.class);

    @InjectMocks
    private BusServiceImpl service;

    @Test
    void shouldCreateBusSuccessfully() {
        // Given
        var amenities = Set.of("WiFi", "AC");
        var request = new BusCreateRequest("ABC123", 45, amenities, BusStatus.AVAILABLE);

        when(busRepo.findByPlate("ABC123")).thenReturn(Optional.empty());
        when(busRepo.save(any(Bus.class))).thenAnswer(inv -> {
            Bus b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.plate()).isEqualTo("ABC123");
        assertThat(response.capacity()).isEqualTo(45);
        assertThat(response.amenities()).containsExactlyInAnyOrderElementsOf(amenities);
        assertThat(response.status()).isEqualTo(BusStatus.AVAILABLE);

        verify(busRepo).save(any(Bus.class));
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenPlateExists() {
        // Given
        var request = new BusCreateRequest("ABC123", 45, Set.of(), BusStatus.AVAILABLE);
        var existingBus = Bus.builder().id(5L).plate("ABC123").build();

        when(busRepo.findByPlate("ABC123")).thenReturn(Optional.of(existingBus));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Already exists bus with plate ABC123");

        verify(busRepo, never()).save(any());
    }

    @Test
    void shouldGetBusById() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi"))
                .status(BusStatus.AVAILABLE)
                .build();

        when(busRepo.findById(10L)).thenReturn(Optional.of(bus));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.plate()).isEqualTo("ABC123");
        assertThat(response.capacity()).isEqualTo(45);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentBus() {
        // Given
        when(busRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus 99 not found");
    }

    @Test
    void shouldUpdateBusSuccessfully() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi"))
                .status(BusStatus.AVAILABLE)
                .build();

        var updateRequest = new BusUpdateRequest("XYZ789", 50, Set.of("AC"), BusStatus.MAINTENANCE);

        when(busRepo.findById(10L)).thenReturn(Optional.of(bus));
        when(busRepo.findByPlate("XYZ789")).thenReturn(Optional.empty());
        when(busRepo.save(any(Bus.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.plate()).isEqualTo("XYZ789");
        assertThat(response.capacity()).isEqualTo(50);
        assertThat(response.amenities()).containsExactly("AC");
        assertThat(response.status()).isEqualTo(BusStatus.MAINTENANCE);

        verify(busRepo).save(any(Bus.class));
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenUpdateWithExistingPlate() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .build();

        var anotherBus = Bus.builder()
                .id(20L)
                .plate("XYZ789")
                .build();

        var updateRequest = new BusUpdateRequest("XYZ789", null, null, null);

        when(busRepo.findById(10L)).thenReturn(Optional.of(bus));
        when(busRepo.findByPlate("XYZ789")).thenReturn(Optional.of(anotherBus));

        // When / Then
        assertThatThrownBy(() -> service.update(10L, updateRequest))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Already exists bus with plate XYZ789");

        verify(busRepo, never()).save(any());
    }

    @Test
    void shouldDeleteBus() {
        // Given
        when(busRepo.existsById(10L)).thenReturn(true);

        // When
        service.delete(10L);

        // Then
        verify(busRepo).deleteById(10L);
    }

    @Test
    void shouldListBusesByAmenities() {
        // Given
        var amenities = Set.of("WiFi", "AC");

        var bus1 = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi", "AC", "Baño"))
                .status(BusStatus.AVAILABLE)
                .build();

        var bus2 = Bus.builder()
                .id(2L)
                .plate("XYZ789")
                .capacity(50)
                .amenities(Set.of("WiFi", "AC"))
                .status(BusStatus.AVAILABLE)
                .build();

        when(busRepo.findByAmenities(amenities, 2L)).thenReturn(List.of(bus1, bus2));

        // When
        var result = service.listByAmenities(amenities);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
    }
}
