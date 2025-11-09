package co.edu.unimagdalena.finalbrasiliant.services;

import java.util.List;

import org.springframework.data.domain.Page;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;

public interface UserService {
	userResponse create(userCreateRequest request);
	userResponse get(Long id);
	userResponse update(Long id, userUpdateRequest request);
	userResponse delete(Long id);
	
	List<userResponse> getByUserName(String userName);
	userResponse getByEmail(String email);
	Page<userResponse> getByRole(Role role);
}
