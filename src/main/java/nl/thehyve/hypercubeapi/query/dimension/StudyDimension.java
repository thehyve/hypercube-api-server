package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.query.HibernateCriteriaQueryBuilder;
import nl.thehyve.hypercubeapi.study.StudyEntity;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;

import java.util.Arrays;
import java.util.List;

@Data
public class StudyDimension extends I2b2Dimension {
    Class elemType = StudyEntity.class;
    List elemFields = Arrays.asList("name");
    String name = "study";
    String alias = "studyName";

    public String getColumnName() {
        throw new UnsupportedOperationException();
    }
    String keyProperty = "name";
    ImplementationType implementationType = ImplementationType.STUDY;

    public DetachedCriteria selectDimensionElements(DetachedCriteria criteria) {
        criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);
        criteria.setProjection(Projections.property("trialVisit"));

        DetachedCriteria dimensionCriteria = DetachedCriteria.forClass(StudyEntity.class, "study");
        dimensionCriteria.createAlias("trialVisits", "trialVisits");
        dimensionCriteria.add(Subqueries.propertyIn("trialVisits.id", criteria));
        return dimensionCriteria;
    }

}
