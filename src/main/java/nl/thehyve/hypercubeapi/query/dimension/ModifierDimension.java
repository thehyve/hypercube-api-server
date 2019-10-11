package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.type.ValueType;
import nl.thehyve.hypercubeapi.type.ValueTypeMapper;
import org.apache.commons.lang.NotImplementedException;
import org.mapstruct.factory.Mappers;
import org.transmartproject.common.type.DimensionType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ModifierDimension is currently only implemented for serializable types. If desired, the implementation could be
 * extended to also support modifiers that link to other tables, thus leading to modifier dimensions with compound
 * element types
 * @see {@link nl.thehyve.hypercubeapi.dimension.DimensionEntity} for how the instances of modifier dimensions are stored in the database.
 */
@Data
public class ModifierDimension extends DimensionImpl {
    private static Map<String, ModifierDimension> byName = new LinkedHashMap<>();
    private static Map<String, ModifierDimension> byCode = new LinkedHashMap<>();

    synchronized static ModifierDimension get(String name, String modifierCode, ValueType valueType,
                                              DimensionType dimensionType, Integer sortIndex) {
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

        ModifierDimension dim = new ModifierDimension(name, modifierCode, valueType, dimensionType, sortIndex);
        dim.verify();
        byName.put(name, dim);
        byCode.put(modifierCode, dim);

        return dim;
    }

    private ModifierDimension(String name, String modifierCode, ValueType valueType,
                              DimensionType dimensionType, Integer sortIndex) {
        super(dimensionType, sortIndex, modifierCode);
        this.name = name;
        this.dimensionType = dimensionType;
        this.sortIndex = sortIndex;
        this.modifierCode = modifierCode;
        this.valueType = valueType;
        Class elementType = Mappers.getMapper(ValueTypeMapper.class).valueTypeToClass(valueType);
        if (!isSerializableType(elementType)) {
            throw new NotImplementedException(
                "Support for non-serializable modifier dimensions is not implemented: " + name);
        }
        this.elemType = elementType;
    }

    public static final String modifierCodeField = "modifierCd";

    final ValueType valueType;
    final Class elemType;
    final String name;
    final String modifierCode;
    final DimensionType dimensionType;
    final Integer sortIndex;
    ImplementationType implementationType = ImplementationType.MODIFIER;

    @Override
    public String toString() {
        return String.format("%s(name: '%s', code: '%s')",
            this.getClass().getSimpleName(), getName(), getModifierCode());
    }

}
