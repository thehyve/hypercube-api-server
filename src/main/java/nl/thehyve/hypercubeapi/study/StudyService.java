package nl.thehyve.hypercubeapi.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.transmartproject.common.dto.*;
import org.transmartproject.common.exception.ResourceNotFound;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMapper studyMapper;

    @Autowired
    StudyService(StudyRepository studyRepository, StudyMapper studyMapper) {
        this.studyRepository = studyRepository;
        this.studyMapper = studyMapper;
    }

    public StudyList getStudies() {
        return StudyList.builder()
            .studies(this.studyRepository.findAll().stream()
                .map(this.studyMapper::studyToStudyDto)
                .collect(Collectors.toList())
            )
            .build();
    }

    public Study getStudy(Long id) {
        Optional<StudyEntity> studyObject = this.studyRepository.findById(id);
        if (studyObject.isPresent()) {
            return this.studyMapper.studyToStudyDto(studyObject.get());
        }
        throw new ResourceNotFound("Could not find study with id " + id.toString());
    }

}
