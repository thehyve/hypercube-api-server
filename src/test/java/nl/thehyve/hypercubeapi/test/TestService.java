package nl.thehyve.hypercubeapi.test;

import nl.thehyve.hypercubeapi.observation.ObservationRepository;
import nl.thehyve.hypercubeapi.patient.PatientMappingRepository;
import nl.thehyve.hypercubeapi.patient.PatientRepository;
import nl.thehyve.hypercubeapi.study.StudyRepository;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private PatientMappingRepository patientMappingRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private TrialVisitRepository trialVisitRepository;

    @Autowired
    private ObservationRepository observationRepository;

    public void cleanAll() {
        this.observationRepository.deleteAll();
        this.patientMappingRepository.deleteAll();
        this.patientRepository.deleteAll();
        this.trialVisitRepository.deleteAll();
        this.studyRepository.deleteAll();
    }

}
