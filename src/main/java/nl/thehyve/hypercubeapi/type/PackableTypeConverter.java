package nl.thehyve.hypercubeapi.type;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PackableTypeConverter implements AttributeConverter<Packable, String> {

    @Override
    public String convertToDatabaseColumn(Packable packable) {
        return packable.getName();
    }

    @Override
    public Packable convertToEntityAttribute(String dbData) {
        return Packable.forName(dbData);
    }

}
