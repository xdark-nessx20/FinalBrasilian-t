package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.Parcel;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Route;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ParcelRepositoryTest extends AbstractRepository {

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Stop stop1;
    private Stop stop3;
    private Parcel parcel1;
    private Parcel parcel2;
    private Parcel parcel3;
    private Parcel parcel4;
    private Parcel parcel5;
    private Parcel parcel6;

    @BeforeEach
    void setUp() {
        parcelRepository.deleteAll();
        stopRepository.deleteAll();
        routeRepository.deleteAll();

        Route route = routeRepository.save(Route.builder()
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKM(BigDecimal.valueOf(415.0))
                .durationMin(360)
                .build());

        stop1 = stopRepository.save(Stop.builder()
                .route(route)
                .name("Terminal Bogotá")
                .stopOrder(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build());

        Stop stop2 = stopRepository.save(Stop.builder()
                .route(route)
                .name("Peaje La Línea")
                .stopOrder(2)
                .lat(4.5000)
                .lng(-74.5000)
                .build());

        stop3 = stopRepository.save(Stop.builder()
                .route(route)
                .name("Terminal Medellín")
                .stopOrder(3)
                .lat(6.2442)
                .lng(-75.5812)
                .build());

        parcel1 = createParcel("PARCEL001", stop1, stop3, new BigDecimal("25000.00"),
                ParcelStatus.IN_TRANSIT, "Juan Pérez", "3001234567",
                "María García", "3007654321", "OTP12345");

        parcel2 = createParcel("PARCEL002", stop1, stop2, new BigDecimal("15000.00"),
                ParcelStatus.DELIVERED, "Carlos Ruiz", "3009876543",
                "Ana López", "3001111111", "OTP67890");

        parcel3 = createParcel("PARCEL003", stop2, stop3, new BigDecimal("20000.00"),
                ParcelStatus.CREATED, "juan perez", "3002222222",
                "Pedro Sánchez", "3003333333", "OTP11111");

        parcel4 = createParcel("PARCEL004", stop1, stop3, new BigDecimal("30000.00"),
                ParcelStatus.IN_TRANSIT, "Luis Martínez", "3004444444",
                "maría garcía", "3005555555", "OTP22222");

        parcel5 = createParcel("PARCEL005", stop1, stop2, new BigDecimal("18000.00"),
                ParcelStatus.FAILED, "JUAN PEREZ", "3006666666",
                "Sofia Torres", "3007777777", "OTP33333");

        parcel6 = createParcel("PARCEL006", stop1, stop3, new BigDecimal("35000.00"),
                ParcelStatus.DELIVERED, "Roberto Cruz", "3008888888",
                "Laura Ramírez", "3009999999", "OTP44444");
    }

    private Parcel createParcel(String code, Stop from, Stop to, BigDecimal price,
                                ParcelStatus status, String senderName, String senderPhone,
                                String receiverName, String receiverPhone, String deliveryOTP) {
        return parcelRepository.save(Parcel.builder()
                .code(code)
                .fromStop(from)
                .toStop(to)
                .price(price)
                .status(status)
                .senderName(senderName)
                .senderPhone(senderPhone)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .deliveryOTP(deliveryOTP)
                .build());
    }

    @Test
    void shouldFindParcelByCode() {
        Optional<Parcel> result = parcelRepository.findByCode("PARCEL001");

        assertThat(result).isPresent()
                .hasValueSatisfying(parcel -> {
                    assertThat(parcel.getId()).isEqualTo(parcel1.getId());
                    assertThat(parcel.getCode()).isEqualTo("PARCEL001");
                    assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.IN_TRANSIT);
                });
    }

    @Test
    void shouldFindParcelsBySenderNameIgnoringCase() {
        List<Parcel> result = parcelRepository.findBySenderNameIgnoringCase("juan perez");

        assertThat(result)
                .hasSize(3)
                .extracting(Parcel::getId)
                .containsExactlyInAnyOrder(parcel1.getId(), parcel3.getId(), parcel5.getId());
    }

    @Test
    void shouldFindParcelsByReceiverNameIgnoringCase() {
        List<Parcel> result = parcelRepository.findByReceiverNameIgnoringCase("maría garcía");

        assertThat(result)
                .hasSize(2)
                .extracting(Parcel::getId)
                .containsExactlyInAnyOrder(parcel1.getId(), parcel4.getId());
    }

    @Test
    void shouldFindParcelByDeliveryOTP() {
        Optional<Parcel> result = parcelRepository.findByDeliveryOTP("OTP12345");

        assertThat(result).isPresent()
                .hasValueSatisfying(parcel -> {
                    assertThat(parcel.getId()).isEqualTo(parcel1.getId());
                    assertThat(parcel.getDeliveryOTP()).isEqualTo("OTP12345");
                    assertThat(parcel.getReceiverName()).isEqualTo("María García");
                });
    }

    @Test
    void shouldFindParcelsByStretchWithBothStops() {
        List<Parcel> result = parcelRepository.findAllByStretch(stop1.getId(), stop3.getId());

        assertThat(result)
                .hasSize(3)
                .extracting(Parcel::getId)
                .containsExactlyInAnyOrder(parcel1.getId(), parcel4.getId(), parcel6.getId());
    }

    @Test
    void shouldFindParcelsByStretchWithOnlyFromStop() {
        List<Parcel> result = parcelRepository.findAllByStretch(stop1.getId(), null);

        assertThat(result)
                .hasSize(5)
                .extracting(Parcel::getId)
                .containsExactlyInAnyOrder(
                        parcel1.getId(), parcel2.getId(),
                        parcel4.getId(), parcel5.getId(), parcel6.getId()
                );
    }

    @Test
    void shouldFindParcelsByStretchWithOnlyToStop() {
        List<Parcel> result = parcelRepository.findAllByStretch(null, stop3.getId());

        assertThat(result)
                .hasSize(4)
                .extracting(Parcel::getId)
                .containsExactlyInAnyOrder(
                        parcel1.getId(), parcel3.getId(),
                        parcel4.getId(), parcel6.getId()
                );
    }

    @Test
    void shouldCountParcelsByStatus() {
        long inTransitCount = parcelRepository.countByStatus(ParcelStatus.IN_TRANSIT);
        long deliveredCount = parcelRepository.countByStatus(ParcelStatus.DELIVERED);
        long createdCount = parcelRepository.countByStatus(ParcelStatus.CREATED);
        long failedCount = parcelRepository.countByStatus(ParcelStatus.FAILED);

        assertThat(inTransitCount).isEqualTo(2);
        assertThat(deliveredCount).isEqualTo(2);
        assertThat(createdCount).isEqualTo(1);
        assertThat(failedCount).isEqualTo(1);
    }

    @Test
    void shouldFindParcelsByStatus() {
        Page<Parcel> result = parcelRepository.findAllByStatus(
                ParcelStatus.IN_TRANSIT, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Parcel::getId)
                .containsExactlyInAnyOrder(parcel1.getId(), parcel4.getId());
    }

    @Test
    void shouldCalculateTotalPriceOfAllParcels() {
        BigDecimal total = parcelRepository.calculateTotal();

        assertThat(total)
                .isNotNull()
                .isEqualByComparingTo(new BigDecimal("143000.00"));
    }
}
