package nl.thehyve.hypercubeapi.tree;

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

@RunWith(SpringRunner.class)
@SpringBootTest
public class TreeEndpointTests {

    @Autowired
    private TreeNodeRepository treeNodeRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private List<TreeNodeEntity> createTestData() {
        return Arrays.asList(
            TreeNodeEntity.builder()
                .fullName("\\Demographics")
                .name("Demographics")
                .level(0)
                .secureObjectToken("PUBLIC")
                .build()
        );
    }

    @Before
    public void setup() {
        this.treeNodeRepository.deleteAll();
        List<TreeNodeEntity> testData = createTestData();
        this.treeNodeRepository.saveAll(testData);
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
    }

    @Test
    public void testGet() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(
            "/v2/tree_nodes?root=\\"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.tree_nodes").value(hasSize(1)));
    }

}
