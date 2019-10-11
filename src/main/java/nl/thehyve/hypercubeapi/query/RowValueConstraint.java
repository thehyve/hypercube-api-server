package nl.thehyve.hypercubeapi.query;

import lombok.Builder;
import lombok.Data;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.type.DataType;
import org.transmartproject.common.type.Operator;

@Data @Builder
public class RowValueConstraint extends Constraint {
    private DataType valueType;
    private Operator operator;
    private Object value;
}
