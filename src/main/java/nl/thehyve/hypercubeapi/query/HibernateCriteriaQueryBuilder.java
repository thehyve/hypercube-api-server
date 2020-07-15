/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package nl.thehyve.hypercubeapi.query;

import nl.thehyve.hypercubeapi.database.*;
import nl.thehyve.hypercubeapi.dimension.DimensionEntity;
import nl.thehyve.hypercubeapi.concept.ConceptEntity;
import nl.thehyve.hypercubeapi.observation.ModifierEntity;
import nl.thehyve.hypercubeapi.query.dimension.Dimension;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitEntity;
import nl.thehyve.hypercubeapi.type.*;
import nl.thehyve.hypercubeapi.type.ValueType;
import nl.thehyve.hypercubeapi.visit.VisitEntity;
import nl.thehyve.hypercubeapi.exception.QueryBuilderException;
import nl.thehyve.hypercubeapi.observation.ObservationEntity;
import nl.thehyve.hypercubeapi.patient.PatientMappingEntity;
import nl.thehyve.hypercubeapi.patientset.PatientSetCollectionEntity;
import nl.thehyve.hypercubeapi.query.dimension.DimensionRegistry;
import nl.thehyve.hypercubeapi.study.StudyEntity;
import nl.thehyve.hypercubeapi.query.dimension.*;
import nl.thehyve.hypercubeapi.relation.*;
import org.apache.commons.lang.NotImplementedException;
import org.hibernate.criterion.*;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.mapstruct.factory.Mappers;
import org.slf4j.*;
import org.transmartproject.common.constraint.*;
import org.transmartproject.common.dto.Study;
import org.transmartproject.common.type.*;
import org.transmartproject.common.validation.DataTypeValidation;

import java.math.BigDecimal;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.transmartproject.common.type.Operator.*;

/**
 * QueryBuilder that produces a {@link DetachedCriteria} object representing
 * the query.
 * Example:
 * <code>
 *     def builder = HibernateCriteriaQueryBuilder.forStudies(studies)
 *     def query = new ConceptConstraint(conceptCode: "favouritebook")
 *     def criteria = builder.buildCriteria(query)
 *     def results = criteria.getExecutableCriteria(sessionFactory.currentSession).list()
 * </code>
 */
public class HibernateCriteriaQueryBuilder extends ConstraintBuilder<Criterion> {

    private Logger log = LoggerFactory.getLogger(HibernateCriteriaQueryBuilder.class);

    public static final String SUBJECT_ID_SOURCE = "SUBJ_ID";
    public static final Date EMPTY_DATE;
    static {
        try {
            EMPTY_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse("0001-01-01 00:00:00");
        } catch (ParseException e) {
            throw new QueryBuilderException("Could not initialise empty date");
        }
    }
    public static final Criterion defaultModifierCriterion = Restrictions.eq("modifierCode", "@");

    public static HibernateCriteriaQueryBuilder forStudies(Collection<Study> studies, DimensionRegistry dimensionRegistry) {
        return new HibernateCriteriaQueryBuilder(false, studies, dimensionRegistry);
    }

    public static HibernateCriteriaQueryBuilder forAllStudies(DimensionRegistry dimensionRegistry) {
        return new HibernateCriteriaQueryBuilder(true, null, dimensionRegistry);
    }

    private final DimensionMetadata valueMetadata;
    private final Field valueTypeField;
    private final Field numberValueField;
    private final Field textValueField;
    private final Field rawTextValueField;
    private final Field patientIdField = Field.builder().dimension("patient").fieldName("id").type(DataType.Id).build();
    private final Field startTimeField = Field.builder().dimension("start time").fieldName("startDate").type(DataType.Date).build();

    private Map<String, Integer> aliasSuffixes = new LinkedHashMap<>();
    private Map<String, String> aliases = new LinkedHashMap<>();
    private final Collection<Study> studies;
    private final boolean accessToAllStudies;

    private final DimensionRegistry dimensionRegistry;

    private HibernateCriteriaQueryBuilder(boolean accessToAllStudies, Collection<Study> studies, DimensionRegistry dimensionRegistry) {
        this.accessToAllStudies = accessToAllStudies;
        this.studies = studies;
        this.dimensionRegistry = dimensionRegistry;
        this.valueMetadata = dimensionRegistry.forDimensionName("value");
        this.valueTypeField = valueMetadata.getMappedField("valueType");
        this.numberValueField = valueMetadata.getMappedField("numericalValue");
        this.textValueField = valueMetadata.getMappedField("textValue");
        this.rawTextValueField = valueMetadata.getMappedField("rawTextValue");
    }

    private HibernateCriteriaQueryBuilder subQueryBuilder() {
        HibernateCriteriaQueryBuilder subQueryBuilder =
                accessToAllStudies ? forAllStudies(dimensionRegistry) : forStudies(studies, dimensionRegistry);
        subQueryBuilder.aliasSuffixes = aliasSuffixes;
        return subQueryBuilder;
    }

    /**
     * Gets an alias for a property name.
     * Within the query builder, a property always gets the same alias.
     * All aliases that have been requested are added to the produces query
     * criteria as aliases.
     * @param propertyName the name of the property of {@link ObservationEntity}.
     * @return an alias as String.
     */
    private String getAlias(String propertyName) {
        assert !propertyName.contains("__");
        String alias = this.aliases.get(propertyName);
        if (alias != null) {
            return alias;
        }
        int suffix = this.aliasSuffixes.get(propertyName) == null ? 0 : this.aliasSuffixes.get(propertyName);
        this.aliasSuffixes.put(propertyName, suffix + 1);
        alias = String.format("%s_%s", propertyName.replace(".", "__"), suffix);
        this.aliases.put(propertyName, alias);
        return alias;
    }

    /**
     * Compiles the property name for <code>field</code> from the dimension property name and the field name.
     */
    private String getFieldPropertyName(Field field) {
        DimensionMetadata metadata = dimensionRegistry.forDimensionName(field.getDimension());
        log.debug("Field property name: field = ${field}, dimension: ${field.dimension}, metadata: ${metadata} | ${metadata.type}");
        switch (metadata.getType()) {
            case COLUMN:
                return metadata.getFieldName();
            case MODIFIER:
            case VALUE:
                return field.getFieldName();
            default:
                break;
        }
        String dimensionAlias;
        if (metadata.getType() == Dimension.ImplementationType.VISIT) {
            dimensionAlias = "visit";
        } else {
            dimensionAlias = getAlias(metadata.getFieldName());
        }
        if (field.getType() == DataType.Object) {
            return String.format("%s.id", dimensionAlias);
        }
        Class<?> fieldType = metadata.getFieldTypes().get(field.getFieldName());
        if (fieldType == null) {
            throw new QueryBuilderException(String.format(
                "Type not found for field '%s' of class %s",
                field.getFieldName(), metadata.getDomainClass().getSimpleName()
        ));
        }
        Class<?> classForType = Mappers.getMapper(DataTypeMapper.class).dataTypeToClass(field.getType());
        if (field.getType() == DataType.None || !classForType.isAssignableFrom(fieldType)) {
            throw new QueryBuilderException(String.format(
                "Field type '%s' not compatible with type %s of %s.%s",
                field.getType(), fieldType.getSimpleName(), metadata.getDomainClass().getSimpleName(), field.getFieldName()));
        }
        return String.format("%s.%s", dimensionAlias, field.getFieldName());
    }

    /**
     * Creates a {@link org.hibernate.criterion.DetachedCriteria} object for {@link ObservationEntity}.
     * @return
     */
    private DetachedCriteria builder() {
        return DetachedCriteria.forClass(ObservationEntity.class, getAlias("observation_fact"));
    }

    /**
     * Creates an empty criteria object.
     */
    @SuppressWarnings("unused")
    public Criterion build(TrueConstraint constraint) {
        return Restrictions.sqlRestriction("1=1");
    }

    /**
     * Creates a criteria for matching value type and value of a {@link ObservationEntity} row with
     * the type and value in the {@link RowValueConstraint}.
     */
    Criterion build(RowValueConstraint constraint) {
        ValueType valueTypeCode;
        Field valueField;
        switch (constraint.getValueType()) {
            case Numeric:
                valueTypeCode = ValueType.Number;
                valueField = numberValueField;
                break;
            case String:
                valueTypeCode = ValueType.Text;
                valueField = textValueField;
                break;
            case Text:
                valueTypeCode = ValueType.RawText;
                valueField = rawTextValueField;
                break;
            case Date:
                valueTypeCode = ValueType.Date;
                valueField = numberValueField;
                break;
            default:
                throw new QueryBuilderException(String.format(
                    "Value type not supported: %s.", constraint.getValueType()));
        }
        if (!DataTypeValidation.supportsType(constraint.getOperator(), constraint.getValueType())) {
            throw new QueryBuilderException(String.format(
                "Value type %s not supported for operator '%s'.",
                constraint.getValueType(), constraint.getOperator()));
        }
        if (!DataTypeValidation.supportsValue(constraint.getValueType(), constraint.getValue())) {
            throw new QueryBuilderException(String.format(
                "Value of class %s not supported for value type '%s'.",
                constraint.getValue() == null ? null : constraint.getValue().getClass().getSimpleName(),
                constraint.getValueType()));
        }

        Constraint conjunction = AndConstraint.builder().args(Arrays.asList(
                FieldConstraint.builder().field(valueTypeField).operator(Equals).value(valueTypeCode).build(),
                FieldConstraint.builder().field(valueField).operator(constraint.getOperator()).value(constraint.getValue()).build()
        )).build();
        return build(conjunction);
    }

    /**
     * Creates a subquery to find observations with the same primary key
     * and match the modifier constraint and value constraint.
     */
    public Criterion build(ModifierConstraint constraint) {
        String observationFactAlias = getAlias("observation_fact");
        Criterion modifierCriterion = null;
        if (constraint.getModifierCode().equals("@")) {
            // no need for a subquery
        } else if (constraint.getModifierCode() != null) {
            modifierCriterion = Restrictions.eq("modifierCode", constraint.getModifierCode());
        } else if (constraint.getPath() != null) {
            String modifierAlias = "modifier_dimension";
            DetachedCriteria subCriteria = DetachedCriteria.forClass(ModifierEntity.class, modifierAlias);
            subCriteria.add(Restrictions.eq(modifierAlias + ".id", constraint.getPath()));
            modifierCriterion = Subqueries.propertyEq("modifierCode", subCriteria.setProjection(Projections.property("modifierCode")));
        } else if (constraint.getDimensionName() != null) {
            String dimensionAlias = "dimension_description";
            DetachedCriteria subCriteria = DetachedCriteria.forClass(DimensionEntity.class, dimensionAlias);
            subCriteria.add(Restrictions.eq(dimensionAlias + ".name", constraint.getDimensionName()));
            modifierCriterion = Subqueries.propertyEq("modifierCode", subCriteria.setProjection(Projections.property("modifierCode")));
        } else {
            throw new QueryBuilderException("Modifier constraint shouldn't have a null value for all modifier path, code and dimension name");
        }
        Constraint valueConstraint;
        if (constraint.getValues() != null) {
            valueConstraint = RowValueConstraint.builder()
                .valueType(constraint.getValues().getValueType())
                .operator(constraint.getValues().getOperator())
                .value(constraint.getValues().getValue())
                .build();
        } else {
            // match all records with the modifier
            valueConstraint = new TrueConstraint();
        }
        if (modifierCriterion == null) {
            Criterion valueCriterion = build(valueConstraint);
            return Restrictions.and(valueCriterion, defaultModifierCriterion);
        } else {
            HibernateCriteriaQueryBuilder subQueryBuilder = subQueryBuilder();
            DetachedCriteria subQuery = subQueryBuilder.buildCriteria(valueConstraint, modifierCriterion)
                    .add(Restrictions.eqProperty("encounterId", observationFactAlias + ".encounterId"))
                    .add(Restrictions.eqProperty("patient", observationFactAlias + ".patient"))
                    .add(Restrictions.eqProperty("concept", observationFactAlias + ".concept"))
                    .add(Restrictions.eqProperty("providerId", observationFactAlias + ".providerId"))
                    .add(Restrictions.eqProperty("startDate", observationFactAlias + ".startDate"))
                    .add(Restrictions.eqProperty("instance", observationFactAlias + ".instance"));

            subQuery = subQuery.setProjection(Projections.id());
            return Subqueries.exists(subQuery);
        }
    }

    /**
     * Creates a subquery to find observations with the same primary key
     * with observation modifier code "@" and matching the constraint specified by
     * type, operator and value in the {@link ValueConstraint}.
     */
    public Criterion build(ValueConstraint constraint) {
        return build(ModifierConstraint.builder().modifierCode("@").values(constraint).build());
    }

    /**
     * Converts a value to the type of the field, which is assumed to be {@link java.lang.Long} for fields of
     * type <code>OBJECT</code> or <code>ID</code>.
     * Otherwise, the field type as declared in the dimension domain class is used.
     */
    private Object convertValue(Field field, Object value) {
        if (value instanceof Collection) {
            return ((Collection<Object>)value).stream()
                .map((Object val) -> convertValue(field, val))
                .collect(Collectors.toList());
        }
        if (field.getType() == DataType.Object || field.getType() == DataType.Id) {
            return (Long)value;
        }
        Class<?> fieldType = dimensionRegistry.forDimensionName(field.getDimension()).getFieldTypes().get(field.getFieldName());
        if (fieldType != null && !fieldType.isInstance(value)) {
            if (Number.class.isAssignableFrom(fieldType) && value instanceof Date) {
                // TODO Remove in TMT-420
                return toNumber((Date)value);
            } else {
                try {
                    return value == null ? null : fieldType.getDeclaredConstructor().newInstance(value);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new QueryBuilderException("Cannot convert value", e);
                }
            }
        }
        return value;
    }

    private static Number toNumber(Date value) {
        return new BigDecimal(value.getTime());
    }

    /**
     * Creates a {@link org.hibernate.criterion.Criterion} for the Boolean constraint that operates
     * on a property and a value.
     *
     * Supports the operators:
     * - {@link Operator#Equals}
     * - {@link Operator#Not_equals}
     * - {@link Operator#Greater_than}
     * - {@link Operator#Greather_than_or_equals}
     * - {@link Operator#Less_than}
     * - {@link Operator#Less_than_or_equals}
     * - {@link Operator#Before}
     * - {@link Operator#After}
     * - {@link Operator#Between}
     * - {@link Operator#Contains} (both for collections and strings)
     * - {@link Operator#Like}
     * - {@link Operator#In}
     *
     * @param operator the operator to apply
     * @param propertyName the name of the property used as left hand side of the operation
     * @param type the type of the property
     * @param value the value used as right hand side of the operation
     * @return a {@link org.hibernate.criterion.Criterion} object representing the operation.
     */
    private static Criterion criterionForOperator(Operator operator, String propertyName, DataType type, Object value) {
        if (!EnumSet.of(Equals, Not_equals).contains(operator) &&
            (value == null || (value instanceof Collection && ((Collection)value).contains(null)))) {
            throw new QueryBuilderException(
                String.format("Null value not supported for \"%s\" operator.", operator));
        }
        switch(operator) {
            case Equals:
                if (value == null){
                    return Restrictions.isNull(propertyName);
                } else {
                    return Restrictions.eq(propertyName, value);
                }
            case Not_equals:
                if (value == null){
                    return Restrictions.isNotNull(propertyName);
                } else {
                    return Restrictions.ne(propertyName, value);
                }
            case Greater_than:
            case After:
                return Restrictions.gt(propertyName, value);
            case Greather_than_or_equals:
                return Restrictions.ge(propertyName, value);
            case Less_than:
            case Before:
                return Restrictions.lt(propertyName, value);
            case Less_than_or_equals:
                return Restrictions.le(propertyName, value);
            case Between:
                List<Date> values = (List<Date>)value;
                return Restrictions.between(propertyName, values.get(0), values.get(1));
            case Contains:
                if (type == DataType.String || type == DataType.Text) {
                    return StringUtils.like(propertyName, value.toString(), MatchMode.ANYWHERE);
                } else {
                    throw new QueryBuilderException(
                            String.format("Operator \"%s\" supported only for STRING and TEXT property types.",
                                operator));
                }
            case Like:
                return StringUtils.like(propertyName, value.toString(), MatchMode.EXACT);
            case In:
                return Restrictions.in(propertyName, ((Collection)value).toArray());
            default:
                throw new QueryBuilderException(String.format(
                    "Operator \"%s\" not supported.", operator));
        }
    }

    /**
     * Creates a {@link org.hibernate.criterion.Criterion} for the Boolean constraint that operates
     * on a property and a value, see {@link #criterionForOperator}.
     * Adds a not null and not empty check for fields of type {@link DataType#Date}.
     *
     * @param operator the operator to apply
     * @param field the field used as left hand side of the operation
     * @param value the value used as right hand side of the operation
     * @return a {@link org.hibernate.criterion.Criterion} object representing the operation.
     */
    private Criterion applyOperator(Operator operator, Field field, Object value) {
        String propertyName = getFieldPropertyName(field);
        Object convertedValue = convertValue(field, value);
        Criterion criterion = criterionForOperator(operator, propertyName, field.getType(), convertedValue);
        Class<?> fieldType = dimensionRegistry.forDimensionName(field.getDimension()).getFieldTypes().get(propertyName);
        if (fieldType != null && Date.class.isAssignableFrom(fieldType)) {
            return Restrictions.and(
                    Restrictions.isNotNull(propertyName),
                    Restrictions.ne(propertyName, EMPTY_DATE),
                    criterion
            );
        } else {
            return criterion;
        }
    }

    /**
     * Creates a criteria object for a field constraint. Applies {@link #convertValue(Field, java.lang.Object)} on the value
     *
     * @throws QueryBuilderException if the field type does not support the operator or the value is not supported
     * for the field type.
     * @see {@link Operator} and {@link DataType} for supported operators and types.
     */
    public Criterion build(FieldConstraint constraint) {
        DimensionMetadata metadata = dimensionRegistry.forDimensionName(constraint.getField().getDimension());
        Field field = metadata.getMappedField(constraint.getField().getFieldName());
        if (!DataTypeValidation.supportsType(constraint.getOperator(), field.getType())) {
            throw new QueryBuilderException(String.format(
                "Field type %s not supported for operator '%s'.",
                field.getType(), constraint.getOperator()));
        }
        if (EnumSet.of(In, Between).contains(constraint.getOperator())) {
            if (constraint.getValue() instanceof Collection) {
                for (Object val: (Collection)constraint.getValue()) {
                    if (!DataTypeValidation.supportsValue(field.getType(), val)) {
                        throw new QueryBuilderException(String.format(
                            "Value of class %s not supported for field type '%s'.",
                            val == null ? null : val.getClass().getSimpleName(), field.getType()));
                    }
                }
            } else {
                throw new QueryBuilderException(String.format(
                    "Expected collection, got %s.",
                    constraint.getValue() == null ? null : constraint.getValue().getClass().getSimpleName()));
            }
        } else {
            if (!DataTypeValidation.supportsValue(field.getType(), constraint.getValue())) {
                throw new QueryBuilderException(String.format(
                    "Value of class %s not supported for field type '%s'.",
                    constraint.getValue() == null ? null : constraint.getValue().getClass().getSimpleName(), field.getType()));
            }
        }
        Criterion criterion = applyOperator(constraint.getOperator(), field, constraint.getValue());
        if (field.getDimension().equals("visit")) {
            /*
             * special case that requires a subquery, because there is no proper
             * reference to the visit dimension in {@link ObservationFact}.
             */
            DetachedCriteria subCriteria = DetachedCriteria
                    .forClass(VisitEntity.class, "visit")
                    .add(criterion)
                    .setProjection(Projections.id());
            return Subqueries.propertyIn("encounterId", subCriteria);
        } else {
            return criterion;
        }
    }

    /**
     * Creates a criteria object for the time constraint by conversion to a field constraint for the start time field.
     */
    public Criterion build(TimeConstraint constraint) {
        switch(constraint.getOperator()) {
            case Before:
            case After:
            case Greather_than_or_equals:
            case Less_than_or_equals:
                return build(FieldConstraint.builder()
                    .field(constraint.getField())
                    .operator(constraint.getOperator())
                    .value(constraint.getValues().get(0))
                    .build());
            case Between:
                return build(FieldConstraint.builder()
                    .field(constraint.getField())
                    .operator(constraint.getOperator())
                    .value(constraint.getValues())
                    .build());
            default:
                throw new QueryBuilderException(String.format(
                    "Operator \"%s\" not supported.", constraint.getOperator()));
        }
    }

    /**
     * FIXME:
     * Implement support for biomarker constraints.
     */
    public Criterion build(BiomarkerConstraint constraint) {
        throw new NotImplementedException();
    }

    /**
     * Creates a criteria object for a patient set by conversion to a field constraint for the patient id field.
     * Note: OFFSET is a syntax tthat is used by Oracle starting from
     */
    public Criterion build(PatientSetConstraint constraint) {
        if (constraint.getPatientIds() != null) {
            return build(FieldConstraint.builder()
                .field(patientIdField)
                .operator(Operator.In)
                .value(constraint.getPatientIds())
                .build());
        } else if (constraint.getPatientSetId() != null) {
            DetachedCriteria subCriteria = DetachedCriteria.forClass(PatientSetCollectionEntity.class, "qt_patient_set_collection");
            log.debug("Subquery on patient set with id " + constraint.getPatientSetId());
            if (constraint.getOffset() != null && constraint.getLimit() != null) {
                log.debug("Restrict subquery to offset {}, limit {}}", constraint.getOffset(), constraint.getLimit());
                subCriteria.add(Restrictions.sqlRestriction(
                        "{alias}.result_instance_id = ? order by {alias}.patient_num offset ? limit ?",
                    new Object[] {constraint.getPatientSetId(), constraint.getOffset(), constraint.getLimit()},
                    new org.hibernate.type.Type[] {LongType.INSTANCE, IntegerType.INSTANCE, IntegerType.INSTANCE}));
            } else {
                subCriteria.add(Restrictions.eq("resultInstance.id", constraint.getPatientSetId()));
            }
            return Subqueries.propertyIn("patient", subCriteria.setProjection(Projections.property("patient")));
        } else if (constraint.getSubjectIds() != null) {
            DetachedCriteria subCriteria = DetachedCriteria.forClass(PatientMappingEntity.class, "patient_mapping");
            subCriteria.add(Restrictions.in("encryptedId", constraint.getSubjectIds()));
            subCriteria.add(Restrictions.eq("source", SUBJECT_ID_SOURCE));
            return Subqueries.propertyIn("patient", subCriteria.setProjection(Projections.property("patient")));
        } else {
            throw new QueryBuilderException("Constraint value not specified: " + constraint.getClass().getSimpleName());
        }
    }

    /**
     * Builds a subquery for a constraint that appears in a subselect constraint.
     * Creates a new subquery on {@link ObservationEntity} with the given constraint
     * and projects for the subselect dimension.
     *
     * @param dimension the dimension to subselect on.
     * @param constraint the subquery constraint.
     * @return the subquery.
     */
    private DetachedCriteria buildSubselect(DimensionMetadata dimension, Constraint constraint) {
        log.debug("Subselect on dimension " + dimension.getDimension().getName());

        DetachedCriteria constraintSubQuery = subQueryBuilder().buildCriteria(constraint);

        ProjectionList projection = Projections.projectionList();
        List<String> subSelectionPropertyNames = subSelectionPropertyNames(dimension);
        for (String propertyName: subSelectionPropertyNames) {
            projection.add(Projections.property(propertyName));
        }

        constraintSubQuery.setProjection(projection);

        if (dimension.getType() == Dimension.ImplementationType.STUDY) {
            // What we actually want is something like
            //
            // def subquery = subQueryBuilder().buildCriteria(constraint.constraint)
            // subquery.projection = Projections.property("trialVisit.study")
            // return Subqueries.propertyIn("trialVisit.study", subquery)
            //
            // but the criterion api doesn't like "trialVisit.study" as an identifier. I couldn't get it to work
            // with joins, so now using a bunch of subqueries

            // select trial visits from subselection observations
            // select studies from trial visits
            DetachedCriteria subQuery1 = DetachedCriteria.forClass(TrialVisitEntity.class).setProjection(Projections.property("study"));
            subQuery1.add(Subqueries.propertyIn("id", constraintSubQuery));

            // select trial visits from studies
            DetachedCriteria subQuery2 = DetachedCriteria.forClass(TrialVisitEntity.class);
            subQuery2.setProjection(Projections.property("id"));
            subQuery2.add(Subqueries.propertyIn("study", subQuery1));

            // limit to the last set of trial visits
            return subQuery2;
        }

        if (dimension.getType() ==  Dimension.ImplementationType.MODIFIER) {
            DetachedCriteria hasModifierSubQuery = DetachedCriteria.forClass(ObservationEntity.class, "has_mods_obf");
            hasModifierSubQuery.setProjection(projection);
            ModifierDimension modifierDimension = (ModifierDimension) dimension.getDimension();
            Criterion modifierObservationRow = Restrictions.eq("modifierCode", modifierDimension.getModifierCode());
            hasModifierSubQuery.add(modifierObservationRow);

            DetachedCriteria modifierValuesSubQuery = DetachedCriteria.forClass(ObservationEntity.class, "mod_vals_obf");
            String modifierValueField = ObservationEntity.observationFactValueField(modifierDimension.getValueType());
            modifierValuesSubQuery.setProjection(Projections.property(modifierValueField));
            modifierValuesSubQuery.add(modifierObservationRow);
            modifierValuesSubQuery.add(Subqueries.propertiesIn(subSelectionPropertyNames.toArray(new String[0]), constraintSubQuery));
            hasModifierSubQuery.add(Subqueries.propertyIn(modifierValueField, modifierValuesSubQuery));

            return hasModifierSubQuery;
        }
        return constraintSubQuery;
    }

    /**
     * Fetches the property names to project on for subselects on a dimension.
     *
     * @param dimension the dimension to get the subselect projection for.
     * @return the list of property names, to be used in the subselection projection.
     */
    private static List<String> subSelectionPropertyNames(DimensionMetadata dimension) {
        switch(dimension.getType()) {
            case TABLE:
            case COLUMN:
            case VISIT:
                String fieldName = dimension.getFieldName();
                return Arrays.asList(fieldName);
            case STUDY:
                return Arrays.asList("trialVisit");
            case MODIFIER:
                return Arrays.asList("encounterId", "patient", "concept", "providerId", "startDate", "instance");
        }
        throw new QueryBuilderException(String.format(
            "Dimension %s is not supported in subselection constraints",
            dimension.getDimension().getName()));
    }

    /**
     * Builds a subquery for checking if an observation appears in a intersection or union or several subqueries.
     * The constraint object should have operator {@link Operator#Intersect} or {@link Operator#Union},
     * a dimension to subquery on, and a non-empty list of subquery constraints.
     *
     * Because the Hibernate API does not directly support intersect and union operators, nested subqueries are constructed:
     * instead of querying { p | p in (A intersect B) } directly, a nested quer
     * { p | p in { p' in A | p' in B } }.
     *
     * Example SQL for subselections on the patient dimension, for constraints A and B:
     * <code>patient_num in (select o1.patient_num from ObservationFact o1 where A and o1.patient_num in
     *      (select o2.patient_num from ObservationFact o2 where B))</code>
     * This is equivalent to :
     * <code>patient_num in ((select o1.patient_num from ObservationFact o1 where A) intersect
     *      (select o2.patient_num from ObservationFact o2 where B))</code>.
     *
     * @param constraint the constraint object.
     * @return the criterion for this constraint.
     */
    public Criterion build(MultipleSubSelectionsConstraint constraint) {
        if (constraint.args.isEmpty()) {
            throw new QueryBuilderException("Empty list of subselection constraints.");
        }
        DimensionMetadata dimension = dimensionRegistry.forDimensionName(constraint.getDimension());
        log.debug(String.format("Build subselection constraints (%s, %s, %d)",
            constraint.getDimension(), constraint.getOperator(), constraint.getArgs().size()));
        LinkedList<Constraint> args = new LinkedList<>(constraint.getArgs());
        Constraint query = args.remove();
        List<Constraint> tail = args;
        if (!tail.isEmpty()) {
            Constraint tailConstraint = new MultipleSubSelectionsConstraint(dimension.getDimension().getName(), constraint.getOperator(), tail);
            switch(constraint.getOperator()) {
                case Intersect:
                    query = AndConstraint.builder().args(Arrays.asList(query, tailConstraint)).build();
                    break;
                case Union:
                    query = OrConstraint.builder().args(Arrays.asList(query, tailConstraint)).build();
                    break;
                default:
                    throw new QueryBuilderException("Operator not supported: " + constraint.getOperator().toString());
            }
        }
        DetachedCriteria criteria = buildSubselect(dimension, query);
        List<String> propertyNames = subSelectionPropertyNames(dimension);
        Criterion result;
        if (propertyNames.size() == 1) {
            result = Subqueries.propertyIn(propertyNames.get(0), criteria);
        } else {
            result = Subqueries.propertiesIn((String[])propertyNames.toArray(), criteria);
        }
        return result;
    }

    public Criterion build(SubSelectionConstraint constraint) {
        return build(new MultipleSubSelectionsConstraint(constraint.getDimension(),
            Operator.Intersect,
            Collections.singletonList(constraint.getConstraint()))
        );
    }

    public Criterion build(ConceptConstraint constraint){
        if (constraint.getConceptCode() != null) {
            return Restrictions.eq("concept", constraint.getConceptCode());
        } else if (constraint.getConceptCodes() != null && !constraint.getConceptCodes().isEmpty()) {
            return InQuery.inValues("concept", constraint.getConceptCodes());
        } else if (constraint.getPath() != null) {
            DetachedCriteria subCriteria = DetachedCriteria.forClass(ConceptEntity.class, "concept_dimension");
            subCriteria.add(Restrictions.eq("concept_dimension.conceptPath", constraint.getPath()));
            return Subqueries.propertyEq("concept", subCriteria.setProjection(Projections.id()));
        } else {
            throw new QueryBuilderException("No path or conceptCode in concept constraint.");
        }
    }

    public Criterion build(StudyNameConstraint constraint){
        if (constraint.getStudyId() == null) {
            throw new QueryBuilderException("Study constraint shouldn't have a null value for studyId");
        }
        DetachedCriteria studyCriteria = DetachedCriteria.forClass(StudyEntity.class, "study");
        studyCriteria.add(Restrictions.eq("study.studyId", constraint.getStudyId()));
        DetachedCriteria trialVisitCriteria = DetachedCriteria.forClass(TrialVisitEntity.class);
        trialVisitCriteria.add(Subqueries.propertyIn("study", studyCriteria.setProjection(Projections.id())));
        return Subqueries.propertyEq("trialVisit", trialVisitCriteria.setProjection(Projections.id()));
    }

    /*
    public Criterion build(StudyObjectConstraint constraint){
        if (constraint.getStudy() == null) {
            throw new QueryBuilderException("Study id constraint shouldn't have a null value for ids");
        }
        List<TrialVisit> trialVisits = trialVisitsService.findTrialVisitsForStudy(constraint.getStudy());
        if (trialVisits.isEmpty()) {
            // Return false if there are no trial visits to filter on
            return build(new Negation(new TrueConstraint()));
        }
        return Restrictions.in("trialVisit", trialVisits);
    }
    */

    public Criterion build(NullConstraint constraint){
        String propertyName = getFieldPropertyName(constraint.getField());
        return Restrictions.isNull(propertyName);
    }

    /**
     * Creates a criteria object the represents the negation of <code>constraint.arg</code>.
     */
    public Criterion build(Negation constraint) {
        return Restrictions.not(build(constraint.getArg()));
    }

    /**
     * Gets the set operator that corresponds to the boolean operator:
     * {@link Operator#Intersect} for {@link Operator#And},
     * {@link Operator#Union} for {@link Operator#Or}.
     * Correspondence in the sense that:
     * <code>A intersect B == { v | v in A and v in B }}</code>
     *
     * @param booleanOperator the boolean operator.
     * @return the corresponding set operator.
     */
    private static Operator getSetOperator(Operator booleanOperator) {
        switch (booleanOperator) {
            case And:
                return Operator.Intersect;
            case Or:
                return Operator.Union;
            default:
                throw new QueryBuilderException(String.format(
                    "Operator not supported: %s", booleanOperator.name()));
        }
    }

    /**
     * Creates a criteria object for the conjunction (if <code>constraint.operator == AND</code>) or
     * disjunction (if <code>constraint.operator == OR</code>) of the constraints in <code>constraint.args</code>.
     * @param operator
     * @param args
     * @return
     */
    private Criterion buildCombination(Operator operator, List<Constraint> args) {
        List<SubSelectionConstraint> subSelectConstraints = new ArrayList<>();
        List<Constraint> currentLevelConstraints = new ArrayList<>();
        for (Constraint arg: args) {
            if (arg instanceof SubSelectionConstraint) {
                subSelectConstraints.add((SubSelectionConstraint)arg);
            } else {
                currentLevelConstraints.add(arg);
            }
        }
        List<Criterion> parts = currentLevelConstraints.stream()
            .map(this::build).collect(Collectors.toList());
        /*
         * Combine subselect queries for the same dimension into a single {@link MultipleSubSelectionsConstraint}.
         *
         * Rationale:
         * { p | (p in A) and (p in B) } is equivalent to { p | p in (A intersect B) }.
         *
         * It appears that the latter results in a better query plan on PostgreSQL databases.
         */
        Map<String, List<SubSelectionConstraint>> subSelectConstraintsByDimension =
            subSelectConstraints.stream().collect(Collectors.groupingBy(SubSelectionConstraint::getDimension));
        for (Map.Entry<String, List<SubSelectionConstraint>> entry: subSelectConstraintsByDimension.entrySet()) {
            MultipleSubSelectionsConstraint dimensionSubselect = new MultipleSubSelectionsConstraint(
                    entry.getKey(),
                    getSetOperator(operator),
                    entry.getValue().stream().map(SubSelectionConstraint::getConstraint).collect(Collectors.toList()));
            parts.add(build(dimensionSubselect));
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        switch (operator) {
            case And:
                return Restrictions.and((Criterion[])parts.toArray());
            case Or:
                return Restrictions.or((Criterion[])parts.toArray());
            default:
                throw new QueryBuilderException("Operator not supported: " + operator);
        }
    }

    @Override
    public Criterion build(AndConstraint constraint) {
        return buildCombination(Operator.And, constraint.getArgs());
    }

    @Override
    public Criterion build(OrConstraint constraint) {
        return buildCombination(Operator.Or, constraint.getArgs());
    }

    /**
     * Creates a criteria object that performs the subquery in <code>constraint.eventQuery</code>
     * and selects all observations for the same patient that start before the earliest start (if
     * <code>constraint.operator == BEFORE</code>) or start after the last start (if <code>constraint.operator == AFTER</code>)
     * event selected by the subquery.
     * If <code>constraint.operator == EXISTS</code>, all observations are selected of patients for which
     * the subquery does not yield an empty result.
     */
    public Criterion build(TemporalConstraint constraint) {
        Constraint eventConstraint = constraint.getEventConstraint();
        HibernateCriteriaQueryBuilder subQueryBuilder = subQueryBuilder();
        DetachedCriteria subquery = subQueryBuilder.buildCriteria(eventConstraint);
        String observationFactAlias = getAlias("observation_fact");
        String subqueryAlias = subQueryBuilder.getAlias("observation_fact");
        subquery.add(Restrictions.eqProperty(observationFactAlias + ".patient", subqueryAlias + ".patient"));
        switch (constraint.getOperator()) {
            case Before:
                return Subqueries.propertyLt("startDate",
                        subquery.setProjection(Projections.min(startTimeField.getFieldName()))
                );
            case After:
                return Subqueries.propertyGt("startDate",
                        subquery.setProjection(Projections.max(startTimeField.getFieldName()))
                );
            case Equals:
                return Subqueries.exists(
                        subquery.setProjection(Projections.id())
                );
            default:
                throw new QueryBuilderException("Operator not supported: " + constraint.getOperator().toString());
        }
    }

    /**
     * Builds a hibernate criterion to find observations for the patients that have a relation
     * of the given type (e.g. Parent-child) and with optionally specified, by constraint, patients.
     */
    public Criterion build(RelationConstraint relationConstraint) {
        DetachedCriteria relationCriteria = DetachedCriteria.forClass(RelationEntity.class, "relation");
        if (relationConstraint.getRelatedSubjectsConstraint() != null) {
            DetachedCriteria patientCriteria = subQueryBuilder()
                .buildCriteria(relationConstraint.getRelatedSubjectsConstraint(), null)
                .setProjection(Projections.property("patient"));
            // I get NPE when I use whole object instead of id projection
            // TODO For some cases the sub-query could be made more efficient to execute bypassing unnecessary table joins/nested sub-queries.
            relationCriteria.add(Subqueries.propertyIn("rightSubject.id",
                    patientCriteria.setProjection(Projections.id())));
        }
        /* FIXME
        RelationType relationType = relationTypeResource.getByLabel(relationConstraint.getRelationTypeLabel());
        if (relationType == null) {
            throw new QueryBuilderException(String.format("No %s relation type found.", relationConstraint.getRelationTypeLabel()));
        }
        */
        DetachedCriteria relationTypeCriteria = DetachedCriteria.forClass(RelationTypeEntity.class, "relation_type");
        relationTypeCriteria.add(Restrictions.eq("relation_type.label", relationConstraint.getRelationTypeLabel()));
        relationCriteria.add(Subqueries.propertyEq("relationType", relationTypeCriteria.setProjection(Projections.id())));
        if (relationConstraint.getBiological() != null) {
            relationCriteria.add(Restrictions.eq("biological", relationConstraint.getBiological()));
        }
        if (relationConstraint.getShareHousehold() != null) {
            relationCriteria.add(Restrictions.eq("shareHousehold", relationConstraint.getShareHousehold()));
        }
        //def patientAlias = getAlias("patient")
        return Subqueries.propertyIn("patient", relationCriteria.setProjection(Projections.property("leftSubject")));
    }

    /**
     * Builds a DetachedCriteria object representing the query for observation facts that satisfy
     * the constraint.
     *
     * @param constraint
     * @return
     */
    DetachedCriteria buildCriteria(Constraint constraint,
                                   Criterion modifierCriterion,
                                   Set<String> propertiesToReserveAliases) {
        aliases.clear();
        for (String property: propertiesToReserveAliases) {
            getAlias(property);
        }
        DetachedCriteria result = builder();
        List<Criterion> restrictions = Arrays.asList(build(constraint));
        if (!accessToAllStudies) {
            restrictions.add(getStudiesCriterion());
        }
        if (modifierCriterion != null) {
            restrictions.add(modifierCriterion);
        }
        for (Map.Entry<String, String> entry: aliases.entrySet()) {
            if (!entry.getKey().equals("observation_fact")) {
                result.createAlias(entry.getKey(), entry.getValue());
            }
        }
        Criterion criterion = Restrictions.and((Criterion[])restrictions.toArray());
        result.add(criterion);
        return result;
    }

    DetachedCriteria buildCriteria(Constraint constraint, Criterion modifierCriterion) {
        return buildCriteria(constraint, modifierCriterion, Collections.emptySet());
    }

    DetachedCriteria buildCriteria(Constraint constraint) {
        return buildCriteria(constraint, defaultModifierCriterion, Collections.emptySet());
    }

    /**
     * Returns a criterion that filters on studies.
     */
    private Criterion getStudiesCriterion() {
        if (studies.isEmpty()) {
            // Return false if there are no studies to filter on
            return build(new Negation(new TrueConstraint()));
        }
        return Restrictions.in(String.format("%s.study", getAlias("trialVisit")), studies);
    }

}
