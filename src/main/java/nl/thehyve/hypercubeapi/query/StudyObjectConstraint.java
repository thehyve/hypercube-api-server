package nl.thehyve.hypercubeapi.query;

import lombok.Builder;
import lombok.Data;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.dto.Study;

import javax.validation.constraints.NotNull;

@Data @Builder
public class StudyObjectConstraint extends Constraint {

    @NotNull
    Study study;

}
