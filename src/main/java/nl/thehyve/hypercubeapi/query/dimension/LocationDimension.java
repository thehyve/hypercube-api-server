package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import lombok.Getter;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;

import java.util.List;

@Data
public class LocationDimension extends I2b2Dimension<String, String> {
    Class elementType = String.class;
    String name = "location";
    String alias = "location";
    String columnName = "locationCd";
    ImplementationType implementationType = ImplementationType.COLUMN;
    @Getter
    boolean elementsSerializable = true;

    @Override
    public String getKey(String element) {
        return element;
    }

    @Override
    public List<String> resolveElements(List<String> keys) {
        return keys;
    }

    @Override
    public void selectIDs(HypercubeQuery query) {

    }
}
