package nl.thehyve.hypercubeapi.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.transmartproject.common.dto.Study;
import org.transmartproject.common.dto.StudyList;
import org.transmartproject.common.resource.StudyResource;

@RestController
@Validated
@CrossOrigin
public class StudyEndpoint implements StudyResource {

    private final StudyService studyService;

    @Autowired
    StudyEndpoint(StudyService studyService) {
        this.studyService = studyService;
    }

    @Override
    public ResponseEntity<StudyList> listStudies() {
        return new ResponseEntity<>(this.studyService.getStudies(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Study> getStudy(Long id) {
        return new ResponseEntity<>(this.studyService.getStudy(id), HttpStatus.OK);
    }

}
