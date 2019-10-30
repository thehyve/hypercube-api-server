package nl.thehyve.hypercubeapi.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum Density {

    Dense(true),
    Sparse(false);

    private boolean value;

    Density(boolean value) {
        this.value = value;
    }

    public boolean isDense() {
        return value;
    }

    @JsonValue
    String getName() {
        return this.name().toUpperCase();
    }

    private static final Map<String, Density> mapping = new HashMap<>();
    static {
        for (Density type: values()) {
            mapping.put(type.name().toUpperCase(), type);
        }
    }

    @JsonCreator
    public static Density forName(String name) {
        if (name == null) {
            return Sparse;
        }
        name = name.toUpperCase();
        return mapping.getOrDefault(name, Sparse);
    }

}
