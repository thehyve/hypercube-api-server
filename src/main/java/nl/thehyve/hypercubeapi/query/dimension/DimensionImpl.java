/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.thehyve.hypercubeapi.observation.ObservationEntity;
import org.transmartproject.common.type.DimensionType;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Data @NoArgsConstructor
public abstract class DimensionImpl {

    private DimensionType dimensionType;
    private Integer sortIndex;
    private String modifierCode;

    DimensionImpl(DimensionType dimensionType, Integer sortIndex, String modifierCode) {
        this.dimensionType = dimensionType;
        this.sortIndex = sortIndex;
        this.modifierCode = modifierCode;
    }

    public abstract String getName();

    /** The internal element type */
    protected abstract Class getElemType();

    abstract ImplementationType getImplementationType();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    static boolean isSerializableType(Class t) {
        return Stream.of(Number.class, String.class, Date.class)
            .anyMatch((Class<?> c) -> c.isAssignableFrom(t));
    };

    /**
     * Differences between serializable and non-serializable element types are implemented by extending the
     * SerializableElemDim trait. This method verifies that the element type is consistent with the usage of this trait.
     */
    final void verify() {
        assert getElemType() != null;
    }

    static<ELKey, ELT> void sort(List<ELT> res, List<ELKey> elementKeys, String property) {
        if (res.size() > 0) {
            Map<ELKey, ELT> ids = new HashMap<>(res.size(), 1.0f);
            for (ELT object: res) {
                ids.put(((Map<String, ELKey>)object).get(property), object);
            }
            res.clear();
            for (ELKey key: elementKeys) {
                res.add(ids.get(key));
            }
        }
    }

    /**
     * Metadata about the fetching method for the dimension.
     * The dimension may be represented by a dimension table (<code>TABLE</code>),
     * a column in the {@link ObservationEntity} table (<code>COLUMN</code>) or as
     * a modifier, which means that the data is stored in another, related, row
     * in the {@link ObservationEntity} table (<code>MODIFIER</code>).
     */
    public enum ImplementationType {
        TABLE,
        COLUMN,
        VALUE,
        MODIFIER,
        STUDY,
        VISIT
    }
}

// Nullable primary key
