package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;

@Data
public class ProviderDimension extends I2b2NullablePKDimension<String> {
    Class elemType = String.class;
    String name = "provider";
    String alias = "provider";
    String columnName = "providerId";
    String nullValue = "@";
    ImplementationType implementationType = ImplementationType.COLUMN;
}
