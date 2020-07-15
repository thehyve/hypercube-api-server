package nl.thehyve.hypercubeapi.query;

import lombok.*;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.type.*;

@Data @Builder
public class RowValueConstraint extends Constraint {
    private DataType valueType;
    private Operator operator;
    private Object value;
}
