package nl.thehyve.hypercubeapi.query.dimension;

import java.util.Map;

abstract class I2b2NullablePKDimension<KeyType, ElementType> extends I2b2Dimension<KeyType, ElementType> {

    abstract KeyType getNullValue();

    public KeyType getElementKey(Map<String, Object> result) {
        KeyType res = getKey(result, getAlias());
        return res == getNullValue() ? null : res;
    }

}
