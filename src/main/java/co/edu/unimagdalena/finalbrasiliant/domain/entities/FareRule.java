package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.DynamicPricing;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "fare_rules")

public class FareRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fare_rule_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_stop_id")
    private Stop fromStop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_stop_id")
    private Stop toStop;

    @Column(nullable = false, precision = 8, scale = 2, name = "base_price")
    private BigDecimal basePrice;

    @ElementCollection
    @Builder.Default
    @CollectionTable(name = "fare_rule_discounts", joinColumns = @JoinColumn(name = "fare_rule_id"))
    @Column(name = "discounts")
    private Map<String, Integer> discounts = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "dynamic_pricing")
    private DynamicPricing dynamicPricing;

}
