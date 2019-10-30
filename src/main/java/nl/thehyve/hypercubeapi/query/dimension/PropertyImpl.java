package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;

@Data
public class PropertyImpl<Type> implements Property {
    private final String name;
    private final String propertyName;
    private final Class<Type> type;

    public Type get(Object element) {
        try {
            Object value = element.getClass().getField(propertyName).get(element);
            if (value == null) {
                return null;
            }
            if (!type.isAssignableFrom(value.getClass())) {
                throw new RuntimeException("Value is not of type " + type.getSimpleName());
            }
            return (Type)value;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot access field " + propertyName, e);
        }
    }
}
