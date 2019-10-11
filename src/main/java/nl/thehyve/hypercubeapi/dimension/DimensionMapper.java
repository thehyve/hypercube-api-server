package nl.thehyve.hypercubeapi.dimension;

import nl.thehyve.hypercubeapi.type.ValueTypeMapper;
import org.mapstruct.Mapper;
import org.transmartproject.common.dto.Dimension;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ValueTypeMapper.class})
public abstract class DimensionMapper {

    abstract Dimension dimensionEntityToDimensionDto(DimensionEntity dimensionEntity);

    public String dimensionEntityToDimensionName(DimensionEntity dimensionEntity) {
        return dimensionEntity.getName();
    }

    abstract List<String> dimensionEntityListToDimensionNameList(List<DimensionEntity> dimensionEntities);

}
