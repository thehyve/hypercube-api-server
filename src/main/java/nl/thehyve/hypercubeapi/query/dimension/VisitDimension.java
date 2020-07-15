package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import nl.thehyve.hypercubeapi.visit.*;
import org.hibernate.criterion.Projections;

import java.util.*;

@Data
public class VisitDimension extends I2b2NullablePKDimension<Long, VisitEntity> {
    public static final String ALIAS = "visit";

    Class elementType = VisitEntity.class;
    List elemFields = Arrays.asList(
        "id", "patient", "activeStatus", "startDate", "endDate", "inOut", "location", "lengthOfStay",
        "encounterIds");
    String name = "visit";
    String alias = ALIAS;
    String columnName = "encounterId";
    Long nullValue = -1L;
    ImplementationType implementationType = ImplementationType.VISIT;

    private final VisitRepository visitRepository;

    VisitDimension(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public List<VisitEntity> resolveElements(List<Long> keys) {
        return this.visitRepository.findAllById(keys);
    }

    @Override
    public Long getKey(VisitEntity element) {
        return element.getId();
    }

    @Override
    public void selectIDs(HypercubeQuery query) {
        query.getProjections().add(Projections.property(columnName), getAlias());
    }

}
