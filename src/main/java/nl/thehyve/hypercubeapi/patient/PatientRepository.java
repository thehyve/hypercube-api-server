package nl.thehyve.hypercubeapi.patient;

import nl.thehyve.hypercubeapi.patient.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
}
