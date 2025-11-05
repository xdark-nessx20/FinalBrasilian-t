package co.edu.unimagdalena.finalbrasiliant.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assignments")
public class Assignment {
    @Id
    @Column(name = "assignment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dispatcher_id")
    private User dispatcher;

    @Column(name = "check_list_ok")
    private Boolean checkListOk;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;
}
