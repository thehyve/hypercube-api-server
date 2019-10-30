package nl.thehyve.hypercubeapi.query.dimension;

import com.google.common.collect.ImmutableList;
import nl.thehyve.hypercubeapi.concept.ConceptRepository;
import nl.thehyve.hypercubeapi.dimension.DimensionEntity;
import nl.thehyve.hypercubeapi.dimension.DimensionRepository;
import nl.thehyve.hypercubeapi.exception.QueryBuilderException;
import nl.thehyve.hypercubeapi.patient.PatientRepository;
import nl.thehyve.hypercubeapi.study.StudyRepository;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitRepository;
import nl.thehyve.hypercubeapi.visit.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DimensionRegistry {

    private static final Logger log = LoggerFactory.getLogger(DimensionRegistry.class);

    // NB: This map only contains the builtin dimensions!
    private final Map<String, Dimension> builtinDimensions;

    /**
     * Primary key columns, except modifierCd and instanceNum.
     */
    private final List<Dimension> primaryKeyDimensions;

    private final DimensionRepository dimensionRepository;

    @Autowired
    DimensionRegistry(DimensionRepository dimensionRepository,
                      StudyRepository studyRepository,
                      ConceptRepository conceptRepository,
                      PatientRepository patientRepository,
                      VisitRepository visitRepository,
                      TrialVisitRepository trialVisitRepository
                      ) {
        this.dimensionRepository = dimensionRepository;

        List<Dimension> dimensions = Arrays.asList(
            new StudyDimension(studyRepository),
            new ConceptDimension(conceptRepository),
            new PatientDimension(patientRepository),
            new VisitDimension(visitRepository),
            new StartTimeDimension(),
            new EndTimeDimension(),
            new LocationDimension(),
            new TrialVisitDimension(trialVisitRepository),
            new ProviderDimension(),
            // VALUE is a fake dimension that is used in e.g. the query builder. It does not have a corresponding DimensionDescription
            new ValueDimension()
        );

        this.builtinDimensions = dimensions.stream().collect(Collectors.toUnmodifiableMap(
            Dimension::getName, dimension -> dimension));

        this.primaryKeyDimensions = ImmutableList.copyOf(
            Arrays.stream(new String[]{"concept", "provider", "patient", "visit", "start time"})
                .map(this.builtinDimensions::get)
                .collect(Collectors.toList()));

        for (Dimension dimension: builtinDimensions.values()) {
            ((DimensionImpl)dimension).verify();
        }
    }

    Dimension getBuiltinDimension(String name) { return builtinDimensions.get(name); }

    Dimension getDimension(DimensionEntity dimensionEntity) {
        if (dimensionEntity.getModifierCode() == null) {
            return getBuiltinDimension(dimensionEntity.getName());
        }
        return ModifierDimension.get(dimensionEntity.getName(),
            dimensionEntity.getModifierCode(), dimensionEntity.getValueType(),
            dimensionEntity.getDimensionType(), dimensionEntity.getDensity(), dimensionEntity.getSortIndex());
    }

    Dimension fromName(String name) {
        if (name.equals("value")) {
            return getBuiltinDimension("value");
        }
        Optional<DimensionEntity> entity = this.dimensionRepository.findByName(name);
        if (entity.isEmpty()) {
            throw new RuntimeException("Cannot find dimension: " + name);
        }
        return getDimension(entity.get());
    }

    public List<Dimension> getAllDimensions() {
        List<Dimension> dimensions = new ArrayList<>(builtinDimensions.values());
        Set<String> dimensionNames = dimensions.stream().map(Dimension::getName).collect(Collectors.toSet());
        for (DimensionEntity dimension: this.dimensionRepository.findAll()) {
            if (!dimensionNames.contains(dimension.getName())) {
                dimensions.add(getDimension(dimension));
                dimensionNames.add(dimension.getName());
            }
        }
        return dimensions;
    }

    public List<Dimension> getPrimaryKeyDimensions() {
        return this.primaryKeyDimensions;
    }

    public Set<String> getSortableDimensionNames() {
        return getAllDimensions().stream()
            .filter(dimension -> dimension instanceof AliasAwareDimension)
            .map(Dimension::getName)
            .collect(Collectors.toSet());
    }

    public DimensionMetadata forDimensionName(String dimensionName) {
        Dimension dim = getBuiltinDimension(dimensionName);
        if (dim == null) {
            dim = fromName(dimensionName);
        }
        if (dim == null) {
            throw new QueryBuilderException("Dimension not found: " + dimensionName);
        }
        return new DimensionMetadata(dim);
    }

}
