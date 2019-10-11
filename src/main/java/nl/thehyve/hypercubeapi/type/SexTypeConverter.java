package nl.thehyve.hypercubeapi.type;

import org.transmartproject.common.type.Sex;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class SexTypeConverter implements AttributeConverter<Sex, String> {

    @Override
    public String convertToDatabaseColumn(Sex sex) {
        return sex.toString();
    }

    @Override
    public Sex convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Sex.Unknown;
        }
        switch(dbData.toLowerCase()) {
            case "m":
            case "male":
                return Sex.Male;
            case "f":
            case "female":
                return Sex.Female;
            default:
                return Sex.Unknown;
        }
    }

}
