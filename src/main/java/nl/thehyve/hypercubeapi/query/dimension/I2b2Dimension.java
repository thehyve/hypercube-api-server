package nl.thehyve.hypercubeapi.query.dimension;

import java.util.Map;

abstract class I2b2Dimension<KeyType, ElementType> extends DimensionImpl<KeyType, ElementType> implements AliasAwareDimension<KeyType, ElementType> {

    public abstract String getAlias();
    public abstract String getColumnName();
    // The property in the dimension table on which observation_facts is to be joined
    String getJoinProperty() { return "id"; }

    @Override
    public KeyType getElementKey(Map<String, Object> result) {
        return getKey(result, getAlias());
    }

}
