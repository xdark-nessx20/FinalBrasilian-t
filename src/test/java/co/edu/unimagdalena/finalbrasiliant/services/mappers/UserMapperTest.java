package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        // Given
        var request = new userCreateRequest(
                "Juan Pérez",
                "juan.perez@email.com",
                "3001234567",
                Role.DRIVER,
                "$2a$10$hashedPassword"
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getUserName()).isEqualTo("Juan Pérez");
        assertThat(entity.getEmail()).isEqualTo("juan.perez@email.com");
        assertThat(entity.getPhone()).isEqualTo("3001234567");
        assertThat(entity.getRole()).isEqualTo(Role.DRIVER);
        assertThat(entity.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");
        assertThat(entity.getId()).isNull(); // Ignored by mapper
        assertThat(entity.getCreatedAt()).isNull(); // Ignored by mapper
        assertThat(entity.getStatus()).isNull(); // Ignored by mapper
    }

    @Test
    void toEntity_shouldMapCreateRequestWithDifferentRole() {
        // Given
        var request = new userCreateRequest(
                "María García",
                "maria.garcia@email.com",
                "3109876543",
                Role.PASSENGER,
                "$2a$10$anotherHash"
        );

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getUserName()).isEqualTo("María García");
        assertThat(entity.getEmail()).isEqualTo("maria.garcia@email.com");
        assertThat(entity.getPhone()).isEqualTo("3109876543");
        assertThat(entity.getRole()).isEqualTo(Role.PASSENGER);
        assertThat(entity.getPasswordHash()).isEqualTo("$2a$10$anotherHash");
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var createdAt = OffsetDateTime.now();
        var user = User.builder()
                .id(10L)
                .userName("Carlos Rodríguez")
                .email("carlos.rodriguez@email.com")
                .phone("3201234567")
                .role(Role.ADMIN)
                .status(true)
                .passwordHash("$2a$10$hashedPassword")
                .createdAt(createdAt)
                .build();

        // When
        var response = mapper.toResponse(user);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.userName()).isEqualTo("Carlos Rodríguez");
        assertThat(response.email()).isEqualTo("carlos.rodriguez@email.com");
        assertThat(response.phone()).isEqualTo("3201234567");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(response.status()).isTrue();
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void toResponse_shouldMapEntityWithInactiveStatus() {
        // Given
        var createdAt = OffsetDateTime.now();
        var user = User.builder()
                .id(5L)
                .userName("Ana López")
                .email("ana.lopez@email.com")
                .phone("3157654321")
                .role(Role.DRIVER)
                .status(false)
                .passwordHash("$2a$10$hash")
                .createdAt(createdAt)
                .build();

        // When
        var response = mapper.toResponse(user);

        // Then
        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.status()).isFalse();
        assertThat(response.role()).isEqualTo(Role.DRIVER);
    }

    @Test
    void patch_shouldUpdateAllFields() {
        // Given
        var user = User.builder()
                .id(10L)
                .userName("Nombre Original")
                .email("original@email.com")
                .phone("3001111111")
                .role(Role.PASSENGER)
                .status(true)
                .passwordHash("$2a$10$oldHash")
                .createdAt(OffsetDateTime.now())
                .build();

        var updateRequest = new userUpdateRequest(
                "Nombre Actualizado",
                "actualizado@email.com",
                "3009999999",
                Role.DRIVER,
                false,
                "$2a$10$newHash"
        );

        // When
        mapper.patch(user, updateRequest);

        // Then
        assertThat(user.getUserName()).isEqualTo("Nombre Actualizado");
        assertThat(user.getEmail()).isEqualTo("actualizado@email.com");
        assertThat(user.getPhone()).isEqualTo("3009999999");
        assertThat(user.getRole()).isEqualTo(Role.DRIVER);
        assertThat(user.getStatus()).isFalse();
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$newHash");
        assertThat(user.getId()).isEqualTo(10L); // No cambió
    }

    @Test
    void patch_shouldIgnoreNullFields() {
        // Given
        var user = User.builder()
                .id(10L)
                .userName("Nombre Original")
                .email("original@email.com")
                .phone("3001111111")
                .role(Role.PASSENGER)
                .status(true)
                .passwordHash("$2a$10$oldHash")
                .build();

        var updateRequest = new userUpdateRequest(null, null, null, null, null, null);

        // When
        mapper.patch(user, updateRequest);

        // Then
        assertThat(user.getUserName()).isEqualTo("Nombre Original"); // No cambió
        assertThat(user.getEmail()).isEqualTo("original@email.com"); // No cambió
        assertThat(user.getPhone()).isEqualTo("3001111111"); // No cambió
        assertThat(user.getRole()).isEqualTo(Role.PASSENGER); // No cambió
        assertThat(user.getStatus()).isTrue(); // No cambió
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$oldHash"); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyUserName() {
        // Given
        var user = User.builder()
                .id(10L)
                .userName("Nombre Viejo")
                .email("email@test.com")
                .phone("3001234567")
                .role(Role.DRIVER)
                .status(true)
                .passwordHash("$2a$10$hash")
                .build();

        var updateRequest = new userUpdateRequest("Nombre Nuevo", null, null, null, null, null);

        // When
        mapper.patch(user, updateRequest);

        // Then
        assertThat(user.getUserName()).isEqualTo("Nombre Nuevo"); // Cambió
        assertThat(user.getEmail()).isEqualTo("email@test.com"); // No cambió
        assertThat(user.getPhone()).isEqualTo("3001234567"); // No cambió
        assertThat(user.getRole()).isEqualTo(Role.DRIVER); // No cambió
        assertThat(user.getStatus()).isTrue(); // No cambió
    }

    @Test
    void patch_shouldUpdateOnlyStatus() {
        // Given
        var user = User.builder()
                .id(10L)
                .userName("Usuario Test")
                .email("test@email.com")
                .phone("3001234567")
                .role(Role.PASSENGER)
                .status(true)
                .passwordHash("$2a$10$hash")
                .build();

        var updateRequest = new userUpdateRequest(null, null, null, null, false, null);

        // When
        mapper.patch(user, updateRequest);

        // Then
        assertThat(user.getStatus()).isFalse(); // Cambió
        assertThat(user.getUserName()).isEqualTo("Usuario Test"); // No cambió
        assertThat(user.getEmail()).isEqualTo("test@email.com"); // No cambió
    }

    @Test
    void patch_shouldUpdatePasswordHash() {
        // Given
        var user = User.builder()
                .id(10L)
                .userName("Usuario")
                .email("user@test.com")
                .phone("3001234567")
                .role(Role.ADMIN)
                .status(true)
                .passwordHash("$2a$10$oldPassword")
                .build();

        var updateRequest = new userUpdateRequest(null, null, null, null, null, "$2a$10$newPassword");

        // When
        mapper.patch(user, updateRequest);

        // Then
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$newPassword"); // Cambió
        assertThat(user.getUserName()).isEqualTo("Usuario"); // No cambió
        assertThat(user.getRole()).isEqualTo(Role.ADMIN); // No cambió
    }

    @Test
    void patch_shouldNotModifyIdAndCreatedAt() {
        // Given
        var createdAt = OffsetDateTime.now().minusDays(1);
        var user = User.builder()
                .id(10L)
                .userName("Original")
                .email("original@email.com")
                .phone("3001234567")
                .role(Role.PASSENGER)
                .status(true)
                .passwordHash("$2a$10$hash")
                .createdAt(createdAt)
                .build();

        var updateRequest = new userUpdateRequest(
                "Actualizado",
                "actualizado@email.com",
                "3009999999",
                Role.DRIVER,
                false,
                "$2a$10$newHash"
        );

        // When
        mapper.patch(user, updateRequest);

        // Then
        assertThat(user.getId()).isEqualTo(10L); // No cambió
        assertThat(user.getCreatedAt()).isEqualTo(createdAt); // No cambió
    }
}