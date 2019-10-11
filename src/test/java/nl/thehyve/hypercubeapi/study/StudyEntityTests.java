package nl.thehyve.hypercubeapi.study;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class StudyEntityTests {

    @Test
    public void testStudyEquals() {
        EqualsVerifier
            .forClass(StudyEntity.class)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.SURROGATE_KEY)
            .verify();
    }

}
