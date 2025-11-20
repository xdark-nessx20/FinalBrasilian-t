package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest(
                "johndoe",
                "john.doe@example.com",
                "3001234567",
                Role.ROLE_PASSENGER,
                "securePassword123"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userName").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("3001234567"))
                .andExpect(jsonPath("$.role").value("ROLE_PASSENGER"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void testGetUser_Success() throws Exception {
        // Given
        User user = User.builder()
                .userName("janedoe")
                .email("jane.doe@example.com")
                .phone("3009876543")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        User savedUser = userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.userName").value("janedoe"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("3009876543"))
                .andExpect(jsonPath("$.role").value("ROLE_DRIVER"));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Given
        User user = User.builder()
                .userName("oldname")
                .email("old@example.com")
                .phone("3001111111")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        User savedUser = userRepository.save(user);

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "newname",
                "new@example.com",
                "3002222222",
                Role.ROLE_ADMIN,
                false,
                "hashedPassword"
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.userName").value("newname"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.phone").value("3002222222"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Given
        User user = User.builder()
                .userName("deleteMe")
                .email("delete@example.com")
                .phone("3003333333")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        User savedUser = userRepository.save(user);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetByUserName_Success() throws Exception {
        // Given
        User user = User.builder()
                .userName("uniqueuser")
                .email("unique@example.com")
                .phone("3004444444")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/by-name")
                        .param("userName", "uniqueuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("uniqueuser"))
                .andExpect(jsonPath("$.email").value("unique@example.com"));
    }

    @Test
    void testGetByEmail_Success() throws Exception {
        // Given
        User user = User.builder()
                .userName("emailuser")
                .email("findme@example.com")
                .phone("3005555555")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/by-email")
                        .param("email", "findme@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("findme@example.com"))
                .andExpect(jsonPath("$.userName").value("emailuser"));
    }

    @Test
    void testGetByPhone_Success() throws Exception {
        // Given
        User user = User.builder()
                .userName("phoneuser")
                .email("phone@example.com")
                .phone("3006666666")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/by-phone")
                        .param("phone", "3006666666"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("3006666666"))
                .andExpect(jsonPath("$.userName").value("phoneuser"));
    }

    @Test
    void testGetByCreatedAtBetween_Success() throws Exception {
        // Given
        OffsetDateTime start = OffsetDateTime.now().minusDays(1);
        OffsetDateTime end = OffsetDateTime.now().plusDays(1);

        User user1 = User.builder()
                .userName("user1")
                .email("user1@example.com")
                .phone("3007777777")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        
        User user2 = User.builder()
                .userName("user2")
                .email("user2@example.com")
                .phone("3008888888")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        
        userRepository.save(user1);
        userRepository.save(user2);

        // When & Then
        mockMvc.perform(get("/api/v1/users/by-created_at")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void testGetByRole_Success() throws Exception {
        // Given
        User driver1 = User.builder()
                .userName("driver1")
                .email("driver1@example.com")
                .phone("3009999999")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        
        User driver2 = User.builder()
                .userName("driver2")
                .email("driver2@example.com")
                .phone("3000000000")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        
        User passenger = User.builder()
                .userName("passenger1")
                .email("passenger1@example.com")
                .phone("3001111112")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        
        userRepository.save(driver1);
        userRepository.save(driver2);
        userRepository.save(passenger);

        // When & Then
        mockMvc.perform(get("/api/v1/users/by-role")
                        .param("role", "ROLE_DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].role", everyItem(is("ROLE_DRIVER"))));
    }

    @Test
    void testGetByStatus_Success() throws Exception {
        // Given
        User activeUser1 = User.builder()
                .userName("active1")
                .email("active1@example.com")
                .phone("3002222223")
                .role(Role.ROLE_PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        
        User activeUser2 = User.builder()
                .userName("active2")
                .email("active2@example.com")
                .phone("3003333334")
                .role(Role.ROLE_DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .build();
        
        User inactiveUser = User.builder()
                .userName("inactive1")
                .email("inactive1@example.com")
                .phone("3004444445")
                .role(Role.ROLE_PASSENGER)
                .status(false)
                .passwordHash("hashedPassword")
                .build();
        
        userRepository.save(activeUser1);
        userRepository.save(activeUser2);
        userRepository.save(inactiveUser);

        // When & Then
        mockMvc.perform(get("/api/v1/users/by-status")
                        .param("status", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].status", everyItem(is(true))))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}