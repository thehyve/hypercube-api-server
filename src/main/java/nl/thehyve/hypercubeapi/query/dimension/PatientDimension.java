package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.patient.*;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import org.hibernate.criterion.Projections;

import java.util.*;

@Data
public class PatientDimension extends I2b2Dimension<Long, PatientEntity> {
    public static final String ALIAS = "patientId";

    Class elementType = PatientEntity.class;
    List elemFields = Arrays.asList("id", "subjectIds", "sex");

    String name = "patient";
    String alias = ALIAS;
    String columnName = "patient.id";
    String keyProperty = "id";
    ImplementationType implementationType = ImplementationType.TABLE;

    private final PatientRepository patientRepository;

    PatientDimension(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Long getKey(PatientEntity element) {
        return element.getId();
    }

    @Override
    public List<PatientEntity> resolveElements(List<Long> keys) {
        return patientRepository.findAllById(keys);
    }

    @Override
    public void selectIDs(HypercubeQuery query) {
        query.getProjections().add(Projections.property("patient"), getAlias());
    }

}
