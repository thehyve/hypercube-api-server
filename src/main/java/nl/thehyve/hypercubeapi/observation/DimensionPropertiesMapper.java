package nl.thehyve.hypercubeapi.observation;

import nl.thehyve.hypercubeapi.query.dimension.Dimension;
import nl.thehyve.hypercubeapi.type.ValueTypeMapper;
import org.mapstruct.factory.Mappers;
import org.transmartproject.common.dto.Field;
import org.transmartproject.common.type.ValueType;

import java.util.List;
import java.util.stream.Collectors;

public class DimensionPropertiesMapper {

    private static final ValueTypeMapper valueTypeMapper = Mappers.getMapper(ValueTypeMapper.class);

    static <K, V> DimensionProperties forDimension(Dimension<K, V> dimension) {
        ValueType valueType;
        List<Field> fields = null;
        if (dimension.isElementsSerializable()) {
            // Sparse dimensions are inlined, dense dimensions are referred to by indexes
            // (referring to objects in the footer message).
            valueType = valueTypeMapper.classToValueType(dimension.getElementType());
        } else {
            valueType = ValueType.Object;
            fields = dimension.getElementFields().stream()
                .map(property -> Field.builder()
                    .name(property.getName())
                    .type(valueTypeMapper.classToValueType(property.getType()))
                    .build())
                .collect(Collectors.toList());
        }
        return DimensionProperties.builder()
            .name(dimension.getName())
            .modifierCode(dimension.getModifierCode())
            .dimensionType(dimension.getDimensionType())
            .sortIndex(dimension.getSortIndex())
            .valueType(valueType)
            .fields(fields)
            .inline(dimension.getDensity() != null && !dimension.getDensity().isDense())
            .build();
    }

}
