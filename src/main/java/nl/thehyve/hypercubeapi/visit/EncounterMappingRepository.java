package nl.thehyve.hypercubeapi.visit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterMappingRepository extends JpaRepository<EncounterMappingEntity, EncounterMappingEntity.EncounterMappingId> {
}
