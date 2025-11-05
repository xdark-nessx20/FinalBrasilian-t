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
@Table(name = "baggages")
public class Baggage {
    @Id
    @Column(name = "baggage_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(name = "weigth_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(precision = 8, scale = 2)
    private BigDecimal fee;

    @Column(name = "tag_code", unique = true)
    private String tagCode;
}
