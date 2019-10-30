package nl.thehyve.hypercubeapi.query.hypercube;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;
import nl.thehyve.hypercubeapi.query.dimension.Dimension;
import org.hibernate.Criteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.internal.CriteriaImpl;
import org.transmartproject.common.type.SortOrder;

import java.util.Map;

@Data @Builder
public class HypercubeQuery {
    private Criteria criteria;
    private ProjectionList projections;
    private Map params;
    private ImmutableMap<Dimension, SortOrder> actualSortOrder;

    CriteriaImpl getCriteriaImpl() {
        if (criteria instanceof CriteriaImpl) {
            return (CriteriaImpl) criteria;
        }
        throw new RuntimeException(
            String.format("Unexpected criteria type: %s", criteria.getClass().getCanonicalName()));
    }

}
