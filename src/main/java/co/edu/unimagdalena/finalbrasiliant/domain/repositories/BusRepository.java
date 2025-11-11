package co.edu.unimagdalena.finalbrasiliant.domain.repositories;


import co.edu.unimagdalena.finalbrasiliant.domain.enums.BusStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByPlate(String plate);
    List<Bus> findByStatus(BusStatus status);
    List<Bus> findByCapacityGreaterThanEqual(Integer capacity);
    Page<Bus> findByStatus(BusStatus status, Pageable pageable);

    // Buscar buses por varias amenidades especificadas
    @Query("""
        SELECT b FROM Bus b 
        WHERE SIZE(b.amenities) >= :amenitiesCount
        AND (
            SELECT COUNT(a) FROM Bus b2 
            JOIN b2.amenities a 
            WHERE b2.id = b.id AND a IN :amenities
        ) = :amenitiesCount
    """)
    List<Bus> findByAmenities(@Param("amenities") Set<String> amenities,
                              @Param("amenitiesCount") long amenitiesCount);
}
