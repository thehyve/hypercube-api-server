package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import lombok.Getter;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import org.hibernate.criterion.Projections;

import java.util.List;

@Data
public class ProviderDimension extends I2b2NullablePKDimension<String, String> {

    public static final String ALIAS = "provider";

    Class elementType = String.class;
    String name = "provider";
    String alias = ALIAS;
    String columnName = "providerId";
    String nullValue = "@";
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
        query.getProjections().add(Projections.property(getColumnName()), getAlias());
    }

}
