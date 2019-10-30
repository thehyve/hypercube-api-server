package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import lombok.Getter;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import org.hibernate.criterion.Projections;

import java.util.Date;
import java.util.List;

@Data
public class EndTimeDimension extends I2b2Dimension<Date, Date> {
    Class elementType = Date.class;
    List<String> elemFields = null;
    String name = "end time";
    String alias = "endDate";
    String columnName = "endDate";
    ImplementationType implementationType = ImplementationType.COLUMN;
    @Getter
    boolean elementsSerializable = true;

    @Override
    public Date getKey(Date element) {
        return element;
    }

    @Override
    public List<Date> resolveElements(List<Date> keys) {
        return keys;
    }

    @Override
    public void selectIDs(HypercubeQuery query) {
        query.getProjections().add(Projections.property("endDate"), "endDate");
    }

}
