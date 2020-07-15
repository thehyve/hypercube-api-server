package nl.thehyve.hypercubeapi.query.dimension;

import lombok.*;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import org.apache.commons.lang.NotImplementedException;

import java.util.*;

/**
 * This is a fake dimension. It is only used in the query builder.
 */
@Data
public class ValueDimension extends DimensionImpl<Object, Object> {
    String name = "value";
    Class elementType = Object.class;
    ImplementationType implementationType = ImplementationType.VALUE;
    @Getter
    boolean elementsSerializable = true;

    @Override
    public Object getKey(Object element) {
        throw new NotImplementedException("Not available for value dimension");
    }

    @Override
    public List<Object> resolveElements(List<Object> keys) {
        throw new NotImplementedException("Not available for value dimension");
    }

    @Override
    public Object getElementKey(Map<String, Object> result) {
        throw new NotImplementedException("Not available for value dimension");
    }

    @Override
    public void selectIDs(HypercubeQuery query) {

    }

}
