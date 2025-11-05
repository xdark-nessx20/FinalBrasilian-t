package co.edu.unimagdalena.finalbrasiliant.domain.repositories;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unimagdalena.finalbrasiliant.domain.entities.Bus;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByPlate(String plate);
    List<Bus> findByStatus(Boolean status);
    List<Bus> findByCapacityGreaterThanEqual(Integer capacity);
    Page<Bus> findByStatus(Boolean status, Pageable pageable);
    long countByStatusTrue();

    // Buscar buses que contengan una amenidad específica
    @Query("""
        SELECT b FROM Bus b 
        WHERE :amenity MEMBER OF b.amenities
    """)
    List<Bus> findByAmenity(@Param("amenity") String amenity);
}
