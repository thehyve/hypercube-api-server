package nl.thehyve.hypercubeapi.patient;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientMappingRepository extends JpaRepository<PatientMappingEntity, PatientMappingEntity.PatientMappingId> {
}
