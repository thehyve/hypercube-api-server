package nl.thehyve.hypercubeapi.type;

import java.math.BigDecimal;
import java.util.Date;

public class ObservationValueMapper {

    public static Object observationFactValue(ValueType valueType,
                                              String textValue,
                                              BigDecimal numberValue,
                                              String rawValue) {
        switch(valueType) {
            case Text:
                return textValue;
            case Number:
                return numberValue;
            case Date:
                return new Date(numberValue.longValue());
            case RawText:
                return rawValue;
            default:
                throw new RuntimeException("Unsupported database value: ObservationFact.valueType " +
                    "must be one of ${ALL_TYPES.join(', ')}. Found '${valueType}'.");
        }
    }

}
