package nl.thehyve.hypercubeapi.type;

import javax.persistence.*;

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
