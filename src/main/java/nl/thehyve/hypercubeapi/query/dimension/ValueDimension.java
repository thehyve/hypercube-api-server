package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;

/**
 * This is a fake dimension. It is only used in the query builder
 */
@Data
public class ValueDimension extends DimensionImpl {
    String name = "value";
    Class elemType = Object.class;
    ImplementationType implementationType = ImplementationType.VALUE;

}
