/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.thehyve.hypercubeapi.type.Density;
import org.transmartproject.common.type.DimensionType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data @NoArgsConstructor
public abstract class DimensionImpl<KeyType, ElementType> implements Dimension<KeyType, ElementType> {

    private DimensionType dimensionType;
    private Density density;
    private Integer sortIndex;
    private String modifierCode;

    DimensionImpl(DimensionType dimensionType, Density density, Integer sortIndex, String modifierCode) {
        this.dimensionType = dimensionType;
        this.density = density;
        this.sortIndex = sortIndex;
        this.modifierCode = modifierCode;
    }

    public abstract String getName();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    KeyType getKey(Map<String, Object> map, String alias) {
        if (!map.containsKey(alias)) {
            throw new IllegalArgumentException(String.format(
                "Result map %s does not contain key %s", map, alias));
        }
        return (KeyType)map.get(alias);
    }

    @Override
    public ElementType resolveElement(KeyType key) {
        return this.resolveElements(Collections.singletonList(key)).get(0);
    }

    static boolean isSerializableType(String dimensionName, Class t) {
        if (t == null) {
            throw new RuntimeException(String.format("No type specified for dimension %s", dimensionName));
        }
        return Stream.of(Number.class, String.class, Date.class)
            .anyMatch((Class<?> c) -> c.isAssignableFrom(t));
    };

    public boolean isElementsSerializable() {
        return false;
    }

    public List<String> getElemFields() {
        return null;
    };

    public List<Property> getElementFields() {
        return getElemFields().stream().map(it -> {
            try {
                Field field = getElementType().getDeclaredField(it);
                if (field == null) {
                    throw new RuntimeException(String.format(
                        "Property %s does not exist on type %s", it, getElementType().getSimpleName()));
                }
                return new PropertyImpl<>(it, it, field.getType());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(String.format(
                    "Property %s does not exist on type %s", it, getElementType().getSimpleName()), e);
            }
        }).collect(Collectors.toUnmodifiableList());
    }

    void verify() {
        if (this.isElementsSerializable()) {
            if (!getName().equals("value") && !isSerializableType(getName(), getElementType())) {
                throw new RuntimeException(String.format(
                    "Dimension %s is marked serializable, but element type %s is not serializable.",
                    getName(), getElementType().getSimpleName()));
            }
        } else {
            if (getElemFields() == null) {
                throw new RuntimeException(String.format(
                    "Dimension %s is marked not serializable, but no element fields have been defined.",
                    getName()));
            }
        }
    }

}
