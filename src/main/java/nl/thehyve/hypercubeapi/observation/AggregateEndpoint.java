package nl.thehyve.hypercubeapi.observation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.transmartproject.common.dto.*;
import org.transmartproject.common.resource.AggregateResource;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Validated
@CrossOrigin
public class AggregateEndpoint implements AggregateResource {

    private final AggregateService aggregateService;

    @Autowired
    AggregateEndpoint(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @Override
    public ResponseEntity<Counts> counts(@Valid ConstraintParameter constraint) {
        return ResponseEntity.ok(this.aggregateService.counts(constraint.getConstraint()));
    }

    @Override
    public ResponseEntity<Map<String, Counts>> countsPerConcept(@Valid ConstraintParameter constraint) {
        return ResponseEntity.ok(this.aggregateService.countsPerConcept(constraint.getConstraint()));
    }

    @Override
    public ResponseEntity<Map<String, Counts>> countsPerStudy(@Valid ConstraintParameter constraint) {
        return ResponseEntity.ok(this.aggregateService.countsPerStudy(constraint.getConstraint()));
    }

    @Override
    public ResponseEntity<Map<String, Map<String, Counts>>> countsPerStudyAndConcept(@Valid ConstraintParameter constraint) {
        return ResponseEntity.ok(this.aggregateService.countsPerStudyAndConcept(constraint.getConstraint()));
    }

    @Override
    public ResponseEntity<Map<String, NumericalValueAggregates>> numericalValueAggregatesPerConcept(@Valid ConstraintParameter constraint) {
        return ResponseEntity.ok(this.aggregateService.numericalValueAggregatesPerConcept(constraint.getConstraint()));
    }

    @Override
    public ResponseEntity<Map<String, CategoricalValueAggregates>> categoricalValueAggregatesPerConcept(@Valid ConstraintParameter constraint) {
        return ResponseEntity.ok(this.aggregateService.categoricalValueAggregatesPerConcept(constraint.getConstraint()));
    }

}
