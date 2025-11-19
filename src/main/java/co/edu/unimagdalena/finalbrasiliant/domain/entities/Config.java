package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "configs")
public class Config {
    @Id
    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private BigDecimal value;
}
