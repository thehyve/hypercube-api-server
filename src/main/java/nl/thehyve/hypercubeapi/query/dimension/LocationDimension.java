package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;

@Data
public class LocationDimension extends I2b2Dimension {
    Class elemType = String.class;
    String name = "location";
    String alias = "location";
    String columnName = "locationCd";
    ImplementationType implementationType = ImplementationType.COLUMN;
}
