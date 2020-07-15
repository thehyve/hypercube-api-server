/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package nl.thehyve.hypercubeapi.query.dimension;

import lombok.Data;
import nl.thehyve.hypercubeapi.query.dimension.Dimension.ImplementationType;
import nl.thehyve.hypercubeapi.visit.VisitEntity;
import nl.thehyve.hypercubeapi.exception.QueryBuilderException;
import nl.thehyve.hypercubeapi.observation.ObservationEntity;
import nl.thehyve.hypercubeapi.study.StudyEntity;
import org.slf4j.*;
import org.transmartproject.common.constraint.Field;
import org.transmartproject.common.type.DataType;

import java.util.*;
import java.util.stream.*;

import static nl.thehyve.hypercubeapi.query.dimension.Dimension.ImplementationType.*;

/**
 * Contains database mapping metadata for the dimensions.
 */
@Data
public class DimensionMetadata {

    private static final Logger log = LoggerFactory.getLogger(DimensionMetadata.class);

    Dimension dimension;
    Class domainClass;

    public ImplementationType getType() {
        return dimension.getImplementationType();
    }

    public String getFieldName() {
        if (!(dimension instanceof I2b2Dimension)){
            return null;
        }
        String colName = ((I2b2Dimension) dimension).getColumnName();
        if (colName.endsWith(".id")) {
            colName = colName.substring(0, colName.length() - 3);
        }
        return colName;
    }

    List<Field> fields;
    Map<String, Class> fieldTypes = new LinkedHashMap<>();

    public final Field getMappedField(String fieldName) {
        Optional<java.lang.reflect.Field> fieldOptional = Arrays.stream(domainClass.getDeclaredFields())
            .filter((java.lang.reflect.Field field) -> !field.isSynthetic() && field.getName().equals(fieldName))
            .findFirst();
        if (fieldOptional.isEmpty()) {
            throw new QueryBuilderException(String.format(
                "No field '%s' found in %s", fieldName, domainClass.getSimpleName()));
        }
        java.lang.reflect.Field field = fieldOptional.get();
        fieldTypes.put(fieldName, field.getType());
        DataType type = DataType.Object;
        if (fieldName.equals("id")) {
            type = DataType.Id;
        } else if (Number.class.isAssignableFrom(field.getType())) {
            type = DataType.Numeric;
        } else if (Date.class.isAssignableFrom(field.getType())) {
            type = DataType.Date;
        } else if (String.class.isAssignableFrom(field.getType())) {
            type = DataType.String;
        }
        return Field.builder().dimension(this.dimension.getName()).type(type).fieldName(field.getName()).build();
    }

    DimensionMetadata(Dimension dim) {
        this.dimension = dim;

        log.info(String.format("Registering dimension %s ...", dim.getName()));

        if (getType() == TABLE) {
            Optional<java.lang.reflect.Field> fieldOptional = Stream.of(ObservationEntity.class.getDeclaredFields()).filter(
                (java.lang.reflect.Field field) -> !field.isSynthetic() && field.getName().equals(getFieldName())).findFirst();
            if (fieldOptional.isEmpty()) {
                throw new QueryBuilderException(String.format(
                    "No field with name %s found in %s",
                    getFieldName(), ObservationEntity.class.getSimpleName()));
            } else {
                this.domainClass = fieldOptional.get().getType();
                this.fields = Stream.of(this.domainClass.getDeclaredFields()).
                    filter((java.lang.reflect.Field field) -> !field.isSynthetic())
                    .map((java.lang.reflect.Field field) -> getMappedField(field.getName()))
                    .collect(Collectors.toList());
            }
        } else if (getType() == STUDY) {
            this.domainClass = StudyEntity.class;
            this.fields = Stream.of(this.domainClass.getDeclaredFields()).
                filter((java.lang.reflect.Field field) -> !field.isSynthetic())
                .map((java.lang.reflect.Field field) -> getMappedField(field.getName()))
                .collect(Collectors.toList());
        } else if (getType() == VISIT) {
            this.domainClass = VisitEntity.class;
            this.fields = Stream.of(this.domainClass.getDeclaredFields()).
                filter((java.lang.reflect.Field field) -> !field.isSynthetic())
                .map((java.lang.reflect.Field field) -> getMappedField(field.getName()))
                .collect(Collectors.toList());
        } else {
            this.domainClass = ObservationEntity.class;
            this.fields = Stream.of(this.domainClass.getDeclaredFields()).
                filter((java.lang.reflect.Field field) -> !field.isSynthetic())
                .map((java.lang.reflect.Field field) -> getMappedField(field.getName()))
                .collect(Collectors.toList());
            switch (getType()) {
                case MODIFIER:
                    this.fields.add(getMappedField("modifierCode"));
                    //fallthrough
                case VALUE:
                    this.fields.add(getMappedField("valueType"));
                    this.fields.add(getMappedField("textValue"));
                    this.fields.add(getMappedField("numericalValue"));
                    this.fields.add(getMappedField("rawTextValue"));
                    break;
                case COLUMN:
                    this.fields.add(getMappedField(getFieldName()));
                    break;
                default:
                    throw new QueryBuilderException(
                        String.format("Unexpected fetch type for dimension '%s'", dim.getName()));
            }
        }
        log.info(String.format("Done for dimension '%s'.", dim.getName()));
    }

}
