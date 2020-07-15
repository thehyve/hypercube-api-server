package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.query.HibernateCriteriaQueryBuilder;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import nl.thehyve.hypercubeapi.study.*;
import org.hibernate.criterion.*;

import java.util.*;

@Data
public class StudyDimension extends I2b2Dimension<String, StudyEntity> {

    public static final String ALIAS = "studyName";

    Class elementType = StudyEntity.class;
    List elemFields = Arrays.asList("studyId");
    String name = "study";
    String alias = ALIAS;

    public String getColumnName() {
        throw new UnsupportedOperationException();
    }
    String keyProperty = "studyId";
    ImplementationType implementationType = ImplementationType.STUDY;

    public DetachedCriteria selectDimensionElements(DetachedCriteria criteria) {
        criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);
        criteria.setProjection(Projections.property("trialVisit"));

        DetachedCriteria dimensionCriteria = DetachedCriteria.forClass(StudyEntity.class, "study");
        dimensionCriteria.createAlias("trialVisits", "trialVisits");
        dimensionCriteria.add(Subqueries.propertyIn("trialVisits.id", criteria));
        return dimensionCriteria;
    }

    private final StudyRepository studyRepository;

    StudyDimension(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    @Override
    public String getKey(StudyEntity element) {
        return element.getStudyId();
    }

    @Override
    public List<StudyEntity> resolveElements(List<String> keys) {
        return this.studyRepository.findAllByStudyIdIsIn(keys);
    }

    @Override
    public void selectIDs(HypercubeQuery query) {
        query.getCriteria()
            .createAlias("trialVisit", TrialVisitDimension.ALIAS)
            .createAlias(TrialVisitDimension.ALIAS + ".study", "study");
        // FIXME query.getProjections().add(Projections.property("study"), getAlias());
    }

}
