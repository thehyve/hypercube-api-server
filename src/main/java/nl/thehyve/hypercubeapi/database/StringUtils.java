/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package nl.thehyve.hypercubeapi.database;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LikeExpression;
import org.hibernate.criterion.MatchMode;

public class StringUtils {

    static final String asLikeLiteral(String s) {
        return s.replaceAll("[\\%_]", "\\\\$0");
    }

    public static final Criterion like(String propertyName, String value, MatchMode mode) {
        return new LikeExpression(propertyName, asLikeLiteral(value), mode, '\\', false) {};
    }

    public static final Criterion like(String propertyName, String value) {
        return like(propertyName, value, MatchMode.EXACT);
    }

    public static final Criterion startsWith(String propertyName, String value) {
        return like(propertyName, value, MatchMode.START);
    }

    public static final Criterion contains(String propertyName, String value) {
        return like(propertyName, value, MatchMode.ANYWHERE);
    }

}
