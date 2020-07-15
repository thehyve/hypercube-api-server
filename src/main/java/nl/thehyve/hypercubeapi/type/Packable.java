package nl.thehyve.hypercubeapi.type;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

public enum Packable {

    Packable(true),
    Not_Packable(false);

    private boolean value;

    Packable(boolean value) {
        this.value = value;
    }

    public boolean packable() {
        return value;
    }

    @JsonValue
    String getName() {
        return this.name().toUpperCase();
    }

    private static final Map<String, Packable> mapping = new HashMap<>();
    static {
        for (Packable type: values()) {
            mapping.put(type.name().toUpperCase(), type);
        }
    }

    @JsonCreator
    public static Packable forName(String name) {
        if (name == null) {
            return Not_Packable;
        }
        name = name.toUpperCase();
        return mapping.getOrDefault(name, Not_Packable);
    }

}
