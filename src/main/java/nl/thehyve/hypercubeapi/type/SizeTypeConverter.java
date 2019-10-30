package nl.thehyve.hypercubeapi.type;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class SizeTypeConverter implements AttributeConverter<Size, String> {

    @Override
    public String convertToDatabaseColumn(Size size) {
        return size.getName();
    }

    @Override
    public Size convertToEntityAttribute(String dbData) {
        return Size.forName(dbData);
    }

}
