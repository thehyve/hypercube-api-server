package nl.thehyve.hypercubeapi.tree;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class TreeEntityTests {

    @Test
    public void testTreeNodeEntityEquals() {
        EqualsVerifier
            .forClass(TreeNodeEntity.class)
            .withPrefabValues(TreeNodeEntity.class,
                TreeNodeEntity.builder()
                    .name("Test")
                    .fullName("\\Test\\")
                    .secureObjectToken("PUBLIC")
                    .build(),
                TreeNodeEntity.builder()
                    .name("Dummy")
                    .fullName("\\Dummy\\")
                    .secureObjectToken("PUBLIC")
                    .build())
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.SURROGATE_KEY)
            .verify();
    }

    @Test
    public void testTreeNodeTagEntityEquals() {
        EqualsVerifier
            .forClass(TreeNodeTagEntity.class)
            .withPrefabValues(TreeNodeEntity.class,
                TreeNodeEntity.builder()
                    .name("Test")
                    .fullName("\\Test\\")
                    .secureObjectToken("PUBLIC")
                    .build(),
                TreeNodeEntity.builder()
                    .name("Dummy")
                    .fullName("\\Dummy\\")
                    .secureObjectToken("PUBLIC")
                    .build())
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.SURROGATE_KEY)
            .verify();
    }

}
