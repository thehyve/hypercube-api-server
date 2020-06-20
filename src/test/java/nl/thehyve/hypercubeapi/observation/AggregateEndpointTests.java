package nl.thehyve.hypercubeapi.observation;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.hypercubeapi.patient.PatientEntity;
import nl.thehyve.hypercubeapi.patient.PatientMappingEntity;
import nl.thehyve.hypercubeapi.patient.PatientMappingRepository;
import nl.thehyve.hypercubeapi.patient.PatientRepository;
import nl.thehyve.hypercubeapi.study.StudyEntity;
import nl.thehyve.hypercubeapi.study.StudyRepository;
import nl.thehyve.hypercubeapi.test.TestService;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitEntity;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitRepository;
import nl.thehyve.hypercubeapi.type.ValueType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.transmartproject.common.constraint.TrueConstraint;
import org.transmartproject.common.dto.CategoricalValueAggregates;
import org.transmartproject.common.dto.ConstraintParameter;
import org.transmartproject.common.dto.Counts;
import org.transmartproject.common.dto.NumericalValueAggregates;
import org.transmartproject.common.type.Sex;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AggregateEndpointTests {

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

    @Autowired
    private TestService testService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private void prepareTestData() {
        this.testService.cleanAll();

        PatientEntity p1 = PatientEntity.builder().sex(Sex.Male).build();
        PatientEntity p2 = PatientEntity.builder().sex(Sex.Female).build();
        this.patientRepository.saveAll(Arrays.asList(p1, p2));
        PatientMappingEntity id1 = PatientMappingEntity.builder().patient(p1).encryptedIdSource("SUBJ_ID").encryptedId("P1").build();
        PatientMappingEntity id2 = PatientMappingEntity.builder().patient(p2).encryptedIdSource("SUBJ_ID").encryptedId("P2").build();
        this.patientMappingRepository.saveAll(Arrays.asList(id1, id2));

        StudyEntity s1 = StudyEntity.builder().studyId("Study 1").secureObjectToken("PUBLIC").build();
        StudyEntity s2 = StudyEntity.builder().studyId("Study 2").secureObjectToken("PUBLIC").build();
        this.studyRepository.saveAll(Arrays.asList(s1, s2));
        TrialVisitEntity tv1 = TrialVisitEntity.builder().study(s1).relativeTimeLabel("Baseline").build();
        TrialVisitEntity tv2 = TrialVisitEntity.builder().study(s2).relativeTimeLabel("Dummy").build();
        this.trialVisitRepository.saveAll(Arrays.asList(tv1, tv2));

        List<ObservationEntity> observations = Arrays.asList(
            ObservationEntity.builder()
                .patient(p1)
                .encounterId(-1L)
                .valueType(ValueType.Number)
                .numericalValue(new BigDecimal(2F))
                .concept("c1")
                .instance(1)
                .trialVisit(tv1)
                .build(),
            ObservationEntity.builder()
                .patient(p2)
                .encounterId(-1L)
                .valueType(ValueType.Number)
                .numericalValue(new BigDecimal(3F))
                .concept("c1")
                .instance(1)
                .trialVisit(tv2)
                .build(),
            ObservationEntity.builder()
                .patient(p2)
                .encounterId(-1L)
                .valueType(ValueType.Text)
                .textValue("A")
                .concept("c2")
                .instance(1)
                .trialVisit(tv2)
                .build()
        );
        this.observationRepository.saveAll(observations);
        this.observationRepository.flush();
    }

    @Before
    public void setup() {
        this.prepareTestData();
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
    }

    @Test
    public void testCounts() throws Exception {
        Counts expected = Counts.builder().patientCount(2).observationCount(3).build();
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/v2/observations/counts")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsBytes(ConstraintParameter.builder().constraint(
                    TrueConstraint.builder().build()
                ).build())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
    }

    @Test
    public void testCountsPerConcept() throws Exception {
        Map<String, Counts> expected = Map.of(
            "c1", Counts.builder().patientCount(2).observationCount(2).build(),
            "c2", Counts.builder().patientCount(1).observationCount(1).build()
        );
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/v2/observations/counts_per_concept")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsBytes(ConstraintParameter.builder().constraint(
                    TrueConstraint.builder().build()
                ).build())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
    }

    @Test
    public void testCountsPerStudy() throws Exception {
        Map<String, Counts> expected = Map.of(
            "Study 1", Counts.builder().patientCount(1).observationCount(1).build(),
            "Study 2", Counts.builder().patientCount(1).observationCount(2).build()
        );
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/v2/observations/counts_per_study")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsBytes(ConstraintParameter.builder().constraint(
                    TrueConstraint.builder().build()
                ).build())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
    }

    @Test
    public void testCountsPerStudyAndConcept() throws Exception {
        Map<String, Map<String, Counts>> expected = Map.of(
            "Study 1", Map.of(
                "c1", Counts.builder().patientCount(1).observationCount(1).build()
            ),
            "Study 2", Map.of(
                "c1", Counts.builder().patientCount(1).observationCount(1).build(),
                "c2", Counts.builder().patientCount(1).observationCount(1).build()
            )
        );
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/v2/observations/counts_per_study_and_concept")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsBytes(ConstraintParameter.builder().constraint(
                    TrueConstraint.builder().build()
                ).build())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
    }

    @Test
    public void testAggregatesPerNumericalConcept() throws Exception {
        Map<String, NumericalValueAggregates> expected = Map.of(
            "c1", NumericalValueAggregates.builder()
                .min(2D)
                .max(3D)
                .avg(2.5D)
                .stdDev(0.7071067811865476D)
                .count(2)
                .build()
        );
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/v2/observations/aggregates_per_numerical_concept")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsBytes(ConstraintParameter.builder().constraint(
                    TrueConstraint.builder().build()
                ).build())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
    }

    @Test
    public void testAggregatesPerCategoricalConcept() throws Exception {
        Map<String, CategoricalValueAggregates> expected = Map.of(
            "c2", CategoricalValueAggregates.builder().nullValueCounts(null).valueCounts(
                Map.of("A", 1)
            ).build()
        );
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/v2/observations/aggregates_per_categorical_concept")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsBytes(ConstraintParameter.builder().constraint(
                    TrueConstraint.builder().build()
                ).build())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
    }

}
