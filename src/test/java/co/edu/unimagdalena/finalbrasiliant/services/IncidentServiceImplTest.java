package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.IncidentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Incident;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.EntityType;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.IncidentType;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.IncidentRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.AlreadyExistsException;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.IncidentServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.IncidentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock
    private IncidentRepository incidentRepo;

    @Spy
    private IncidentMapper mapper = Mappers.getMapper(IncidentMapper.class);

    @InjectMocks
    private IncidentServiceImpl service;

    @Test
    void shouldCreateAndReturnResponse() {
        // Given
        var request = new IncidentCreateRequest(
                EntityType.TRIP,
                1L,
                IncidentType.SECURITY,
                "Pasajero sospechoso reportado"
        );

        when(incidentRepo.findByEntityTypeAndEntityId(EntityType.TRIP, 1L))
                .thenReturn(Optional.empty());

        when(incidentRepo.save(any(Incident.class))).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            i.setId(10L);
            i.setCreatedAt(OffsetDateTime.now());
            return i;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.entityType()).isEqualTo(EntityType.TRIP);
        assertThat(response.entityId()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(IncidentType.SECURITY);
        assertThat(response.note()).isEqualTo("Pasajero sospechoso reportado");
        assertThat(response.createdAt()).isNotNull();

        verify(incidentRepo).save(any(Incident.class));
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenIncidentAlreadyExistsForEntity() {
        // Given
        var request = new IncidentCreateRequest(
                EntityType.TICKET,
                2L,
                IncidentType.OVERBOOK,
                "Doble venta"
        );

        var existingIncident = Incident.builder()
                .id(5L)
                .entityType(EntityType.TICKET)
                .entityId(2L)
                .type(IncidentType.OVERBOOK)
                .build();

        when(incidentRepo.findByEntityTypeAndEntityId(EntityType.TICKET, 2L))
                .thenReturn(Optional.of(existingIncident));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Already exists an Incident for the TICKET with id 2");

        verify(incidentRepo, never()).save(any());
    }

    @Test
    void shouldGetIncidentById() {
        // Given
        var incident = Incident.builder()
                .id(10L)
                .entityType(EntityType.PARCEL)
                .entityId(3L)
                .type(IncidentType.DELIVERY_FAIL)
                .createdAt(OffsetDateTime.now())
                .note("Paquete no entregado")
                .build();

        when(incidentRepo.findById(10L)).thenReturn(Optional.of(incident));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.entityType()).isEqualTo(EntityType.PARCEL);
        assertThat(response.entityId()).isEqualTo(3L);
        assertThat(response.type()).isEqualTo(IncidentType.DELIVERY_FAIL);
        assertThat(response.note()).isEqualTo("Paquete no entregado");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentIncident() {
        // Given
        when(incidentRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incident 99 not found");
    }

    @Test
    void shouldUpdateIncidentViaPatch() {
        // Given
        var incident = Incident.builder()
                .id(10L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.VEHICLE)
                .createdAt(OffsetDateTime.now())
                .note("Nota original")
                .build();

        var updateRequest = new IncidentUpdateRequest("Nota actualizada", 2L);

        when(incidentRepo.findById(10L)).thenReturn(Optional.of(incident));
        when(incidentRepo.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.note()).isEqualTo("Nota actualizada");
        assertThat(response.entityId()).isEqualTo(2L);
        verify(incidentRepo).save(any(Incident.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentIncident() {
        // Given
        var updateRequest = new IncidentUpdateRequest("Nueva nota", 5L);
        when(incidentRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incident 99 not found");

        verify(incidentRepo, never()).save(any());
    }

    @Test
    void shouldDeleteIncident() {
        // When
        service.delete(10L);

        // Then
        verify(incidentRepo).deleteById(10L);
    }

    @Test
    void shouldListIncidentsByCreatedAtBetweenDates() {
        // Given
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        var incident1 = Incident.builder()
                .id(1L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.SECURITY)
                .createdAt(from.plusDays(1))
                .note("Incidente 1")
                .build();

        var incident2 = Incident.builder()
                .id(2L)
                .entityType(EntityType.TICKET)
                .entityId(2L)
                .type(IncidentType.OVERBOOK)
                .createdAt(from.plusDays(3))
                .note("Incidente 2")
                .build();

        var page = new PageImpl<>(List.of(incident1, incident2));
        when(incidentRepo.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageable))
                .thenReturn(page);

        // When
        var result = service.listByCreatedAt(from, to, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenToDateIsBeforeFromDate() {
        // Given
        OffsetDateTime from = OffsetDateTime.now();
        OffsetDateTime to = from.minusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        // When / Then
        assertThatThrownBy(() -> service.listByCreatedAt(from, to, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date may not be before start date");

        verify(incidentRepo, never()).findByCreatedAtBetweenOrderByCreatedAtDesc(any(), any(), any());
    }

    @Test
    void shouldListIncidentsByEntityTypeWhenTypeIsNull() {
        // Given
        var incident1 = Incident.builder()
                .id(1L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.SECURITY)
                .createdAt(OffsetDateTime.now())
                .build();

        var incident2 = Incident.builder()
                .id(2L)
                .entityType(EntityType.TRIP)
                .entityId(2L)
                .type(IncidentType.VEHICLE)
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepo.findByEntityType(EntityType.TRIP))
                .thenReturn(List.of(incident1, incident2));

        // When
        var result = service.listByTypeAndEntityType(null, EntityType.TRIP);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(i -> i.entityType() == EntityType.TRIP);
        verify(incidentRepo).findByEntityType(EntityType.TRIP);
        verify(incidentRepo, never()).findByType(any());
        verify(incidentRepo, never()).findByEntityTypeAndType(any(), any());
    }

    @Test
    void shouldListIncidentsByTypeWhenEntityTypeIsNull() {
        // Given
        var incident1 = Incident.builder()
                .id(1L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.SECURITY)
                .createdAt(OffsetDateTime.now())
                .build();

        var incident2 = Incident.builder()
                .id(2L)
                .entityType(EntityType.PARCEL)
                .entityId(3L)
                .type(IncidentType.SECURITY)
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepo.findByType(IncidentType.SECURITY))
                .thenReturn(List.of(incident1, incident2));

        // When
        var result = service.listByTypeAndEntityType(IncidentType.SECURITY, null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(i -> i.type() == IncidentType.SECURITY);
        verify(incidentRepo).findByType(IncidentType.SECURITY);
        verify(incidentRepo, never()).findByEntityType(any());
        verify(incidentRepo, never()).findByEntityTypeAndType(any(), any());
    }

    @Test
    void shouldListIncidentsByTypeAndEntityTypeWhenBothProvided() {
        // Given
        var incident = Incident.builder()
                .id(1L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.SECURITY)
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepo.findByEntityTypeAndType(EntityType.TRIP, IncidentType.SECURITY))
                .thenReturn(List.of(incident));

        // When
        var result = service.listByTypeAndEntityType(IncidentType.SECURITY, EntityType.TRIP);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).entityType()).isEqualTo(EntityType.TRIP);
        assertThat(result.get(0).type()).isEqualTo(IncidentType.SECURITY);
        verify(incidentRepo).findByEntityTypeAndType(EntityType.TRIP, IncidentType.SECURITY);
        verify(incidentRepo, never()).findByType(any());
        verify(incidentRepo, never()).findByEntityType(any());
    }

    @Test
    void shouldReturnEmptyListWhenNoIncidentsMatchFilters() {
        // Given
        when(incidentRepo.findByEntityTypeAndType(EntityType.PARCEL, IncidentType.DELIVERY_FAIL))
                .thenReturn(List.of());

        // When
        var result = service.listByTypeAndEntityType(IncidentType.DELIVERY_FAIL, EntityType.PARCEL);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetAllIncidentsPaginated() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        var incident1 = Incident.builder()
                .id(1L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.SECURITY)
                .createdAt(OffsetDateTime.now())
                .build();

        var incident2 = Incident.builder()
                .id(2L)
                .entityType(EntityType.TICKET)
                .entityId(2L)
                .type(IncidentType.OVERBOOK)
                .createdAt(OffsetDateTime.now())
                .build();

        var page = new PageImpl<>(List.of(incident1, incident2));
        when(incidentRepo.findAll(pageable)).thenReturn(page);

        // When
        var result = service.getAll(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyPageWhenNoIncidents() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        var emptyPage = new PageImpl<Incident>(List.of());
        when(incidentRepo.findAll(pageable)).thenReturn(emptyPage);

        // When
        var result = service.getAll(pageable);

        // Then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}