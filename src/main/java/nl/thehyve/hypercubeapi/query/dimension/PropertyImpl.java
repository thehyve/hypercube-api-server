package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;

import java.util.Map;

@Data
class PropertyImpl<T> {
    final String name;
    final String propertyName;
    final Class type;
    T get(Object element) {
        return element instanceof Map ? (T)((Map) element).get(propertyName) : null;
    }
}
