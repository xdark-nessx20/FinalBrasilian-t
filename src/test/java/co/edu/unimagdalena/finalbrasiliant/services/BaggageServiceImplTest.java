package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.BaggageDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Baggage;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Ticket;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.BaggageRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.impl.BaggageServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.BaggageMapper;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaggageServiceImplTest {

    @Mock
    private BaggageRepository baggageRepo;

    @Mock
    private TicketRepository ticketRepo;

    @Mock
    private UserRepository userRepo;

    @Spy
    private BaggageMapper mapper = Mappers.getMapper(BaggageMapper.class);

    @InjectMocks
    private BaggageServiceImpl service;

    @Test
    void shouldCreateAndReturnResponse() {
        // Given
        var passenger = User.builder()
                .id(1L)
                .userName("Juan Pérez")
                .build();

        var ticket = Ticket.builder()
                .id(5L)
                .passenger(passenger)
                .seatNumber("A12")
                .build();

        var request = new BaggageCreateRequest(5L, new BigDecimal("15.50"), new BigDecimal("25000.00"));

        when(ticketRepo.findById(5L)).thenReturn(Optional.of(ticket));
        when(baggageRepo.save(any(Baggage.class))).thenAnswer(inv -> {
            Baggage b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ticket().id()).isEqualTo(5L);
        assertThat(response.ticket().passengerName()).isEqualTo("Juan Pérez");
        assertThat(response.weightKg()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(response.tagCode()).startsWith("BAG-");
        assertThat(response.tagCode()).hasSize(12); // BAG- + 8 chars

        verify(baggageRepo).save(any(Baggage.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTicketNotExists() {
        // Given
        var request = new BaggageCreateRequest(99L, new BigDecimal("15.50"), new BigDecimal("25000.00"));
        when(ticketRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 99 not found");

        verify(baggageRepo, never()).save(any());
    }

    @Test
    void shouldGenerateUniqueTagCode() {
        // Given
        var passenger = User.builder().id(1L).userName("Juan").build();
        var ticket = Ticket.builder().id(5L).passenger(passenger).build();
        var request = new BaggageCreateRequest(5L, new BigDecimal("10.00"), new BigDecimal("15000.00"));

        when(ticketRepo.findById(5L)).thenReturn(Optional.of(ticket));
        when(baggageRepo.save(any(Baggage.class))).thenAnswer(inv -> {
            Baggage b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        // When
        var response1 = service.create(request);
        var response2 = service.create(request);

        // Then
        assertThat(response1.tagCode()).isNotEqualTo(response2.tagCode());
        assertThat(response1.tagCode()).matches("^BAG-[A-Z0-9]{8}$");
        assertThat(response2.tagCode()).matches("^BAG-[A-Z0-9]{8}$");
    }

    @Test
    void shouldGetBaggageById() {
        // Given
        var passenger = User.builder().id(1L).userName("Ana López").build();
        var ticket = Ticket.builder().id(5L).passenger(passenger).build();

        var baggage = Baggage.builder()
                .id(10L)
                .ticket(ticket)
                .weightKg(new BigDecimal("20.00"))
                .fee(new BigDecimal("30000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        when(baggageRepo.findById(10L)).thenReturn(Optional.of(baggage));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ticket().id()).isEqualTo(5L);
        assertThat(response.ticket().passengerName()).isEqualTo("Ana López");
        assertThat(response.weightKg()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(response.tagCode()).isEqualTo("BAG-ABC12345");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentBaggage() {
        // Given
        when(baggageRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage 99 not found");
    }

    @Test
    void shouldUpdateBaggageViaPatch() {
        // Given
        var baggage = Baggage.builder()
                .id(10L)
                .ticket(Ticket.builder().id(5L).build())
                .weightKg(new BigDecimal("15.50"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        var updateRequest = new BaggageUpdateRequest(new BigDecimal("30000.00"));

        when(baggageRepo.findById(10L)).thenReturn(Optional.of(baggage));
        when(baggageRepo.save(any(Baggage.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("30000.00"));
        verify(baggageRepo).save(any(Baggage.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentBaggage() {
        // Given
        var updateRequest = new BaggageUpdateRequest(new BigDecimal("30000.00"));
        when(baggageRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage 99 not found");

        verify(baggageRepo, never()).save(any());
    }

    @Test
    void shouldDeleteBaggage() {
        // When
        service.delete(10L);

        // Then
        verify(baggageRepo).deleteById(10L);
    }

    @Test
    void shouldGetBaggageByTagCode() {
        // Given
        var passenger = User.builder().id(1L).userName("Carlos Ruiz").build();
        var ticket = Ticket.builder().id(5L).passenger(passenger).build();

        var baggage = Baggage.builder()
                .id(10L)
                .ticket(ticket)
                .weightKg(new BigDecimal("18.00"))
                .fee(new BigDecimal("28000.00"))
                .tagCode("BAG-XYZ78910")
                .build();

        when(baggageRepo.findByTagCode("BAG-XYZ78910")).thenReturn(Optional.of(baggage));

        // When
        var response = service.getByTagCode("BAG-XYZ78910");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tagCode()).isEqualTo("BAG-XYZ78910");
        assertThat(response.ticket().passengerName()).isEqualTo("Carlos Ruiz");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTagCodeNotExists() {
        // Given
        when(baggageRepo.findByTagCode("BAG-INVALID")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByTagCode("BAG-INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage with tag BAG-INVALID not found");
    }

    @Test
    void shouldListBaggagesByPassenger() {
        // Given
        var passenger = User.builder().id(1L).userName("María García").build();
        var ticket1 = Ticket.builder().id(5L).passenger(passenger).build();
        var ticket2 = Ticket.builder().id(6L).passenger(passenger).build();

        var baggage1 = Baggage.builder()
                .id(10L)
                .ticket(ticket1)
                .weightKg(new BigDecimal("15.00"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-AAA11111")
                .build();

        var baggage2 = Baggage.builder()
                .id(11L)
                .ticket(ticket2)
                .weightKg(new BigDecimal("20.00"))
                .fee(new BigDecimal("35000.00"))
                .tagCode("BAG-BBB22222")
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(passenger));
        when(baggageRepo.findByTicket_Passenger_Id(1L)).thenReturn(List.of(baggage1, baggage2));

        // When
        var result = service.listByPassenger(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).ticket().passengerName()).isEqualTo("María García");
        assertThat(result.get(1).id()).isEqualTo(11L);
        assertThat(result.get(1).ticket().passengerName()).isEqualTo("María García");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPassengerNotExists() {
        // Given
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.listByPassenger(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Passenger 99 not found");

        verify(baggageRepo, never()).findByTicket_Passenger_Id(any());
    }

    @Test
    void shouldReturnEmptyListWhenPassengerHasNoBaggages() {
        // Given
        var passenger = User.builder().id(1L).userName("Pedro").build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(passenger));
        when(baggageRepo.findByTicket_Passenger_Id(1L)).thenReturn(List.of());

        // When
        var result = service.listByPassenger(1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldListBaggagesByWeightGreaterThanOrEqual() {
        // Given
        var passenger = User.builder().id(1L).userName("Luis").build();
        var ticket1 = Ticket.builder().id(5L).passenger(passenger).build();
        var ticket2 = Ticket.builder().id(6L).passenger(passenger).build();

        var baggage1 = Baggage.builder()
                .id(10L)
                .ticket(ticket1)
                .weightKg(new BigDecimal("20.00"))
                .fee(new BigDecimal("30000.00"))
                .tagCode("BAG-AAA11111")
                .build();

        var baggage2 = Baggage.builder()
                .id(11L)
                .ticket(ticket2)
                .weightKg(new BigDecimal("25.00"))
                .fee(new BigDecimal("40000.00"))
                .tagCode("BAG-BBB22222")
                .build();

        var page = new PageImpl<>(List.of(baggage1, baggage2));
        var pageable = PageRequest.of(0, 10);

        when(baggageRepo.findByWeightKgGreaterThanEqual(new BigDecimal("20.00"), pageable))
                .thenReturn(page);

        // When
        var result = service.listByWeightKg(new BigDecimal("20.00"), true, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).weightKg()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getContent().get(1).weightKg()).isEqualByComparingTo(new BigDecimal("25.00"));

        verify(baggageRepo).findByWeightKgGreaterThanEqual(new BigDecimal("20.00"), pageable);
        verify(baggageRepo, never()).findByWeightKgLessThanEqual(any(), any());
    }

    @Test
    void shouldListBaggagesByWeightLessThanOrEqual() {
        // Given
        var passenger = User.builder().id(1L).userName("Sofia").build();
        var ticket1 = Ticket.builder().id(5L).passenger(passenger).build();
        var ticket2 = Ticket.builder().id(6L).passenger(passenger).build();

        var baggage1 = Baggage.builder()
                .id(10L)
                .ticket(ticket1)
                .weightKg(new BigDecimal("8.00"))
                .fee(new BigDecimal("15000.00"))
                .tagCode("BAG-CCC33333")
                .build();

        var baggage2 = Baggage.builder()
                .id(11L)
                .ticket(ticket2)
                .weightKg(new BigDecimal("10.00"))
                .fee(new BigDecimal("18000.00"))
                .tagCode("BAG-DDD44444")
                .build();

        var page = new PageImpl<>(List.of(baggage1, baggage2));
        var pageable = PageRequest.of(0, 10);

        when(baggageRepo.findByWeightKgLessThanEqual(new BigDecimal("10.00"), pageable))
                .thenReturn(page);

        // When
        var result = service.listByWeightKg(new BigDecimal("10.00"), false, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).weightKg()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(result.getContent().get(1).weightKg()).isEqualByComparingTo(new BigDecimal("10.00"));

        verify(baggageRepo).findByWeightKgLessThanEqual(new BigDecimal("10.00"), pageable);
        verify(baggageRepo, never()).findByWeightKgGreaterThanEqual(any(), any());
    }

    @Test
    void shouldReturnEmptyPageWhenNoBaggagesMatchWeightCriteria() {
        // Given
        var page = new PageImpl<Baggage>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(baggageRepo.findByWeightKgGreaterThanEqual(new BigDecimal("50.00"), pageable))
                .thenReturn(page);

        // When
        var result = service.listByWeightKg(new BigDecimal("50.00"), true, pageable);

        // Then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}