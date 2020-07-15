package nl.thehyve.hypercubeapi.query.dimension;

import lombok.*;
import nl.thehyve.hypercubeapi.dimension.DimensionEntity;
import nl.thehyve.hypercubeapi.query.hypercube.*;
import nl.thehyve.hypercubeapi.type.*;
import org.apache.commons.lang.NotImplementedException;
import org.mapstruct.factory.Mappers;
import org.transmartproject.common.type.DimensionType;

import java.math.BigDecimal;
import java.util.*;

/**
 * ModifierDimension is currently only implemented for serializable types. If desired, the implementation could be
 * extended to also support modifiers that link to other tables, thus leading to modifier dimensions with compound
 * element types
 * @see {@link DimensionEntity} for how the instances of modifier dimensions are stored in the database.
 */
@Data
public class ModifierDimension extends DimensionImpl<String, Object> {

    public static final String modifierCodeField = "modifierCd";

    private static Map<String, ModifierDimension> byName = new LinkedHashMap<>();
    private static Map<String, ModifierDimension> byCode = new LinkedHashMap<>();

    synchronized static ModifierDimension get(String name, String modifierCode, ValueType valueType,
                                              DimensionType dimensionType, Density density, Integer sortIndex) {
        if (byName.containsKey(name)) {
            ModifierDimension dim = byName.get(name);
            assert dim == byCode.get(dim.modifierCode);
            if (modifierCode.equals(dim.modifierCode)) {
                return dim;
            }

            throw new RuntimeException(String.format(
                "Attempting to create a modifier dimension with modifier code %s while an" +
                " identical modifier dimension with different properties already exists: %s",
                modifierCode, dim));
        }
        assert !byCode.containsKey(modifierCode);

        ModifierDimension dim = new ModifierDimension(name, modifierCode, valueType, dimensionType, density, sortIndex);
        dim.verify();
        byName.put(name, dim);
        byCode.put(modifierCode, dim);

        return dim;
    }

    final ValueType valueType;
    final Class elementType;
    final String name;
    final String modifierCode;
    final DimensionType dimensionType;
    final Integer sortIndex;
    ImplementationType implementationType = ImplementationType.MODIFIER;
    @Getter
    boolean elementsSerializable = true;

    private ModifierDimension(String name, String modifierCode, ValueType valueType,
                              DimensionType dimensionType, Density density, Integer sortIndex) {
        super(dimensionType, density, sortIndex, modifierCode);
        this.name = name;
        this.dimensionType = dimensionType;
        this.sortIndex = sortIndex;
        this.modifierCode = modifierCode;
        this.valueType = valueType;
        Class elementType = Mappers.getMapper(ValueTypeMapper.class).valueTypeToClass(valueType);
        if (!isSerializableType(getName(), elementType)) {
            throw new NotImplementedException(
                "Support for non-serializable modifier dimensions is not implemented: " + name);
        }
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return String.format("%s(name: '%s', code: '%s')",
            this.getClass().getSimpleName(), getName(), getModifierCode());
    }

    public void addModifierValue(Map<String, Object> result, ProjectionMap modifierRow) {
        assert modifierRow.get(modifierCodeField) == modifierCode;
        Object modifierValue = ObservationValueMapper.observationFactValue(
            (ValueType) modifierRow.get("valueType"),
            (String) modifierRow.get("textValue"),
            (BigDecimal) modifierRow.get("numericalValue"),
            (String) modifierRow.get("rawTextValue"));

        if (result.putIfAbsent(name, modifierValue) != null) {
            throw new RuntimeException(String.format("%s already used as an alias or as a different modifier", name));
        }
    }

    @Override
    public String getKey(Object element) {
        return null;
    }

    @Override
    public List<Object> resolveElements(List<String> keys) {
        throw new NotImplementedException(String.format(
            "Resolve elements should not be called on modifier dimension '%s'", name));
    }

    @Override
    public String getElementKey(Map<String, Object> result) {
        return (String)result.get(getName());
    }

    @Override
    public void selectIDs(HypercubeQuery query) {

    }

}
