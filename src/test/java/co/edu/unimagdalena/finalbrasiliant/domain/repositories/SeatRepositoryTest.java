package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Seat;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.SeatType;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BusRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SeatRepositoryTest extends AbstractRepository {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BusRepository busRepository;

    private Bus bus1;
    private Bus bus2;
    private Seat seat1;
    private Seat seat2;
    private Seat seat3;
    private Seat seat4;
    private Seat seat5;

    @BeforeEach
    void setUp() {
        seatRepository.deleteAll();
        busRepository.deleteAll();

        bus1 = busRepository.save(Bus.builder()
                .plate("ABC123")
                .capacity(45)
                .status(BusStatus.AVAILABLE)
                .build());

        bus2 = busRepository.save(Bus.builder()
                .plate("XYZ789")
                .capacity(30)
                .status(BusStatus.AVAILABLE)
                .build());

        seat1 = createSeat(bus1, "A1", SeatType.STANDARD);
        seat2 = createSeat(bus1, "A2", SeatType.STANDARD);
        seat3 = createSeat(bus1, "P1", SeatType.PREFERENTIAL);
        seat4 = createSeat(bus2, "A1", SeatType.STANDARD);
        seat5 = createSeat(bus2, "P1", SeatType.PREFERENTIAL);
    }

    private Seat createSeat(Bus bus, String number, SeatType type) {
        return seatRepository.save(Seat.builder()
                .bus(bus)
                .number(number)
                .type(type)
                .build());
    }

    @Test
    void shouldFindAllSeatsByBusId() {
        List<Seat> result = seatRepository.findAllByBus_Id(bus1.getId());

        assertThat(result)
                .hasSize(3)
                .extracting(Seat::getId)
                .containsExactlyInAnyOrder(seat1.getId(), seat2.getId(), seat3.getId());

        assertThat(result)
                .allSatisfy(seat -> assertThat(seat.getBus().getId()).isEqualTo(bus1.getId()));
    }

    @Test
    void shouldFindAllSeatsForSecondBus() {
        List<Seat> result = seatRepository.findAllByBus_Id(bus2.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(Seat::getId)
                .containsExactlyInAnyOrder(seat4.getId(), seat5.getId());

        assertThat(result)
                .extracting(Seat::getNumber)
                .containsExactlyInAnyOrder("A1", "P1");
    }

    @Test
    void shouldFindSeatByNumberAndBusId() {
        Optional<Seat> result = seatRepository.findByNumberAndBus_Id("A1", bus1.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(seat -> {
                    assertThat(seat.getId()).isEqualTo(seat1.getId());
                    assertThat(seat.getNumber()).isEqualTo("A1");
                    assertThat(seat.getType()).isEqualTo(SeatType.STANDARD);
                    assertThat(seat.getBus().getId()).isEqualTo(bus1.getId());
                });
    }

    @Test
    void shouldFindDifferentSeatWithSameNumberInDifferentBus() {
        Optional<Seat> result = seatRepository.findByNumberAndBus_Id("A1", bus2.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(seat -> {
                    assertThat(seat.getId()).isEqualTo(seat4.getId());
                    assertThat(seat.getNumber()).isEqualTo("A1");
                    assertThat(seat.getBus().getId()).isEqualTo(bus2.getId());
                });
    }

    @Test
    void shouldFindPreferentialSeatByNumberAndBusId() {
        Optional<Seat> result = seatRepository.findByNumberAndBus_Id("P1", bus1.getId());

        assertThat(result).isPresent()
                .hasValueSatisfying(seat -> {
                    assertThat(seat.getId()).isEqualTo(seat3.getId());
                    assertThat(seat.getNumber()).isEqualTo("P1");
                    assertThat(seat.getType()).isEqualTo(SeatType.PREFERENTIAL);
                });
    }

    @Test
    void shouldFindAllStandardSeats() {
        List<Seat> result = seatRepository.findAllByType(SeatType.STANDARD);

        assertThat(result)
                .hasSize(3)
                .extracting(Seat::getId)
                .containsExactlyInAnyOrder(seat1.getId(), seat2.getId(), seat4.getId());

        assertThat(result)
                .allSatisfy(seat -> assertThat(seat.getType()).isEqualTo(SeatType.STANDARD));
    }

    @Test
    void shouldFindAllPreferentialSeats() {
        List<Seat> result = seatRepository.findAllByType(SeatType.PREFERENTIAL);

        assertThat(result)
                .hasSize(2)
                .extracting(Seat::getId)
                .containsExactlyInAnyOrder(seat3.getId(), seat5.getId());

        assertThat(result)
                .allSatisfy(seat -> assertThat(seat.getType()).isEqualTo(SeatType.PREFERENTIAL));

        assertThat(result)
                .extracting(Seat::getNumber)
                .containsExactlyInAnyOrder("P1", "P1");
    }
}