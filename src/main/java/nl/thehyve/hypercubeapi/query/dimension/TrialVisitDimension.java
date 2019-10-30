package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitEntity;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitRepository;

import java.util.Arrays;
import java.util.List;

@Data
public class TrialVisitDimension extends I2b2Dimension<Long, TrialVisitEntity> {

    public static final String ALIAS = "trialVisitId";

    Class elementType = TrialVisitEntity.class;
    List elemFields = Arrays.asList("id", "study", "relativeTimeLabel", "relativeTimeUnit", "relativeTime");
    String name = "trial visit";
    String alias = ALIAS;
    String columnName = "trialVisit.id";
    ImplementationType implementationType = ImplementationType.TABLE;

    private final TrialVisitRepository trialVisitRepository;

    TrialVisitDimension(TrialVisitRepository trialVisitRepository) {
        this.trialVisitRepository = trialVisitRepository;
    }

    @Override
    public Long getKey(TrialVisitEntity element) {
        return element.getId();
    }

    @Override
    public List<TrialVisitEntity> resolveElements(List<Long> keys) {
        return this.trialVisitRepository.findAllById(keys);
    }

    @Override
    public void selectIDs(HypercubeQuery query) {

    }

}
