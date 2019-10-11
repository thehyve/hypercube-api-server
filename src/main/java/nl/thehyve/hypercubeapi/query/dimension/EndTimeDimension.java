package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;

import java.util.Date;

@Data
public class EndTimeDimension extends I2b2Dimension {
    Class elemType = Date.class;
    String name = "end time";
    String alias = "endDate";
    String columnName = "endDate";
    ImplementationType implementationType = ImplementationType.COLUMN;
}
