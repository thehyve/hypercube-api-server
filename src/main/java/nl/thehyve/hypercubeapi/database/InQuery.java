/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package nl.thehyve.hypercubeapi.database;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * This class aimed to overcome the maximum number of parameters PostgreSQL can handle:
 *   'Tried to send an out-of-range integer as a 2-byte value'.
 *
 * For PostgreSQL, given a query, a property and a list of values, the query is executed for
 * sublists of the list of values and the results are combined.
 */
public class InQuery {

    public static final int POSTGRES_MAX_PARAMETERS = 10000;

    /**
     * Creates a disjunctive property constraint, the equivalent of
     * <code>Restrictions.in(property, values.collect().toArray())<code>.
     *
     * @param property the property to filter by.
     * @param values the list of values to filter on.
     * @return the criterion.
     */
    public static <T> Criterion inValues(String property, List<T> values) {
        if (values.size() == 0) {
            return Restrictions.not(Restrictions.sqlRestriction("1=1"));
        } else {
            return Restrictions.in(property, values.toArray());
        }
    }

}
