package co.edu.unimagdalena.finalbrasiliant.services;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;

public interface UserService {
	userResponse create(userCreateRequest request);
	userResponse get(Long id);
	userResponse update(Long id, userUpdateRequest request);
	void delete(Long id);
	
	userResponse getByUserName(String userName);
    userResponse getByEmail(String email);
    userResponse getByPhone(String phone);
    Page<userResponse> getByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    List<userResponse> getByRole(Role role);
    Page<userResponse> getByStatus(Boolean status, Pageable pageable);
}
