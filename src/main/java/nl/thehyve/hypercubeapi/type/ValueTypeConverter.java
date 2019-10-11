package nl.thehyve.hypercubeapi.type;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ValueTypeConverter implements AttributeConverter<ValueType, String> {

    @Override
    public String convertToDatabaseColumn(ValueType valueType) {
        return valueType.getCode();
    }

    @Override
    public ValueType convertToEntityAttribute(String dbData) {
        return ValueType.forCode(dbData);
    }

}
