package nl.thehyve.hypercubeapi.type;

import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ValueTypeMapper {

    public Class valueTypeToClass(ValueType valueType) {
        switch (valueType) {
            case Text:
            case RawText:
                return String.class;
            case Number:
                return Double.class;
            case Date:
                return Date.class;
            default:
                throw new RuntimeException(String.format(
                    "Unsupported value type: %s. Should be one of [%s].",
                    valueType,
                    Arrays.stream(ValueType.values()).map(ValueType::getCode).collect(Collectors.joining(", ")))
                );
        }
    }

    public org.transmartproject.common.type.ValueType classToValueType(Class type) {
        if (String.class.isAssignableFrom(type)) {
            return org.transmartproject.common.type.ValueType.String;
        } else if (Double.class.isAssignableFrom(type) ||
            BigDecimal.class.isAssignableFrom(type) ||
            Float.class.isAssignableFrom(type)
        ) {
            return org.transmartproject.common.type.ValueType.Double;
        } else if (Number.class.isAssignableFrom(type)) {
            return org.transmartproject.common.type.ValueType.Int;
        } else if (Date.class.isAssignableFrom(type) || TemporalAccessor.class.isAssignableFrom(type)) {
            return org.transmartproject.common.type.ValueType.Timestamp;
        }
        return org.transmartproject.common.type.ValueType.Object;
    }

    public org.transmartproject.common.type.ValueType mapValueType(ValueType valueType) {
        switch (valueType) {
            case Text:
            case RawText:
                return org.transmartproject.common.type.ValueType.String;
            case Number:
                return org.transmartproject.common.type.ValueType.Double;
            case Date:
                return org.transmartproject.common.type.ValueType.Timestamp;
            default:
                throw new RuntimeException(String.format(
                    "Unsupported value type: %s. Should be one of [%s].",
                    valueType,
                    Arrays.stream(ValueType.values()).map(ValueType::getCode).collect(Collectors.joining(", ")))
                );
        }
    }

}
