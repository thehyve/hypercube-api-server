package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.patient.PatientEntity;

import java.util.Arrays;
import java.util.List;

@Data
public class PatientDimension extends I2b2Dimension {
    Class elemType = PatientEntity.class;
    List elemFields = Arrays.asList("id", "trial", "inTrialId", "subjectIds", "birthDate", "deathDate",
                       "age", "race", "maritalStatus", "religion", "sexCd", "sex");

    String name = "patient";
    String alias = "patientId";
    String columnName = "patient.id";
    String keyProperty = "id";
    ImplementationType implementationType = ImplementationType.TABLE;
}
