package nl.thehyve.hypercubeapi.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import nl.thehyve.hypercubeapi.concept.ConceptEntity;
import nl.thehyve.hypercubeapi.observation.ObservationEntity;
import nl.thehyve.hypercubeapi.patient.PatientEntity;
import nl.thehyve.hypercubeapi.patient.PatientMappingEntity;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitEntity;
import org.junit.Test;
import org.transmartproject.common.type.Sex;

public class DomainTests {

    @Test
    public void testPatientEquals() {
        EqualsVerifier
            .forClass(PatientEntity.class)
            .withPrefabValues(PatientMappingEntity.class,
                PatientMappingEntity.builder().encryptedIdSource("SUBJ_ID").encryptedId("Test 1").build(),
                PatientMappingEntity.builder().encryptedIdSource("SUBJ_ID").encryptedId("TEST ABC").build())
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.SURROGATE_KEY)
            .verify();
    }

    @Test
    public void testConceptEquals() {
        EqualsVerifier
            .forClass(ConceptEntity.class)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.SURROGATE_KEY)
            .verify();
    }

    @Test
    public void testObservationIdEquals() {
        EqualsVerifier
            .forClass(ObservationEntity.ObservationId.class)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.SURROGATE_KEY)
            .verify();
    }

    @Test
    public void testObservationEquals() {
        EqualsVerifier
            .forClass(ObservationEntity.class)
            .withPrefabValues(PatientEntity.class,
                PatientEntity.builder().id(1L).sex(Sex.Male).build(),
                PatientEntity.builder().id(2L).sex(Sex.Female).build())
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.SURROGATE_KEY)
            .verify();
    }

    @Test
    public void testTrialVisitEquals() {
        EqualsVerifier
            .forClass(TrialVisitEntity.class)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.SURROGATE_KEY)
            .verify();
    }

}
