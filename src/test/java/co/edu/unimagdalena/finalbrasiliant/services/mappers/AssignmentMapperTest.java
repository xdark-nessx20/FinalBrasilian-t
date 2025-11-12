package co.edu.unimagdalena.finalbrasiliant.services.mappers;

import co.edu.unimagdalena.finalbrasiliant.api.dto.AssignmentDTOs.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Assignment;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Trip;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentMapperTest {
    private final AssignmentMapper mapper = Mappers.getMapper(AssignmentMapper.class);

    @Test
    void toEntity_shouldMapCreateRequestWithCheckListOk() {
        // Given
        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getCheckListOk()).isTrue();
        assertThat(entity.getTrip()).isNull(); // Ignored by mapper
        assertThat(entity.getDriver()).isNull(); // Ignored by mapper
        assertThat(entity.getDispatcher()).isNull(); // Ignored by mapper
    }

    @Test
    void toEntity_shouldMapCreateRequestWithNullCheckListOk() {
        // Given
        var request = new AssignmentCreateRequest(1L, 2L, 3L, null);

        // When
        var entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getCheckListOk()).isNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var driver = User.builder().id(2L).userName("Juan Driver").build();
        var dispatcher = User.builder().id(3L).userName("Carlos Dispatcher").build();
        var assignedAt = OffsetDateTime.now();

        var assignment = Assignment.builder()
                .id(10L)
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(assignedAt)
                .build();

        // When
        var response = mapper.toResponse(assignment);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.driver().id()).isEqualTo(2L);
        assertThat(response.driver().userName()).isEqualTo("Juan Driver");
        assertThat(response.dispatcher().id()).isEqualTo(3L);
        assertThat(response.dispatcher().userName()).isEqualTo("Carlos Dispatcher");
        assertThat(response.checkListOk()).isTrue();
        assertThat(response.assignedAt()).isEqualTo(assignedAt);
    }

    @Test
    void patch_shouldUpdateCheckListOk() {
        // Given
        var assignment = Assignment.builder()
                .id(10L)
                .checkListOk(false)
                .build();

        var updateRequest = new AssignmentUpdateRequest(true, null, null);

        // When
        mapper.patch(assignment, updateRequest);

        // Then
        assertThat(assignment.getCheckListOk()).isTrue();
    }

    @Test
    void patch_shouldIgnoreNullCheckListOk() {
        // Given
        var assignment = Assignment.builder()
                .id(10L)
                .checkListOk(true)
                .build();

        var updateRequest = new AssignmentUpdateRequest(null, null, null);

        // When
        mapper.patch(assignment, updateRequest);

        // Then
        assertThat(assignment.getCheckListOk()).isTrue(); // No cambió
    }

    @Test
    void patch_shouldNotModifyDriverAndDispatcher() {
        // Given
        var driver = User.builder().id(2L).userName("Juan").build();
        var dispatcher = User.builder().id(3L).userName("Carlos").build();

        var assignment = Assignment.builder()
                .id(10L)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(false)
                .build();

        var updateRequest = new AssignmentUpdateRequest(true, 99L, 99L);

        // When
        mapper.patch(assignment, updateRequest);

        // Then
        assertThat(assignment.getDriver()).isEqualTo(driver); // No cambió
        assertThat(assignment.getDispatcher()).isEqualTo(dispatcher); // No cambió
        assertThat(assignment.getCheckListOk()).isTrue(); // Sí cambió
    }

    @Test
    void toUserSummary_shouldMapUser() {
        // Given
        var user = User.builder()
                .id(5L)
                .userName("Ana López")
                .email("ana@example.com")
                .build();

        // When
        var userSummary = mapper.toUserSummary(user);

        // Then
        assertThat(userSummary.id()).isEqualTo(5L);
        assertThat(userSummary.userName()).isEqualTo("Ana López");
    }

    @Test
    void toUserSummary_shouldHandleNullUser() {
        // When
        var userSummary = mapper.toUserSummary(null);

        // Then
        assertThat(userSummary).isNull();
    }
}
