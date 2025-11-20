package co.edu.unimagdalena.finalbrasiliant.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unimagdalena.finalbrasiliant.domain.entities.User;
import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;

import java.time.OffsetDateTime;


public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserName(String userName);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

	Optional<User> findByPhone(String Phone);
	Page<User> findAllByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
	List<User> findAllByRole(Role role);
	Page<User> findAllByStatus(Boolean status, Pageable pageable);
}
