package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.concept.ConceptEntity;
import nl.thehyve.hypercubeapi.concept.ConceptRepository;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import org.hibernate.criterion.Projections;

import java.util.Arrays;
import java.util.List;

@Data
public class ConceptDimension extends I2b2NullablePKDimension<String, ConceptEntity> {

    public static final String ALIAS = "concept";

    Class<ConceptEntity> elementType = ConceptEntity.class;
    List<String> elemFields = Arrays.asList("conceptPath", "id", "name");
    String name = "concept";
    String alias = ALIAS;
    String columnName = "concept";
    String nullValue = "@";
    ImplementationType implementationType = ImplementationType.COLUMN;

    private final ConceptRepository conceptRepository;

    ConceptDimension(ConceptRepository conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    @Override
    public String getKey(ConceptEntity element) {
        return element.getId();
    }

    @Override
    public List<ConceptEntity> resolveElements(List<String> conceptCodes) {
        return this.conceptRepository.findAllById(conceptCodes);
    }

    @Override
    public void selectIDs(HypercubeQuery query) {
        query.getProjections().add(Projections.property(columnName), getAlias());
    }

}
