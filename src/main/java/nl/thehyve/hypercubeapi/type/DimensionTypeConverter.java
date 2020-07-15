package nl.thehyve.hypercubeapi.type;

import org.transmartproject.common.type.DimensionType;

import javax.persistence.*;

@Converter(autoApply = true)
public class DimensionTypeConverter implements AttributeConverter<DimensionType, String> {

    @Override
    public String convertToDatabaseColumn(DimensionType type) {
        return type.name();
    }

    @Override
    public DimensionType convertToEntityAttribute(String dbData) {
        return DimensionType.forName(dbData);
    }

}
