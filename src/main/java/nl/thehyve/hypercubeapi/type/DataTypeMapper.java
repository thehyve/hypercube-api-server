package nl.thehyve.hypercubeapi.type;

import org.mapstruct.Mapper;
import org.transmartproject.common.type.DataType;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class DataTypeMapper {

    public Class dataTypeToClass(DataType dataType) {
        switch (dataType) {
            case Text:
            case String:
                return String.class;
            case Numeric:
                return Double.class;
            case Date:
                return Date.class;
            case Id:
            case Object:
                return Long.class;
            case Collection:
                return List.class;

            default:
                throw new RuntimeException(String.format(
                    "Unsupported data type: %s. Should be one of [%s].",
                    dataType,
                    Arrays.stream(DataType.values()).map(DataType::toString).collect(Collectors.joining(", ")))
                );
        }
    }

}
