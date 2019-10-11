package nl.thehyve.hypercubeapi.dimension;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DimensionRepository extends JpaRepository<DimensionEntity, Long> {

    Optional<DimensionEntity> findByName(String name);

}
