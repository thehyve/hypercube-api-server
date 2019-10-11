package nl.thehyve.hypercubeapi.query.dimension;

import nl.thehyve.hypercubeapi.dimension.DimensionEntity;
import nl.thehyve.hypercubeapi.dimension.DimensionRepository;
import nl.thehyve.hypercubeapi.exception.QueryBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class DimensionRegistry {

    private static final Logger log = LoggerFactory.getLogger(DimensionRegistry.class);

    public static final StudyDimension STUDY =            new StudyDimension();
    public static final ConceptDimension CONCEPT =        new ConceptDimension();
    public static final PatientDimension PATIENT =        new PatientDimension();
    public static final VisitDimension VISIT =            new VisitDimension();
    public static final StartTimeDimension START_TIME =   new StartTimeDimension();
    public static final EndTimeDimension END_TIME =       new EndTimeDimension();
    public static final LocationDimension LOCATION =      new LocationDimension();
    public static final TrialVisitDimension TRIAL_VISIT = new TrialVisitDimension();
    public static final ProviderDimension PROVIDER =      new ProviderDimension();

    // VALUE is a fake dimension that is used in e.g. the query builder. It does not have a corresponding DimensionDescription
    public static final ValueDimension VALUE =            new ValueDimension();

    // NB: This map only contains the builtin dimensions! To get a dimension that is not necessarily builtin
    // use DimensionDescription.findByName(name).dimension
    private static final Map<String, DimensionImpl> builtinDimensions = Map.of(
        STUDY.getName(),       STUDY,
        CONCEPT.getName(),     CONCEPT,
        PATIENT.getName(),     PATIENT,
        VISIT.getName(),       VISIT,
        START_TIME.getName(),  START_TIME,
        END_TIME.getName(),    END_TIME,
        LOCATION.getName(),    LOCATION,
        TRIAL_VISIT.getName(), TRIAL_VISIT,
        PROVIDER.getName(),    PROVIDER
    );

    static {
        for (DimensionImpl dimension: builtinDimensions.values()) {
            dimension.verify();
        }
    }

    private final DimensionRepository dimensionRepository;

    @Autowired
    DimensionRegistry(DimensionRepository dimensionRepository) {
        this.dimensionRepository = dimensionRepository;
    }


    DimensionImpl getBuiltinDimension(String name) { return builtinDimensions.get(name); }

    boolean isBuiltinDimension(String name) { return builtinDimensions.containsKey(name); }

    DimensionImpl getDimension(DimensionEntity dimensionEntity) {
        if (dimensionEntity.getModifierCode() == null) {
            return getBuiltinDimension(dimensionEntity.getName());
        }
        return ModifierDimension.get(dimensionEntity.getName(),
            dimensionEntity.getModifierCode(), dimensionEntity.getValueType(),
            dimensionEntity.getDimensionType(), dimensionEntity.getSortIndex());
    }

    DimensionImpl fromName(String name) {
        if (name.equals("value")) {
            return VALUE;
        }
        Optional<DimensionEntity> entity = this.dimensionRepository.findByName(name);
        if (!entity.isPresent()) {
            throw new RuntimeException("Cannot find dimension: " + name);
        }
        return getDimension(entity.get());
    }

    public DimensionMetadata forDimensionName(String dimensionName) {
        DimensionImpl dim = getBuiltinDimension(dimensionName);
        if (dim == null) {
            dim = fromName(dimensionName);
        }
        if (dim == null) {
            throw new QueryBuilderException("Dimension not found: " + dimensionName);
        }
        return forDimension(dim);
    }

    public static DimensionMetadata forDimension(DimensionImpl dimension) {
        return new DimensionMetadata(dimension);
    }

}
