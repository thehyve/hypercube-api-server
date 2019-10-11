package nl.thehyve.hypercubeapi.study;

import nl.thehyve.hypercubeapi.test.TestService;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StudyEndpointTests {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private TestService testService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private Long testStudyId;

    private void createTestData() {
        this.testService.cleanAll();
        List<StudyEntity> studies = Arrays.asList(
            StudyEntity.builder().studyId("STUDY_1").secureObjectToken("PUBLIC").build(),
            StudyEntity.builder().studyId("STUDY_2").secureObjectToken("PUBLIC").build()
        );
        this.studyRepository.saveAll(studies);
        this.testStudyId = studies.get(0).getId();
    }

    @Before
    public void setup() {
        this.createTestData();
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
    }

    @Test
    public void testGet() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(
            "/v2/studies"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.studies").value(hasSize(2)));
        mockMvc.perform(MockMvcRequestBuilders.get(
            String.format("/v2/studies/%d", testStudyId)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.studyId").value("STUDY_1"));
    }

}
