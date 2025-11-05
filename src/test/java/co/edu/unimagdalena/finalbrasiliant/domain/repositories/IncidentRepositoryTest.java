package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Incident;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentRepositoryTest extends AbstractRepository {

    @Autowired
    private IncidentRepository incidentRepository;

    private Incident incident1;
    private Incident incident4;
    private Incident incident5;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        incidentRepository.deleteAll();

        now = OffsetDateTime.now();

        incident1 = createIncident(EntityType.TRIP, 1L, IncidentType.SECURITY, "Pasajero sospechoso reportado");

        Incident incident2 = createIncident(EntityType.TICKET, 2L, IncidentType.OVERBOOK, "Doble venta del mismo asiento");

        Incident incident3 = createIncident(EntityType.PARCEL, 3L, IncidentType.DELIVERY_FAIL, "Paquete no entregado en destino");

        incident4 = createIncident(EntityType.TRIP, 4L, IncidentType.VEHICLE,"Falla mecánica en el motor");

        incident5 = createIncident(EntityType.TRIP, 5L, IncidentType.SECURITY, "Equipaje abandonado en bus");
    }

    private Incident createIncident(EntityType entityType, Long entityId, IncidentType type, String note) {
        Incident incident = Incident.builder()
                .entityType(entityType)
                .entityId(entityId)
                .type(type)
                .note(note)
                .build();

        return incidentRepository.save(incident);
    }

    @Test
    void shouldFindIncidentsByDateRangeOrderedByCreatedAtDesc() {
        Page<Incident> result = incidentRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                now.minusDays(1), now.plusMinutes(1), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(5);
    }

    @Test
    void shouldFindIncidentsByEntityType() {
        List<Incident> result = incidentRepository.findByEntityType(EntityType.TRIP);

        assertThat(result)
                .hasSize(3)
                .extracting(Incident::getId)
                .containsExactlyInAnyOrder(incident1.getId(), incident4.getId(), incident5.getId())
                .allMatch(id -> result.stream()
                        .anyMatch(i -> i.getId().equals(id) && i.getEntityType() == EntityType.TRIP));
    }

    @Test
    void shouldFindIncidentsByType() {
        List<Incident> result = incidentRepository.findByType(IncidentType.SECURITY);

        assertThat(result)
                .hasSize(2)
                .extracting(Incident::getId)
                .containsExactlyInAnyOrder(incident1.getId(), incident5.getId())
                .allMatch(id -> result.stream()
                        .anyMatch(i -> i.getId().equals(id) && i.getType() == IncidentType.SECURITY));
    }

    @Test
    void shouldFindIncidentsByEntityTypeAndType() {
        List<Incident> result = incidentRepository.findByEntityTypeAndType(
                EntityType.TRIP, IncidentType.SECURITY);

        assertThat(result)
                .hasSize(2)
                .extracting(Incident::getId)
                .containsExactlyInAnyOrder(incident1.getId(), incident5.getId())
                .allMatch(id -> result.stream()
                        .anyMatch(i -> i.getId().equals(id)
                                && i.getEntityType() == EntityType.TRIP
                                && i.getType() == IncidentType.SECURITY));
    }

    @Test
    void shouldCountIncidentsByType() {
        long securityCount = incidentRepository.countByType(IncidentType.SECURITY);
        long vehicleCount = incidentRepository.countByType(IncidentType.VEHICLE);
        long overbookCount = incidentRepository.countByType(IncidentType.OVERBOOK);

        assertThat(securityCount).isEqualTo(2);
        assertThat(vehicleCount).isEqualTo(1);
        assertThat(overbookCount).isEqualTo(1);
    }

    @Test
    void shouldCountIncidentsByEntityType() {
        long tripCount = incidentRepository.countByEntityType(EntityType.TRIP);
        long ticketCount = incidentRepository.countByEntityType(EntityType.TICKET);
        long parcelCount = incidentRepository.countByEntityType(EntityType.PARCEL);

        assertThat(tripCount).isEqualTo(3);
        assertThat(ticketCount).isEqualTo(1);
        assertThat(parcelCount).isEqualTo(1);
    }
}