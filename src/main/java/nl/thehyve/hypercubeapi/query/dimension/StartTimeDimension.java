package nl.thehyve.hypercubeapi.query.dimension;

import lombok.*;
import nl.thehyve.hypercubeapi.exception.QueryBuilderException;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;

import java.text.*;
import java.util.*;

@Data
public class StartTimeDimension extends I2b2NullablePKDimension<Date, Date> {

    public static final Date EMPTY_DATE;
    static {
        try {
            EMPTY_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse("0001-01-01 00:00:00");
        } catch (ParseException e) {
            throw new QueryBuilderException("Could not initialise empty date");
        }
    }

    public static final String ALIAS = "startDate";

    Class elementType = Date.class;
    String name = "start time";
    String alias = ALIAS;
    String columnName = "startDate";
    Date nullValue = EMPTY_DATE;
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

    }

}
