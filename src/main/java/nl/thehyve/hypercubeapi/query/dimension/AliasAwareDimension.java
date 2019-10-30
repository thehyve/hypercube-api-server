package nl.thehyve.hypercubeapi.query.dimension;

public interface AliasAwareDimension<KeyType, ElementType> extends Dimension<KeyType, ElementType> {
    String getAlias();
}
