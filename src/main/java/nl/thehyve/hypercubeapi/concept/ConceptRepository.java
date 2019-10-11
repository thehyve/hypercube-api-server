package nl.thehyve.hypercubeapi.concept;

import nl.thehyve.hypercubeapi.concept.ConceptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptRepository extends JpaRepository<ConceptEntity, String> {
}
