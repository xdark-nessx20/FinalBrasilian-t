package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class BusRepositoryTest extends AbstractRepository {

    @Autowired
    private BusRepository busRepository;

    private Bus bus1;
    private Bus bus2;
    private Bus bus3;
    private Bus bus4;
    private Bus bus5;

    @BeforeEach
    void setUp() {
        busRepository.deleteAll();

        bus1 = createBus("ABC123", 45, BusStatus.AVAILABLE,
                Set.of("WiFi", "AC", "Baño", "USB"));

        bus2 = createBus("XYZ789", 50, BusStatus.AVAILABLE,
                Set.of("WiFi", "AC"));

        bus3 = createBus("DEF456", 40, BusStatus.MAINTENANCE,
                Set.of("WiFi", "AC", "Baño"));

        bus4 = createBus("GHI789", 55, BusStatus.INACTIVE,
                Set.of("AC"));

        bus5 = createBus("JKL012", 48, BusStatus.ON_ROUTE,
                Set.of("WiFi", "AC", "Baño", "USB", "TV"));
    }

    private Bus createBus(String plate, Integer capacity, BusStatus status, Set<String> amenities) {
        return busRepository.save(Bus.builder()
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .amenities(amenities)
                .build());
    }

    @Test
    void shouldFindBusByPlate() {
        Optional<Bus> result = busRepository.findByPlate("ABC123");

        assertThat(result).isPresent()
                .hasValueSatisfying(bus -> {
                    assertThat(bus.getId()).isEqualTo(bus1.getId());
                    assertThat(bus.getPlate()).isEqualTo("ABC123");
                    assertThat(bus.getCapacity()).isEqualTo(45);
                });
    }

    @Test
    void shouldFindBusesByStatus() {
        List<Bus> result = busRepository.findByStatus(BusStatus.AVAILABLE);

        assertThat(result)
                .hasSize(2)
                .extracting(Bus::getId)
                .containsExactlyInAnyOrder(bus1.getId(), bus2.getId());
    }

    @Test
    void shouldFindBusesByCapacityGreaterThanEqual() {
        List<Bus> result = busRepository.findByCapacityGreaterThanEqual(48);

        assertThat(result)
                .hasSize(3)
                .extracting(Bus::getId)
                .containsExactlyInAnyOrder(bus2.getId(), bus4.getId(), bus5.getId());
    }

    @Test
    void shouldFindBusesByStatusWithPagination() {
        Page<Bus> result = busRepository.findByStatus(
                BusStatus.AVAILABLE, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Bus::getPlate)
                .containsExactlyInAnyOrder("ABC123", "XYZ789");
    }

    @Test
    void shouldFindBusesByMultipleAmenities() {
        Set<String> requiredAmenities = Set.of("WiFi", "AC", "Baño");

        List<Bus> result = busRepository.findByAmenities(
                requiredAmenities, requiredAmenities.size());

        assertThat(result)
                .hasSize(3)
                .extracting(Bus::getId)
                .containsExactlyInAnyOrder(bus1.getId(), bus3.getId(), bus5.getId());
    }

    @Test
    void shouldFindBusesByAllAmenities() {
        Set<String> allAmenities = Set.of("WiFi", "AC", "Baño", "USB", "TV");

        List<Bus> result = busRepository.findByAmenities(
                allAmenities, allAmenities.size());

        assertThat(result)
                .hasSize(1)
                .extracting(Bus::getId)
                .containsExactly(bus5.getId());
    }

    @Test
    void shouldNotFindBusesWhenAmenitiesNotMatch() {
        Set<String> requiredAmenities = Set.of("WiFi", "AC", "Baño", "GPS");

        List<Bus> result = busRepository.findByAmenities(
                requiredAmenities, requiredAmenities.size());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindBusByPlateReturnEmptyWhenNotFound() {
        Optional<Bus> result = busRepository.findByPlate("NOTEXIST");

        assertThat(result).isEmpty();
    }
}
