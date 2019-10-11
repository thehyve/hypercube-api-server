package nl.thehyve.hypercubeapi.observation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservationRepository extends JpaRepository<ObservationEntity, ObservationEntity.ObservationId> {
}
