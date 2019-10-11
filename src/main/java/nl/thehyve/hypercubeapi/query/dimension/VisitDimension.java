package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.visit.VisitEntity;

import java.util.Arrays;
import java.util.List;

@Data
public class VisitDimension extends I2b2NullablePKDimension<Long> {
    Class elemType = VisitEntity.class;
    List elemFields = Arrays.asList(
        "id", "patientId", "activeStatusCd",
        "startDate", "endDate", "inoutCd", "locationCd", "lengthOfStay", "encounterIds");
    String name = "visit";
    String alias = "visit";
    String columnName = "encounterNum";
    String keyProperty = "id";
    Long nullValue = -1L;
    ImplementationType implementationType = ImplementationType.VISIT;

}
