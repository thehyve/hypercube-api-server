package nl.thehyve.hypercubeapi.observation;

import nl.thehyve.hypercubeapi.query.HibernateCriteriaQueryBuilder;
import nl.thehyve.hypercubeapi.query.dimension.DimensionRegistry;
import nl.thehyve.hypercubeapi.type.ValueType;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.StatelessSessionImpl;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.dto.CategoricalValueAggregates;
import org.transmartproject.common.dto.Counts;
import org.transmartproject.common.dto.NumericalValueAggregates;

import javax.persistence.EntityManagerFactory;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
public class AggregateService {

    private final SessionFactory sessionFactory;
    private final DimensionRegistry dimensionRegistry;

    @Autowired
    AggregateService(EntityManagerFactory entityManagerFactory, DimensionRegistry dimensionRegistry) {
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.dimensionRegistry = dimensionRegistry;
    }

    Counts counts(@Valid Constraint constraint) {
        StatelessSessionImpl session = (StatelessSessionImpl) sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        HibernateCriteriaQueryBuilder queryBuilder = HibernateCriteriaQueryBuilder.forAllStudies(dimensionRegistry);
        Criterion criterion = queryBuilder.build(constraint);
        try {
            Criteria criteria = session.createCriteria(ObservationEntity.class);
            criteria.add(criterion);
            criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);
            criteria.setProjection(Projections.projectionList()
                .add(Projections.rowCount(), "observationCount")
                .add(Projections.countDistinct("patient"), "patientCount")
            );
            criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            Map row = (Map)criteria.uniqueResult();
            tx.commit();
            return Counts.builder()
                .observationCount((Long)row.get("observationCount"))
                .patientCount((Long)row.get("patientCount"))
                .build();
        } finally {
            session.close();
        }
    }

    Map<String, Counts> countsPerConcept(@Valid Constraint constraint) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        HibernateCriteriaQueryBuilder queryBuilder = HibernateCriteriaQueryBuilder.forAllStudies(dimensionRegistry);
        Criterion criterion = queryBuilder.build(constraint);
        try {
            DetachedCriteria criteria = DetachedCriteria.forClass(ObservationEntity.class);
            criteria.add(criterion);
            criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);
            criteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty("concept"), "concept")
                .add(Projections.rowCount(), "observationCount")
                .add(Projections.countDistinct("patient"), "patientCount")
            );
            criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            List<Map> rows = (List<Map>)criteria.getExecutableCriteria(session).list();
            tx.commit();
            return rows.stream().collect(Collectors.toMap(
                (Map row) -> (String)row.get("concept"),
                (Map row) -> Counts.builder()
                    .observationCount((Long)row.get("observationCount"))
                    .patientCount((Long)row.get("patientCount"))
                    .build()));
        } finally {
            session.close();
        }
    }

    Map<String, Counts> countsPerStudy(@Valid Constraint constraint) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        HibernateCriteriaQueryBuilder queryBuilder = HibernateCriteriaQueryBuilder.forAllStudies(dimensionRegistry);
        Criterion criterion = queryBuilder.build(constraint);
        try {
            DetachedCriteria criteria = DetachedCriteria.forClass(ObservationEntity.class);
            criteria.createAlias("trialVisit", "trialVisit");
            criteria.createAlias("trialVisit.study", "study");
            criteria.add(criterion);
            criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);
            criteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty("study.studyId"), "study")
                .add(Projections.rowCount(), "observationCount")
                .add(Projections.countDistinct("patient"), "patientCount")
            );
            criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            List<Map> rows = (List<Map>)criteria.getExecutableCriteria(session).list();
            tx.commit();
            return rows.stream().collect(Collectors.toMap(
                (Map row) -> (String)row.get("study"),
                (Map row) -> Counts.builder()
                    .observationCount((Long)row.get("observationCount"))
                    .patientCount((Long)row.get("patientCount"))
                    .build()));
        } finally {
            session.close();
        }
    }

    Map<String, Map<String, Counts>> countsPerStudyAndConcept(@Valid Constraint constraint) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        HibernateCriteriaQueryBuilder queryBuilder = HibernateCriteriaQueryBuilder.forAllStudies(dimensionRegistry);
        Criterion criterion = queryBuilder.build(constraint);
        try {
            DetachedCriteria criteria = DetachedCriteria.forClass(ObservationEntity.class);
            criteria.createAlias("trialVisit", "trialVisit");
            criteria.createAlias("trialVisit.study", "study");
            criteria.add(criterion);
            criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);
            criteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty("study.studyId"), "study")
                .add(Projections.groupProperty("concept"), "concept")
                .add(Projections.rowCount(), "observationCount")
                .add(Projections.countDistinct("patient"), "patientCount")
            );
            criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            List<Map> rows = (List<Map>)criteria.getExecutableCriteria(session).list();
            tx.commit();
            return rows.stream().collect(Collectors.groupingBy(
                (Map row) -> (String)row.get("study"),
                Collectors.toMap(
                    (Map row) -> (String)row.get("concept"),
                    (Map row) -> Counts.builder()
                        .observationCount((Long)row.get("observationCount"))
                        .patientCount((Long)row.get("patientCount"))
                        .build())));
        } finally {
            session.close();
        }
    }

    Map<String, NumericalValueAggregates> numericalValueAggregatesPerConcept(@Valid Constraint constraint) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        HibernateCriteriaQueryBuilder queryBuilder = HibernateCriteriaQueryBuilder.forAllStudies(dimensionRegistry);
        Criterion criterion = queryBuilder.build(constraint);
        try {
            DetachedCriteria criteria = DetachedCriteria.forClass(ObservationEntity.class);
            criteria.add(criterion);
            criteria.add(Restrictions.eq("valueType", ValueType.Number));
            criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);
            criteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty("concept"), "concept")
                .add(Projections.min("numericalValue"), "min")
                .add(Projections.max("numericalValue"), "max")
                .add(Projections.avg("numericalValue"), "avg")
                .add(Projections.count("numericalValue"), "count")
                .add(Projections.sqlProjection(
                    "STDDEV_SAMP(nval_num) as stdDev",
                    new String[]{"stdDev"},
                    new org.hibernate.type.DoubleType[]{StandardBasicTypes.DOUBLE}))
            );
            criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            List<Map> rows = (List<Map>)criteria.getExecutableCriteria(session).list();
            tx.commit();
            return rows.stream().collect(Collectors.toMap(
                (Map row) -> (String)row.get("concept"),
                (Map row) -> NumericalValueAggregates.builder()
                    .min(((BigDecimal)row.get("min")).doubleValue())
                    .max(((BigDecimal)row.get("max")).doubleValue())
                    .avg(((Double)row.get("avg")))
                    .count(((Long)row.get("count")).intValue())
                    .stdDev(((Double)row.get("stdDev")))
                    .build()));
        } finally {
            session.close();
        }
    }

    Map<String, CategoricalValueAggregates> categoricalValueAggregatesPerConcept(@Valid Constraint constraint) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        HibernateCriteriaQueryBuilder queryBuilder = HibernateCriteriaQueryBuilder.forAllStudies(dimensionRegistry);
        Criterion criterion = queryBuilder.build(constraint);
        try {
            DetachedCriteria criteria = DetachedCriteria.forClass(ObservationEntity.class);
            criteria.add(criterion);
            criteria.add(Restrictions.eq("valueType", ValueType.Text));
            criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);
            criteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty("concept"), "concept")
                .add(Projections.groupProperty("textValue"), "value")
                .add(Projections.rowCount(), "count")
            );
            criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            List<Map> rows = (List<Map>)criteria.getExecutableCriteria(session).list();
            tx.commit();
            Map<String, Map<String, Integer>> counts = rows.stream().collect(Collectors.groupingBy(
                (Map row) -> (String)row.get("concept"),
                Collectors.toMap(
                    (Map row) -> (String)row.get("value"),
                    (Map row) -> ((Long)row.get("count")).intValue())
            ));
            return counts.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                (Map.Entry<String, Map<String, Integer>> entry) -> {
                    Map<String, Integer> valueCounts = entry.getValue();
                    Integer nullCounts = valueCounts.remove(null);
                    return CategoricalValueAggregates.builder()
                        .nullValueCounts(nullCounts)
                        .valueCounts(valueCounts)
                        .build();
                }
            ));
        } finally {
            session.close();
        }
    }

}
