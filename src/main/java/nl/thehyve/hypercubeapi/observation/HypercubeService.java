package nl.thehyve.hypercubeapi.observation;

import com.google.common.collect.ImmutableMap;
import nl.thehyve.hypercubeapi.query.HibernateCriteriaQueryBuilder;
import nl.thehyve.hypercubeapi.query.dimension.*;
import nl.thehyve.hypercubeapi.query.hypercube.Hypercube;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeImpl;
import nl.thehyve.hypercubeapi.query.hypercube.HypercubeQuery;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.*;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.StatelessSessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.transmartproject.common.constraint.Constraint;
import org.transmartproject.common.type.SortOrder;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class HypercubeService {

    private final SessionFactory sessionFactory;
    private final DimensionRegistry dimensionRegistry;

    @Autowired
    HypercubeService(EntityManagerFactory entityManagerFactory, DimensionRegistry dimensionRegistry) {
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.dimensionRegistry = dimensionRegistry;
    }

    private HypercubeQuery buildCriteria(StatelessSessionImpl session,
                                         Collection<Dimension> dimensions,
                                         Map<Dimension, SortOrder> orderDims) {
        Set<Dimension> nonSortableDimensions = orderDims.keySet().stream().filter(
            dimension -> !(dimension instanceof AliasAwareDimension)).collect(Collectors.toSet());
        if (!nonSortableDimensions.isEmpty()) {
            throw new RuntimeException("Sorting over these dimensions is not supported: " +
                nonSortableDimensions.stream().map(Dimension::getName).collect(Collectors.joining(", ")));
        }
        Map<AliasAwareDimension, SortOrder> orderByDimensions = orderDims.entrySet().stream().collect(Collectors.toUnmodifiableMap(
            (Map.Entry<Dimension, SortOrder> entry) -> (AliasAwareDimension)(entry.getKey()),
            Map.Entry<Dimension, SortOrder>::getValue));

        Criteria q = new CriteriaImpl("nl.thehyve.hypercubeapi.observation.ObservationEntity", "observation_fact", session);
        // The main reason to use this projections block is that it clears all the default projections that
        // select all fields.
        HypercubeQuery query = HypercubeQuery.builder()
            .criteria(q)
            .projections(Projections.projectionList()
                .add(Projections.property("valueType"), "valueType")
                .add(Projections.property("textValue"), "textValue")
                .add(Projections.property("numericalValue"), "numericalValue")
                .add(Projections.property("rawTextValue"), "rawTextValue")
            )
            .params(Map.of("modifierCodes", Collections.singletonList("@")))
            .build();

        for (Dimension dimension: dimensions) {
            dimension.selectIDs(query);
        }

        Map<Dimension, SortOrder> actualSortOrder = new HashMap<>();

        boolean hasModifiers = dimensions.stream().anyMatch(dimension -> dimension instanceof ModifierDimension);
        if (hasModifiers) {
            Set<String> sortableDimensionNames = dimensionRegistry.getSortableDimensionNames();
            String nonModifierSortableDimensions = orderByDimensions.keySet().stream()
                .filter(dimension -> !(sortableDimensionNames.contains(dimension.getName())))
                .map(Dimension::getName)
                .collect(Collectors.joining(", "));
            if (!nonModifierSortableDimensions.isEmpty()) {
                String modifierDimensions = dimensions.stream().filter(
                    dimension -> dimension instanceof ModifierDimension).map(
                    Dimension::getName).collect(Collectors.joining(", "));
                throw new RuntimeException(String.format(
                    "Sorting over these dimensions is not supported when querying the %s dimensions: %s",
                    modifierDimensions, nonModifierSortableDimensions));
            }

            // Make sure all primary key dimension columns are selected, even if they are not part of the result
            for (Dimension dimension: dimensionRegistry.getPrimaryKeyDimensions()) {
                DimensionImpl dim = (DimensionImpl)dimension;
                if (!(dimensions.contains(dim))) {
                    dim.selectIDs(query);
                }
            }

            Set<AliasAwareDimension> neededPrimaryKeyDimensions = dimensionRegistry.getPrimaryKeyDimensions().stream()
                .map(dimension -> (AliasAwareDimension)dimension).collect(Collectors.toSet());
            query.getProjections().add(Projections.property("instance"), "instance");

            for (Map.Entry<AliasAwareDimension, SortOrder> entry: orderByDimensions.entrySet()) {
                AliasAwareDimension aaDim = entry.getKey();
                SortOrder so = entry.getValue();
                q.addOrder(so == SortOrder.Desc ? Order.desc(aaDim.getAlias()) : Order.asc(aaDim.getAlias()));
                actualSortOrder.put(aaDim, so);
                neededPrimaryKeyDimensions.remove(aaDim);
            }
            for (AliasAwareDimension dimension: neededPrimaryKeyDimensions) {
                q.addOrder(Order.asc(dimension.getAlias()));
                actualSortOrder.put(dimension, SortOrder.Asc);
            }
            q.addOrder(Order.asc("instance"));
        } else {
            for (Map.Entry<AliasAwareDimension, SortOrder> entry: orderByDimensions.entrySet()) {
                AliasAwareDimension aaDim = entry.getKey();
                SortOrder so = entry.getValue();
                q.addOrder(so == SortOrder.Desc ? Order.desc(aaDim.getAlias()) : Order.asc(aaDim.getAlias()));
                actualSortOrder.put(aaDim, so);
            }
        }
        q.setProjection(query.getProjections());
        query.setActualSortOrder(ImmutableMap.copyOf(actualSortOrder));

        q.add(Restrictions.in("modifierCode", query.getParams().get("modifierCodes")));

        return query;
    }

    public void write(OutputStream output, Constraint constraint) throws IOException {
        StatelessSessionImpl session = (StatelessSessionImpl) sessionFactory.openStatelessSession();
        try {
            session.connection().setAutoCommit(false);
            Transaction tx = session.beginTransaction();

            HibernateCriteriaQueryBuilder queryBuilder = HibernateCriteriaQueryBuilder.forAllStudies(dimensionRegistry);
            Criterion criterion = queryBuilder.build(constraint);

            List<Dimension> allDimensions = dimensionRegistry.getAllDimensions();
            HypercubeQuery query = buildCriteria(session, allDimensions, Map.of());
            Criteria criteria = query.getCriteria();

            criteria.add(criterion);
            criteria.add(HibernateCriteriaQueryBuilder.defaultModifierCriterion);

            Hypercube hypercube = new HypercubeImpl(allDimensions, query);
            HypercubeJsonSerializer serializer = new HypercubeJsonSerializer(hypercube, output);
            serializer.write();

            tx.commit();
            // Write to output
            output.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected query exception", e);
        } finally {
            session.close();
        }

    }

}
