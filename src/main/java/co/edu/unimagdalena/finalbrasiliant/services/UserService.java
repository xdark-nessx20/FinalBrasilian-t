package co.edu.unimagdalena.finalbrasiliant.services;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;

public interface UserService {
	userResponse create(userCreateRequest request);
	userResponse get(Long id);
	userResponse update(Long id, userUpdateRequest request);
	userResponse delete(Long id);
	
	Optional<userResponse> getByUserName(String userName);
    Optional<userResponse> getByEmail(String email);
    Optional<userResponse> getByPhone(String phone);
    Page<userResponse> getByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    List<userResponse> getByRole(Role role);
    Page<userResponse> getByStatus(Boolean status, Pageable pageable);
}
