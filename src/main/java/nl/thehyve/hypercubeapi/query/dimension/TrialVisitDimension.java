package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitEntity;

import java.util.Arrays;
import java.util.List;

@Data
public class TrialVisitDimension extends I2b2Dimension {

    Class elemType = TrialVisitEntity.class;
    List elemFields = Arrays.asList("id", "studyId", "relTimeLabel", "relTimeUnit", "relTime");
    String name = "trial visit";
    String alias = "trialVisitId";
    String columnName = "trialVisit.id";
    String keyProperty = "id";
    ImplementationType implementationType = ImplementationType.TABLE;

}
