package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class UserRepositoryTest extends AbstractRepository {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        baseTime = OffsetDateTime.now();

        user1 = createUser(
                "Juan Pérez",
                "juan.perez@example.com",
                "3001234567",
                Role.ROLE_PASSENGER,
                true
        );

        user2 = createUser(
                "María García",
                "maria.garcia@example.com",
                "3007654321",
                Role.ROLE_DRIVER,
                true
        );

        user3 = createUser(
                "Carlos Rodríguez",
                "carlos.rodriguez@example.com",
                "3009876543",
                Role.ROLE_PASSENGER,
                false
        );

        user4 = createUser(
                "Ana Martínez",
                "ana.martinez@example.com",
                "3005551234",
                Role.ROLE_ADMIN,
                true
        );
    }

    private User createUser(String userName, String email, String phone, Role role, Boolean status) {
        return userRepository.save(User.builder()
                .userName(userName)
                .email(email)
                .phone(phone)
                .role(role)
                .status(status)
                .passwordHash("hashed_password_" + email)
                .build());
    }

    @Test
    void shouldFindUserByUserName() {
        Optional<User> result = userRepository.findByUserName("Juan Pérez");

        assertThat(result).isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(user1.getId());
                    assertThat(user.getUserName()).isEqualTo("Juan Pérez");
                    assertThat(user.getEmail()).isEqualTo("juan.perez@example.com");
                    assertThat(user.getRole()).isEqualTo(Role.ROLE_PASSENGER);
                });
    }

    @Test
    void shouldFindUserByEmail() {
        Optional<User> result = userRepository.findByEmailIgnoreCase("maria.garcia@example.com");

        assertThat(result).isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(user2.getId());
                    assertThat(user.getUserName()).isEqualTo("María García");
                    assertThat(user.getEmail()).isEqualTo("maria.garcia@example.com");
                    assertThat(user.getRole()).isEqualTo(Role.ROLE_DRIVER);
                });
    }

    @Test
    void shouldFindUserByPhone() {
        Optional<User> result = userRepository.findByPhone("3009876543");

        assertThat(result).isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(user3.getId());
                    assertThat(user.getUserName()).isEqualTo("Carlos Rodríguez");
                    assertThat(user.getPhone()).isEqualTo("3009876543");
                    assertThat(user.getStatus()).isFalse();
                });
    }

    @Test
    void shouldFindAllUsersByCreatedAtBetween() {
        OffsetDateTime start = baseTime.minusMinutes(5);
        OffsetDateTime end = baseTime.plusMinutes(5);

        Page<User> result = userRepository.findAllByCreatedAtBetween(
                start, end, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(4)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(
                        user1.getId(),
                        user2.getId(),
                        user3.getId(),
                        user4.getId()
                );
    }

    @Test
    void shouldFindAllUsersByRole() {
        List<User> passengers = userRepository.findAllByRole(Role.ROLE_PASSENGER);

        assertThat(passengers)
                .hasSize(2)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(user1.getId(), user3.getId());

        List<User> drivers = userRepository.findAllByRole(Role.ROLE_DRIVER);

        assertThat(drivers)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user2.getId());

        List<User> admins = userRepository.findAllByRole(Role.ROLE_ADMIN);

        assertThat(admins)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user4.getId());
    }

    @Test
    void shouldFindAllUsersByStatus() {
        Page<User> activeUsers = userRepository.findAllByStatus(
                true, PageRequest.of(0, 10));

        assertThat(activeUsers.getContent())
                .hasSize(3)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId(), user4.getId());

        assertThat(activeUsers.getContent())
                .allSatisfy(user -> assertThat(user.getStatus()).isTrue());
    }

    @Test
    void shouldFindInactiveUsers() {
        Page<User> inactiveUsers = userRepository.findAllByStatus(
                false, PageRequest.of(0, 10));

        assertThat(inactiveUsers.getContent())
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user3.getId());

        assertThat(inactiveUsers.getContent())
                .allSatisfy(user -> assertThat(user.getStatus()).isFalse());
    }
}