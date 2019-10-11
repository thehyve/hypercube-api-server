package nl.thehyve.hypercubeapi.query;

import lombok.Builder;
import lombok.Data;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.type.Operator;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data @Builder
public class MultipleSubSelectionsConstraint extends Constraint {

    @NotBlank
    private String dimension;

    /**
     * {@link Operator#Intersect} or {@link Operator#Union}.
     */
    @NotNull
    private Operator operator;

    @Valid
    List<Constraint> args;

}
