package nl.thehyve.hypercubeapi.study;

import nl.thehyve.hypercubeapi.dimension.DimensionMapper;
import org.mapstruct.Mapper;
import org.transmartproject.common.dto.Study;

@Mapper(componentModel = "spring", uses = {DimensionMapper.class})
public interface StudyMapper {

    Study studyToStudyDto(StudyEntity study);

}
