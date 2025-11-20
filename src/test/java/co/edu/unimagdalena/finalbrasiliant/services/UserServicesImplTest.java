package co.edu.unimagdalena.finalbrasiliant.services;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.services.impl.UserServiceImpl;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.UserMapper;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void shouldCreateAndReturnResponse() {
        // Given
        var request = new UserCreateRequest(
                "juan_perez",
                "juan.perez@example.com",
                "3001234567",
                Role.ROLE_PASSENGER,
                "hashedPassword123"
        );

        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(OffsetDateTime.now());
            u.setStatus(true);
            return u;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userName()).isEqualTo("juan_perez");
        assertThat(response.email()).isEqualTo("juan.perez@example.com");
        assertThat(response.phone()).isEqualTo("3001234567");
        assertThat(response.role()).isEqualTo(Role.ROLE_PASSENGER);
        assertThat(response.status()).isTrue();
        assertThat(response.createdAt()).isNotNull();

        verify(userRepo).save(any(User.class));
    }

    @Test
    void shouldGetUserById() {
        // Given
        var user = User.builder()
                .id(1L)
                .userName("maria_lopez")
                .email("maria.lopez@example.com")
                .phone("3009876543")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hashedPassword456")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        // When
        var response = service.get(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userName()).isEqualTo("maria_lopez");
        assertThat(response.email()).isEqualTo("maria.lopez@example.com");
        assertThat(response.phone()).isEqualTo("3009876543");
        assertThat(response.role()).isEqualTo(Role.ROLE_DRIVER);
        assertThat(response.status()).isTrue();
    }

    @Test
    void shouldUpdateUserViaPatch() {
        // Given
        var user = User.builder()
                .id(1L)
                .userName("carlos_gomez")
                .email("carlos.gomez@example.com")
                .phone("3001111111")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword789")
                .createdAt(OffsetDateTime.now())
                .build();

        var updateRequest = new UserUpdateRequest(
                "carlos_gomez_updated",
                "carlos.nuevo@example.com",
                "3002222222",
                Role.ROLE_ADMIN,
                false,
                "newHashedPassword"
        );

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.userName()).isEqualTo("carlos_gomez_updated");
        assertThat(response.email()).isEqualTo("carlos.nuevo@example.com");
        assertThat(response.phone()).isEqualTo("3002222222");
        assertThat(response.role()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(response.status()).isFalse();
        verify(userRepo).save(any(User.class));
    }

    @Test
    void shouldDeleteUser() {
        // When
        service.delete(1L);

        // Then
        verify(userRepo).deleteById(1L);
    }

    @Test
    void shouldGetUserByUserName() {
        // Given
        var user = User.builder()
                .id(1L)
                .userName("pedro_martinez")
                .email("pedro.martinez@example.com")
                .phone("3003333333")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword111")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByUserName("pedro_martinez")).thenReturn(Optional.of(user));

        // When
        var response = service.getByUserName("pedro_martinez");

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userName()).isEqualTo("pedro_martinez");
        assertThat(response.email()).isEqualTo("pedro.martinez@example.com");
    }

    @Test
    void shouldGetUserByEmail() {
        // Given
        var user = User.builder()
                .id(2L)
                .userName("ana_rodriguez")
                .email("ana.rodriguez@example.com")
                .phone("3004444444")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hashedPassword222")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByEmailIgnoreCase("ana.rodriguez@example.com")).thenReturn(Optional.of(user));

        // When
        var response = service.getByEmail("ana.rodriguez@example.com");

        // Then
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.userName()).isEqualTo("ana_rodriguez");
        assertThat(response.email()).isEqualTo("ana.rodriguez@example.com");
    }

    @Test
    void shouldGetUserByPhone() {
        // Given
        var user = User.builder()
                .id(3L)
                .userName("luis_fernandez")
                .email("luis.fernandez@example.com")
                .phone("3005555555")
                .role(Role.ROLE_ADMIN)
                .status(true)
                .passwordHash("hashedPassword333")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByPhone("3005555555")).thenReturn(Optional.of(user));

        // When
        var response = service.getByPhone("3005555555");

        // Then
        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.userName()).isEqualTo("luis_fernandez");
        assertThat(response.phone()).isEqualTo("3005555555");
    }

    @Test
    void shouldGetUsersByCreatedAtBetween() {
        // Given
        OffsetDateTime start = OffsetDateTime.now().minusDays(10);
        OffsetDateTime end = OffsetDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        var user1 = User.builder()
                .id(1L)
                .userName("user1")
                .email("user1@example.com")
                .phone("3001111111")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hash1")
                .createdAt(start.plusDays(2))
                .build();

        var user2 = User.builder()
                .id(2L)
                .userName("user2")
                .email("user2@example.com")
                .phone("3002222222")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hash2")
                .createdAt(start.plusDays(5))
                .build();

        var page = new PageImpl<>(List.of(user1, user2));
        when(userRepo.findAllByCreatedAtBetween(start, end, pageable)).thenReturn(page);

        // When
        var result = service.getByCreatedAtBetween(start, end, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
    }

    @Test
    void shouldGetUsersByRole() {
        // Given
        var user1 = User.builder()
                .id(1L)
                .userName("driver1")
                .email("driver1@example.com")
                .phone("3001111111")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hash1")
                .createdAt(OffsetDateTime.now())
                .build();

        var user2 = User.builder()
                .id(2L)
                .userName("driver2")
                .email("driver2@example.com")
                .phone("3002222222")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hash2")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findAllByRole(Role.ROLE_DRIVER)).thenReturn(List.of(user1, user2));

        // When
        var result = service.getByRole(Role.ROLE_DRIVER);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(u -> u.role() == Role.ROLE_DRIVER);
        assertThat(result.get(0).userName()).isEqualTo("driver1");
        assertThat(result.get(1).userName()).isEqualTo("driver2");
    }

    @Test
    void shouldGetUsersByStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        var user1 = User.builder()
                .id(1L)
                .userName("active_user1")
                .email("active1@example.com")
                .phone("3001111111")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hash1")
                .createdAt(OffsetDateTime.now())
                .build();

        var user2 = User.builder()
                .id(2L)
                .userName("active_user2")
                .email("active2@example.com")
                .phone("3002222222")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hash2")
                .createdAt(OffsetDateTime.now())
                .build();

        var page = new PageImpl<>(List.of(user1, user2));
        when(userRepo.findAllByStatus(true, pageable)).thenReturn(page);

        // When
        var result = service.getByStatus(true, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(u -> u.status() == true);
        assertThat(result.getContent().get(0).userName()).isEqualTo("active_user1");
        assertThat(result.getContent().get(1).userName()).isEqualTo("active_user2");
    }
}