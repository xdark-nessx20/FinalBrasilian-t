package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long id;
	
	@Column(nullable = false, name = "user_name")
	private String userName;	
	
	@Column(nullable = false, unique = true)
    private String email;
	
	@Column(nullable = false, unique = true, length = 20)
	private String phone;

	@Column(nullable = false)
    @Enumerated(EnumType.STRING)
	@Builder.Default
    private Role role = Role.PASSENGER;
	
	@Column(nullable = false)
	private Boolean status;
	
	@Column(nullable = false)
	private String passwordHash;
	
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

}
