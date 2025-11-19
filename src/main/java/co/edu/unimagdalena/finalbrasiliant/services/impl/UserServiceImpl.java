package co.edu.unimagdalena.finalbrasiliant.services.impl;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unimagdalena.finalbrasiliant.api.dto.UserDTO.*;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalbrasiliant.exceptions.NotFoundException;
import co.edu.unimagdalena.finalbrasiliant.services.UserService;
import co.edu.unimagdalena.finalbrasiliant.services.mappers.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepo;
	private final UserMapper userMapper;
	
	@Transactional
	@Override
	public UserResponse create(UserCreateRequest request) {
	    User user = userMapper.toEntity(request);
	    User saved = userRepo.save(user);
	    return userMapper.toResponse(saved);
	}
	
	@Override
    public UserResponse get(Long id) {
        return userRepo.findById(id).map(userMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));
    }
	
	@Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        var user = userRepo.findById(id).orElseThrow(() -> new NotFoundException("user %d not found.".formatted(id)));
        userMapper.patch(user, request);
        return userMapper.toResponse(userRepo.save(user));
    }
	
	@Override
    @Transactional
    public void delete(Long id) {
        userRepo.deleteById(id);
    }

	@Override
	public UserResponse getByUserName(String userName) {
		return userRepo.findByUserName(userName).map(userMapper::toResponse).orElseThrow(
				()-> new NotFoundException("user with the name %s not found".formatted(userName)));
	}
	
	@Override
	public UserResponse getByEmail(String email) {
		return userRepo.findByEmail(email).map(userMapper::toResponse).orElseThrow(
				()-> new NotFoundException("user with the e-mail %s not found".formatted(email)));
	}
	
	@Override
	public UserResponse getByPhone(String phone) {
		return userRepo.findByPhone(phone).map(userMapper::toResponse).orElseThrow(
				()-> new NotFoundException("user with the phone %s not found".formatted(phone)));
	}
	
	@Override
	public Page<UserResponse> getByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable){
		Page<User> users = userRepo.findAllByCreatedAtBetween(start, end, pageable);
		if (users.isEmpty()) {
		    throw new NotFoundException("No users found between %s and %s".formatted(start, end));
		}
		return users.map(userMapper::toResponse);
	}
	
	@Override
    public List<UserResponse> getByRole(Role role){
    	List<User> users = userRepo.findAllByRole(role);
		if (users.isEmpty()) {
		    throw new NotFoundException("No users found with role  %s".formatted(role));
		}
		return users.stream().map(userMapper::toResponse).toList();
    }
    
    @Override
    public Page<UserResponse> getByStatus(Boolean status, Pageable pageable) {
        Page<User> users = userRepo.findAllByStatus(status, pageable);
        if (users.isEmpty()) {
            String readableStatus = status ? "Active" : "Innactive";
            throw new NotFoundException("No users with status %s".formatted(readableStatus));
        }
        return users.map(userMapper::toResponse);
    }
}
