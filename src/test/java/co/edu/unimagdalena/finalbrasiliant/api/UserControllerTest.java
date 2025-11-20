package co.edu.unimagdalena.finalbrasiliant.api;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest extends BaseTest {
    
    @MockitoBean
    UserService service;

    @Test
    void createUser_shouldReturn201AndLocation() throws Exception {
        var req = new UserCreateRequest("johndoe", "john@example.com", 
                "+573001234567", Role.ROLE_PASSENGER, "hashedPassword123");
        var resp = new UserResponse(1L, "johndoe", "john@example.com", 
                "+573001234567", Role.ROLE_PASSENGER, true, OffsetDateTime.now());

        when(service.create(any(UserCreateRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/users/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phone").value("+573001234567"))
                .andExpect(jsonPath("$.role").value("ROLE_PASSENGER"))
                .andExpect(jsonPath("$.status").value(true));

        verify(service).create(any(UserCreateRequest.class));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new UserResponse(1L, "johndoe", "john@example.com", 
                "+573001234567", Role.ROLE_PASSENGER, true, OffsetDateTime.now());

        when(service.get(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_PASSENGER"));

        verify(service).get(1L);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new UserUpdateRequest("johndoe_updated", "john.new@example.com", 
                "+573009876543", Role.ROLE_DRIVER, false, "newHashedPassword");
        var resp = new UserResponse(1L, "johndoe_updated", "john.new@example.com", 
                "+573009876543", Role.ROLE_DRIVER, false, OffsetDateTime.now());

        when(service.update(eq(1L), any(UserUpdateRequest.class))).thenReturn(resp);

        mvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("johndoe_updated"))
                .andExpect(jsonPath("$.email").value("john.new@example.com"))
                .andExpect(jsonPath("$.phone").value("+573009876543"))
                .andExpect(jsonPath("$.role").value("ROLE_DRIVER"))
                .andExpect(jsonPath("$.status").value(false));

        verify(service).update(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void getByUserName_shouldReturn200() throws Exception {
        var resp = new UserResponse(1L, "johndoe", "john@example.com", 
                "+573001234567", Role.ROLE_PASSENGER, true, OffsetDateTime.now());

        when(service.getByUserName("johndoe")).thenReturn(resp);

        mvc.perform(get("/api/v1/users/by-name")
                        .param("userName", "johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("johndoe"));

        verify(service).getByUserName("johndoe");
    }

    @Test
    void getByEmail_shouldReturn200() throws Exception {
        var resp = new UserResponse(1L, "johndoe", "john@example.com", 
                "+573001234567", Role.ROLE_PASSENGER, true, OffsetDateTime.now());

        when(service.getByEmail("john@example.com")).thenReturn(resp);

        mvc.perform(get("/api/v1/users/by-email")
                        .param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(service).getByEmail("john@example.com");
    }

    @Test
    void getByPhone_shouldReturn200() throws Exception {
        var resp = new UserResponse(1L, "johndoe", "john@example.com", 
                "+573001234567", Role.ROLE_PASSENGER, true, OffsetDateTime.now());

        when(service.getByPhone("+573001234567")).thenReturn(resp);

        mvc.perform(get("/api/v1/users/by-phone")
                        .param("phone", "+573001234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.phone").value("+573001234567"));

        verify(service).getByPhone("+573001234567");
    }

    @Test
    void getByCreatedAtBetween_shouldReturn200() throws Exception {
        var start = OffsetDateTime.now().minusDays(7);
        var end = OffsetDateTime.now();
        var users = List.of(
                new UserResponse(1L, "johndoe", "john@example.com", 
                        "+573001234567", Role.ROLE_PASSENGER, true, start.plusDays(1)),
                new UserResponse(2L, "janedoe", "jane@example.com", 
                        "+573007654321", Role.ROLE_DRIVER, true, start.plusDays(3))
        );
        var page = new PageImpl<>(users, PageRequest.of(0, 10), 2);

        when(service.getByCreatedAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class), any()))
                .thenReturn(page);

        mvc.perform(get("/api/v1/users/by-created_at")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(service).getByCreatedAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class), any());
    }

    @Test
    void getByRole_shouldReturn200() throws Exception {
        var users = List.of(
                new UserResponse(1L, "johndoe", "john@example.com", 
                        "+573001234567", Role.ROLE_PASSENGER, true, OffsetDateTime.now()),
                new UserResponse(2L, "janedoe", "jane@example.com", 
                        "+573007654321", Role.ROLE_PASSENGER, true, OffsetDateTime.now())
        );

        when(service.getByRole(Role.ROLE_PASSENGER)).thenReturn(users);

        mvc.perform(get("/api/v1/users/by-role")
                        .param("role", "ROLE_PASSENGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("ROLE_PASSENGER"))
                .andExpect(jsonPath("$[1].role").value("ROLE_PASSENGER"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getByRole(Role.ROLE_PASSENGER);
    }

    @Test
    void getByStatus_shouldReturn200() throws Exception {
        var users = List.of(
                new UserResponse(1L, "johndoe", "john@example.com", 
                        "+573001234567", Role.ROLE_PASSENGER, true, OffsetDateTime.now()),
                new UserResponse(2L, "janedoe", "jane@example.com", 
                        "+573007654321", Role.ROLE_DRIVER, true, OffsetDateTime.now())
        );
        var page = new PageImpl<>(users, PageRequest.of(0, 10), 2);

        when(service.getByStatus(eq(true), any())).thenReturn(page);

        mvc.perform(get("/api/v1/users/by-status")
                        .param("status", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].status").value(true))
                .andExpect(jsonPath("$.content[1].status").value(true));

        verify(service).getByStatus(eq(true), any());
    }
}