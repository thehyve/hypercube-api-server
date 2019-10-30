package nl.thehyve.hypercubeapi.query.dimension;

import nl.thehyve.hypercubeapi.observation.ObservationEntity;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import nl.thehyve.hypercubeapi.type.Density;
import org.transmartproject.common.type.DimensionType;

import java.util.List;
import java.util.Map;

public interface Dimension<KeyType, ElementType> {

    String getModifierCode();

    DimensionType getDimensionType();

    Integer getSortIndex();

    /**
     * Metadata about the fetching method for the dimension.
     * The dimension may be represented by a dimension table (<code>TABLE</code>),
     * a column in the {@link ObservationEntity} table (<code>COLUMN</code>) or as
     * a modifier, which means that the data is stored in another, related, row
     * in the {@link ObservationEntity} table (<code>MODIFIER</code>).
     */
    enum ImplementationType {
        TABLE,
        COLUMN,
        VALUE,
        MODIFIER,
        STUDY,
        VISIT
    }

    /**
     * @return true if the elements of this dimension are serializable, meaning they are of type String, Date, or
     * Number.
     * Note that 'serializable' within this interface is a subset of the java.io.Serializable meaning. Serializable
     * here is limited to types that can be directly serialized by the rest-api, i.e. String, Date, or subclasses of
     * Number.
     */
    boolean isElementsSerializable();

    /**
     * @return if the element type is serializable, return the type (String, Date, or a subclass of Number). If the
     * elements are not serializable, returns null.
     */
    Class<ElementType> getElementType();

    /**
     * @return for dimensions with non-serializable elements, an (ordered) immutable map with as keys the field names
     * that should be used for serialization, and as values instances of Property that can be used to retrieve the
     * property from an element.
     * If the elements of this dimension are serializable (according to getElementsSerializable()), this method returns
     * null.
     */
    List<Property> getElementFields();

    /**
     * Get an element's key, i.e. a simple value that uniquely identifies the element.
     * @param element
     * @return a simple object (Number, String, or Date) that is unique for this element and thus identifies it.
     */
    KeyType getKey(ElementType element);

    String getName();

    Density getDensity();

    List<ElementType> resolveElements(List<KeyType> keys);

    KeyType getElementKey(Map<String, Object> result);

    ElementType resolveElement(KeyType key);

    ImplementationType getImplementationType();

    void selectIDs(HypercubeQuery query);

}
