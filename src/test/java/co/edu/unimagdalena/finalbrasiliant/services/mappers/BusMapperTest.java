package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BusDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;


import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BusMapperTest {

    private final BusMapper mapper = Mappers.getMapper(BusMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        // Given
        var amenities = Set.of("WiFi", "AC", "Baño");
        var request = new BusCreateRequest(
                "ABC123",
                45,
                amenities,
                BusStatus.AVAILABLE
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getPlate()).isEqualTo("ABC123");
        assertThat(entity.getCapacity()).isEqualTo(45);
        assertThat(entity.getAmenities()).containsExactlyInAnyOrderElementsOf(amenities);
        assertThat(entity.getStatus()).isEqualTo(BusStatus.AVAILABLE);
        assertThat(entity.getId()).isNull(); // Ignored by mapper
    }


    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var amenities = Set.of("WiFi", "AC", "USB");
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(amenities)
                .status(BusStatus.AVAILABLE)
                .build();

        // When
        var response = mapper.toResponse(bus);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.plate()).isEqualTo("ABC123");
        assertThat(response.capacity()).isEqualTo(45);
        assertThat(response.amenities()).containsExactlyInAnyOrderElementsOf(amenities);
        assertThat(response.status()).isEqualTo(BusStatus.AVAILABLE);
    }

    @Test
    void toResponse_shouldMapEntityWithEmptyAmenities() {
        // Given
        var bus = Bus.builder()
                .id(5L)
                .plate("XYZ789")
                .capacity(50)
                .status(BusStatus.ON_ROUTE)
                .build();

        // When
        var response = mapper.toResponse(bus);

        // Then
        assertThat(response.amenities()).isEmpty();
    }

    @Test
    void patch_shouldUpdateAllFields() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi"))
                .status(BusStatus.AVAILABLE)
                .build();

        var updateRequest = new BusUpdateRequest(
                "XYZ789",
                50,
                Set.of("WiFi", "AC", "Baño"),
                BusStatus.MAINTENANCE
        );

        // When
        mapper.patch(bus, updateRequest);

        // Then
        assertThat(bus.getPlate()).isEqualTo("XYZ789");
        assertThat(bus.getCapacity()).isEqualTo(50);
        assertThat(bus.getAmenities()).containsExactlyInAnyOrder("WiFi", "AC", "Baño");
        assertThat(bus.getStatus()).isEqualTo(BusStatus.MAINTENANCE);
        assertThat(bus.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi", "AC"))
                .status(BusStatus.AVAILABLE)
                .build();

        var updateRequest = new BusUpdateRequest(null, null, null, null);

        // When
        mapper.patch(bus, updateRequest);

        // Then
        assertThat(bus.getPlate()).isEqualTo("ABC123"); // No cambió
        assertThat(bus.getCapacity()).isEqualTo(45); // No cambió
        assertThat(bus.getAmenities()).containsExactlyInAnyOrder("WiFi", "AC"); // No cambió
        assertThat(bus.getStatus()).isEqualTo(BusStatus.AVAILABLE); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyPlate() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi"))
                .status(BusStatus.AVAILABLE)
                .build();

        var updateRequest = new BusUpdateRequest("NEW123", null, null, null);

        // When
        mapper.patch(bus, updateRequest);

        // Then
        assertThat(bus.getPlate()).isEqualTo("NEW123"); // Cambió
        assertThat(bus.getCapacity()).isEqualTo(45); // No cambió
        assertThat(bus.getAmenities()).containsExactly("WiFi"); // No cambió
        assertThat(bus.getStatus()).isEqualTo(BusStatus.AVAILABLE); // No cambió
    }


    @Test
    void patch_shouldUpdateOnlyStatus() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi"))
                .status(BusStatus.AVAILABLE)
                .build();

        var updateRequest = new BusUpdateRequest(null, null, null, BusStatus.INACTIVE);

        // When
        mapper.patch(bus, updateRequest);

        // Then
        assertThat(bus.getStatus()).isEqualTo(BusStatus.INACTIVE); // Cambió
        assertThat(bus.getPlate()).isEqualTo("ABC123"); // No cambió
        assertThat(bus.getCapacity()).isEqualTo(45); // No cambió
        assertThat(bus.getAmenities()).containsExactly("WiFi"); // No cambió
    }

    @Test
    void patch_shouldClearAmenitiesWithEmptySet() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi", "AC", "Baño"))
                .status(BusStatus.AVAILABLE)
                .build();

        var updateRequest = new BusUpdateRequest(null, null, Set.of(), null);

        // When
        mapper.patch(bus, updateRequest);

        // Then
        assertThat(bus.getAmenities()).isEmpty(); // Se limpió
    }

    @Test
    void patch_shouldNotModifyId() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC123")
                .capacity(45)
                .amenities(Set.of("WiFi"))
                .status(BusStatus.AVAILABLE)
                .build();

        var updateRequest = new BusUpdateRequest(
                "NEW123",
                60,
                Set.of("AC"),
                BusStatus.MAINTENANCE
        );

        // When
        mapper.patch(bus, updateRequest);

        // Then
        assertThat(bus.getId()).isEqualTo(10L); // No cambió
    }
}
