package nl.thehyve.hypercubeapi.query.dimension;

abstract class I2b2NullablePKDimension<ELKey> extends I2b2Dimension {

    abstract ELKey getNullValue();

}
