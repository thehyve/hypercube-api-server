package nl.thehyve.hypercubeapi.type;

import javax.persistence.*;

@Converter(autoApply = true)
public class DensityTypeConverter implements AttributeConverter<Density, String> {

    @Override
    public String convertToDatabaseColumn(Density density) {
        return density.getName();
    }

    @Override
    public Density convertToEntityAttribute(String dbData) {
        return Density.forName(dbData);
    }

}
