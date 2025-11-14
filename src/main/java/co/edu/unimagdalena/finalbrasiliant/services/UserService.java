package co.edu.unimagdalena.finalbrasiliant.services;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;

public interface UserService {
	UserResponse create(UserCreateRequest request);
	UserResponse get(Long id);
	UserResponse update(Long id, UserUpdateRequest request);
	void delete(Long id);
	
	UserResponse getByUserName(String userName);
    UserResponse getByEmail(String email);
    UserResponse getByPhone(String phone);
    Page<UserResponse> getByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    List<UserResponse> getByRole(Role role);
    Page<UserResponse> getByStatus(Boolean status, Pageable pageable);
}
