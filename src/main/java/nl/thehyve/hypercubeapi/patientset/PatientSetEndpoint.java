package nl.thehyve.hypercubeapi.patientset;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.dto.*;
import org.transmartproject.common.resource.PatientSetResource;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@Validated
@CrossOrigin
public class PatientSetEndpoint implements PatientSetResource {

    @Override
    public ResponseEntity<PatientSetList> listPatientSets() {
        return null;
    }

    @Override
    public ResponseEntity<PatientSetResult> getPatientSet(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<PatientSetResult> createPatientSet(@NotBlank String name, Boolean reuse, @Valid Constraint constraint) {
        return null;
    }

}
