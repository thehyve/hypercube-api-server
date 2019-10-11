package nl.thehyve.hypercubeapi.query.dimension;

abstract class I2b2Dimension extends DimensionImpl implements AliasAwareDimension {

    public abstract String getAlias();
    public abstract String getColumnName();
    // The property in the dimension table on which observation_facts is to be joined
    String getJoinProperty() { return "id"; }

}
