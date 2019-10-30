package nl.thehyve.hypercubeapi.observation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.transmartproject.common.dto.Field;
import org.transmartproject.common.type.DimensionType;
import org.transmartproject.common.type.ValueType;

import java.util.List;

@Data @Builder @EqualsAndHashCode(of={"name"})
class DimensionProperties {

    String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String modifierCode;

    DimensionType dimensionType;

    Integer sortIndex;

    ValueType valueType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<Field> fields;

    Boolean inline;

}
