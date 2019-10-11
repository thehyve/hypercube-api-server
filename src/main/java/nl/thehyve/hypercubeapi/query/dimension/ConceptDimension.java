package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.concept.ConceptEntity;

import java.util.Arrays;
import java.util.List;

@Data
public class ConceptDimension extends I2b2NullablePKDimension<String> {
    Class elemType = ConceptEntity.class;
    List elemFields = Arrays.asList("conceptPath", "conceptCode", "name");
    String joinProperty = "conceptCode";
    String name = "concept";
    String alias = "conceptCode";
    String columnName = "conceptCode";
    String keyProperty = "conceptCode";
    String nullValue = "@";
    // ObservationFact.conceptCode is a string, not an i2b2.ConceptDimension
    ImplementationType implementationType = ImplementationType.COLUMN;

}
