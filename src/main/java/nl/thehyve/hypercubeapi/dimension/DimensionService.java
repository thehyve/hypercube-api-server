package nl.thehyve.hypercubeapi.dimension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.transmartproject.common.dto.Dimension;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DimensionService {

    protected static final Logger log = LoggerFactory.getLogger(DimensionService.class);

    private final DimensionRepository dimensionRepository;
    private final DimensionMapper dimensionMapper;

    @Autowired
    DimensionService(DimensionRepository dimensionRepository, DimensionMapper dimensionMapper) {
        this.dimensionRepository = dimensionRepository;
        this.dimensionMapper = dimensionMapper;
    }

    /**
     * Retrieve all dimensions that are available in this database for all studies
     * @return a list of all dimensions
     */
    public List<Dimension> getAllDimensions() {
        return this.dimensionRepository.findAll().stream()
            .map(this.dimensionMapper::dimensionEntityToDimensionDto)
            .collect(Collectors.toList());
    }

}
