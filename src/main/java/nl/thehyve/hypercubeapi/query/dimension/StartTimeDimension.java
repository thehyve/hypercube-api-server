package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.exception.QueryBuilderException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Data
public class StartTimeDimension extends I2b2NullablePKDimension<Date> {

    public static final Date EMPTY_DATE;
    static {
        try {
            EMPTY_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse("0001-01-01 00:00:00");
        } catch (ParseException e) {
            throw new QueryBuilderException("Could not initialise empty date");
        }
    }

    Class elemType = Date.class;
    String name = "start time";

    String alias = "startDate";
    String columnName = "startDate";
    Date nullValue = EMPTY_DATE;
    ImplementationType implementationType = ImplementationType.COLUMN;

}
