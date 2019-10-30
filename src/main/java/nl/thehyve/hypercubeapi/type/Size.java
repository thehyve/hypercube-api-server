package nl.thehyve.hypercubeapi.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum Size {

    Small,
    Medium,
    Large;

    @JsonValue
    String getName() {
        return this.name().toUpperCase();
    }

    private static final Map<String, Size> mapping = new HashMap<>();
    static {
        for (Size type: values()) {
            mapping.put(type.name().toUpperCase(), type);
        }
    }

    @JsonCreator
    public static Size forName(String name) {
        if (name == null) {
            return Small;
        }
        name = name.toUpperCase();
        return mapping.getOrDefault(name, Small);
    }

}
