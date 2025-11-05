package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import co.edu.unimagdalena.finalbrasiliant.domain.enums.ParcelStatus;
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
@Table(name = "parcels")
public class Parcel {
    @Id
    @Column(name = "parcel_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_stop_id")
    private Stop fromStop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_stop_id")
    private Stop toStop;

    @Column(precision = 8, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ParcelStatus status;

    //Sender and receiver may be not users, so aja...
    @Column(nullable = false, name = "sender_name")
    private String senderName;

    @Column(nullable = false, unique = true, length = 15, name = "sender_phone")
    private String senderPhone;

    @Column(nullable = false, name = "receiver_name")
    private String receiverName;

    @Column(nullable = false, unique = true, length = 15, name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "delivery_OTP", length = 10, unique = true)
    private String deliveryOTP;
}
