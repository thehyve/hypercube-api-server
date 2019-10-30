package nl.thehyve.hypercubeapi.query.dimension;

/**
 * A property descriptor returned from getElementFields()
 */
public interface Property {

    /** The name to use for this property externally, e.g. for serialization */
    String getName();

    /** The type of this property. This is the return type for `get` */
    Class getType();

    /** Given an element, return the value for this property */
    Object get(Object element);

}
