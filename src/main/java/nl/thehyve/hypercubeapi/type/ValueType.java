package nl.thehyve.hypercubeapi.type;

import java.util.HashMap;
import java.util.Map;

public enum ValueType {

    Text("T"),
    Number("N"),
    Date("D"),
    RawText("B"),
    None("");

    private String code;

    ValueType(String code) {
        this.code = code;
    }

    String getCode() {
        return this.code.toUpperCase();
    }

    private static final Map<String, ValueType> mapping = new HashMap<>();
    static {
        for (ValueType type: values()) {
            mapping.put(type.code.toUpperCase(), type);
        }
    }

    public static ValueType forCode(String code) {
        if (code == null) {
            return None;
        }
        code = code.toUpperCase();
        return mapping.getOrDefault(code, None);
    }

}
