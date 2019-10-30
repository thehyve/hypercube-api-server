/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package nl.thehyve.hypercubeapi.query.hypercube;

import com.google.common.collect.*;
import nl.thehyve.hypercubeapi.query.dimension.*;
import nl.thehyve.hypercubeapi.type.ObservationValueMapper;
import nl.thehyve.hypercubeapi.type.ValueType;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.internal.CriteriaImpl;
import org.transmartproject.common.type.SortOrder;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
public class HypercubeImpl implements Hypercube {
    /*
     * The data representation:
     *
     * For dense dimensions:
     * Dimension element keys are stored in dimensionElementIdxes. Those are mapped to the actual dimension elements
     * in dimensionElements. Each dimension has a numeric index in dimensionsIndexMap. Each ClinicalValue
     */

    private static final int FETCH_SIZE = 10000;

    private CriteriaImpl criteria;

    // ImmutableMap guarantees the same iteration order as the input, and can in fact be converted efficiently to an
    // ImmutableList<Entry>.
    private final ImmutableMap<Dimension, Integer> dimensionsIndex;
    private final ImmutableList<Dimension> dimensions;
    private final ImmutableMap<String, Integer> aliases;
    private final ImmutableList<ModifierDimension> modifierDimensions;
    private final ImmutableList<Dimension> denseDimensions;
    private final ImmutableMap<Dimension, SortOrder> sortOrder;

    // Map from Dimension -> dimension element keys
    // The IndexedArraySet provides efficient O(1) indexOf/contains operations
    // Only used for non-inline dimensions
    private final Map<Dimension, IndexedArraySet<?>> dimensionElementKeys;
    private int completeScanNumber = 0;

    // A map that stores the actual dimension elements once they are loaded
    private Map<Dimension, ImmutableList<Object>> dimensionElements = new HashMap<>();

    public HypercubeImpl(Collection<Dimension> dimensions, HypercubeQuery query) throws SQLException {
        this.dimensions = ImmutableList.copyOf(dimensions);
        this.dimensionsIndex = ImmutableMap.copyOf(IntStream.range(0, this.dimensions.size()).boxed()
            .collect(Collectors.toMap(
                this.dimensions::get,
                (Integer index) -> index
            )));
        this.modifierDimensions = ImmutableList.copyOf(dimensions.stream()
            .filter(dimension -> dimension instanceof ModifierDimension)
            .map(dimension -> (ModifierDimension)dimension)
            .collect(Collectors.toList()));
        this.denseDimensions = ImmutableList.copyOf(dimensions.stream()
            .filter(dimension -> dimension.getDensity() != null && dimension.getDensity().isDense())
            .collect(Collectors.toList()));
        this.sortOrder = query.getActualSortOrder();

        this.criteria = query.getCriteriaImpl();
        //to run the query in the same transaction all the time. e.g. for dimensions elements loading and data receiving.
        this.criteria.getSession().connection().setAutoCommit(false);
        String[] aliases = this.criteria.getProjection().getAliases();
        this.aliases = ImmutableMap.copyOf(IntStream.range(0, aliases.length).boxed().collect(Collectors.toMap(
            index -> aliases[index],
            index -> index
        )));
        this.dimensionElementKeys = new HashMap<>();
    }

    private void scanCompleted() {
        completeScanNumber += 1;
    }

    @Override
    public PeekingIterator<HypercubeValue> iterator() {
        return new ResultIterator();
    }

    @Override
    public ImmutableList<Object> dimensionElements(Dimension dim) {
        checkDimension(dim);
        checkIsDense(dim);
        if (!(dimensionElements.containsKey(dim))) {
            IndexedArraySet<Object> dimElKeys = dimensionElementKeys(dim);
            if (dimElKeys != null && !dimElKeys.isEmpty()) {
                dimensionElements.put(dim, ImmutableList.copyOf(dim.resolveElements(dimElKeys)));
            } else {
                dimensionElements.put(dim, ImmutableList.of());
            }
        }
        return dimensionElements.get(dim);
    }

    @Override
    public List<Dimension> getDimensions() {
        return this.dimensions;
    }

    @Override
    public Object dimensionElement(Dimension dim, Integer idx) {
        return dimensionElements(dim).get(idx);
    }

    private <KeyType> IndexedArraySet<KeyType> dimensionElementKeys(Dimension<KeyType, ?> dim) {
        checkDimension(dim);
        checkIsDense(dim);
        if (completeScanNumber <= 0) {
            fetchAllDimensionsElementKeys();
        }
        return (IndexedArraySet<KeyType>)dimensionElementKeys.get(dim);
    }

    @Override
    public Object dimensionElementKey(Dimension dim, Integer idx) {
        checkDimension(dim);
        checkIsDense(dim);
        if (completeScanNumber <= 0 && !dimensionElementKeys.get(dim).contains(idx)) {
            fetchAllDimensionsElementKeys();
        }
        return dimensionElementKeys.get(dim).get(idx);
    }

    @Override
    public ImmutableMap<Dimension, SortOrder> getSortOrder() {
        return this.sortOrder;
    }

    private <KeyType> int indexDimensionElement(Dimension dimension, KeyType dimensionElementKey) {
        IndexedArraySet<KeyType> elementKeys = (IndexedArraySet<KeyType>)dimensionElementKeys.get(dimension);
        if (elementKeys == null) {
            elementKeys = new IndexedArraySet<>();
            dimensionElementKeys.put(dimension, elementKeys);
        }
        int dimElementIdx = elementKeys.indexOf(dimensionElementKey);
        if (dimElementIdx == -1) {
            dimElementIdx = elementKeys.size();
            elementKeys.add(dimensionElementKey);
        }
        return dimElementIdx;
    }

    private Object[] getDimensionElementIndexes(Map<String, Object> result) {
        // actually this array only contains indexes for dense dimensions, for sparse ones it contains the
        // element keys directly
        Object[] dimensionElementIdxes = new Object[dimensions.size()];
        // Save keys of dimension elements
        // Closures are not called statically afaik, even with @CompileStatic; use a plain old loop
        for (int i = 0; i < dimensionElementIdxes.length; i++) {
            Dimension dim = dimensions.get(i);
            Object dimElementKey = dim.getElementKey(result);
            if (dimElementKey == null) {
                dimensionElementIdxes[i] = null;
            } else if (dim.getDensity().isDense()) {
                dimensionElementIdxes[i] = indexDimensionElement(dim, dimElementKey);
            } else {
                dimensionElementIdxes[i] = dimElementKey;
            }
        }
        return dimensionElementIdxes;
    }

    /**
     * ResultIterator (and the derived DimensionsLoaderIterator) make intimate use of private HypercubeImpl data and
     * private methods.
     */
    private class ResultIterator extends AbstractIterator<HypercubeValue> implements PeekingIterator<HypercubeValue> {

        private final ScrollableResults results;
        private final Iterator<? extends Map<String, Object>> resultIterator;

        ResultIterator() {
            this.results = criteria
                .setFetchSize(FETCH_SIZE)
                .scroll(ScrollMode.FORWARD_ONLY);
            this.resultIterator = (modifierDimensions != null && !modifierDimensions.isEmpty()
                    ? new ModifierResultIterator(modifierDimensions, aliases, results)
                    : new ProjectionMapIterator(aliases, results)
            );
        }

        @Override
        public HypercubeValueImpl computeNext() {
            if (!resultIterator.hasNext()) {
                results.close();
                endOfData();
                scanCompleted();
                return (null);
            }

            Map<String, Object> row = resultIterator.next();
            return transformRow(row);
        }

        HypercubeValueImpl transformRow(Map<String, Object> row) {
            Object value = ObservationValueMapper.observationFactValue(
                (ValueType) row.get("valueType"),
                (String) row.get("textValue"),
                (BigDecimal) row.get("numericalValue"),
                (String) row.get("rawTextValue"));

            Object[] dimensionElementIdexes = getDimensionElementIndexes(row);

            return new HypercubeValueImpl(HypercubeImpl.this, dimensionElementIdexes, value);
        }
    }

    static void checkIsDense(Dimension dim) {
        if (!dim.getDensity().isDense()) {
            throw new UnsupportedOperationException("Cannot get dimension element for sparse dimension " +
                    dim.getClass().getSimpleName());
        }
    }

    void checkDimension(Dimension dim) {
        if (!(dimensions.contains(dim))) {
            throw new IllegalArgumentException("Dimension $dim is not part of this result");
        }
    }

    int getDimensionsIndex(Dimension dim) {
        Integer i = dimensionsIndex.get(dim);
        if (i == null) {
            throw new IllegalArgumentException("Dimension $dim is not part of this result");
        }
        return i;
    }

    private void fetchAllDimensionsElementKeys() {
        // exhaust the iterator for side effect
        Iterators.getLast(new DimensionsLoaderIterator());
    }

    private class DimensionsLoaderIterator extends ResultIterator {

        DimensionsLoaderIterator() {
            super();
        }

        HypercubeValueImpl transformRow(Map<String, Object> row) {
            for (Dimension dimension: denseDimensions) {
                Object key = dimension.getElementKey(row);
                if (key != null) {
                    indexDimensionElement(dimension, key);
                }
            }
            return null;
        }
    }

    public void close() throws IOException {
        if (!criteria.getSession().isClosed()) {
            if (criteria.getSession() instanceof Closeable) {
                ((Closeable) criteria.getSession()).close();
            } else {
                throw new IllegalStateException("Session can not be closed");
            }
        }
    }

    /**
     * Group modifiers together. Assumes the query is sorted on the primary key columns with modifierCd last.
     * The returned values are result maps that have the modifiers added in.
     *
     * Todo: This solution was meant as a temporary solution, with a better solution involving a database side join
     * of the modifier ObservationFacts to the non-modifier ObservationFacts. However Hibernate 4 criteria queries do
     * not support such joins, and it also won't be added to the api because the api is deprecated. The functionality
     * is available in the replacement JPA api, but the Grails ORM does not use that. There does not seem to be an
     * easy way to convert a data model or a query from the hibernate api representation to hibernates JPA
     * representation, so for now there is no upgrade path that does not involve a lot of work or some very ugly
     * hacks. Let's hope Grails switches to the JPA api at some point, but that is probably a lot of work for them.
     *
     * If we decide to take on this work, a better alternative might be to bypass hibernate and JPA altogether for
     * queries, and build SQL directly. When streaming large datasets the CPU overhead of hibernate can become quite
     * significant, so using raw SQL avoids that.
     */
    // If we don't extend a Java object but just implement Iterator, the Groovy type checker will barf on the
    // ResultIterator constructor. (Groovy 3.1.10)
    static class ModifierResultIterator extends UnmodifiableIterator<Map<String, Object>> {

        static final List<String> primaryKey = ImmutableList.of(
                // excludes modifierCd as we want to group them
                ConceptDimension.ALIAS,
                ProviderDimension.ALIAS,
                PatientDimension.ALIAS,
                VisitDimension.ALIAS,
                StartTimeDimension.ALIAS,
                "instance"
        );

        final ProjectionMapIterator iter;
        final List<ModifierDimension> modifierDimensions;

        ModifierResultIterator(List<ModifierDimension> modifierDimensions, Map<String, Integer> aliases,
                               ScrollableResults results) {
            iter = new ProjectionMapIterator(aliases, results);
            this.modifierDimensions = modifierDimensions;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Map<String, Object> next() {
            Map<String, ProjectionMap> group = nextGroup();

            if (!group.containsKey("@")) {
                throw new IllegalStateException(
                        "Modifier observations have to be selected together with the corresponding measurement observation (modifierCd = \"@\")");
            }

            Map<String, Object> result = group.get("@").toMutable();

            for (ModifierDimension dim: modifierDimensions) {
                ProjectionMap modResult = group.get(dim.getModifierCode());
                if (modResult != null) {
                    dim.addModifierValue(result, modResult);
                }
            }

            return result;
        }

        /**
         * @return the next group (based on primary key except modifierCd) as a map from modifier code to result
         */
        private Map<String, ProjectionMap> nextGroup() {
            ProjectionMap groupLeader = iter.next();
            Map<String, ProjectionMap> group = new HashMap<>(Map.of(
                (String) groupLeader.get(ModifierDimension.modifierCodeField),
                groupLeader));
            Object[] groupKey = new Object[primaryKey.size()];
            for (int i = 0; i < primaryKey.size(); i++) {
                groupKey[i] = groupLeader.get(primaryKey.get(i));
            }

            while (iter.hasNext() && belongsToCurrentGroup(groupKey, iter.peek())) {
                ProjectionMap next = iter.next();
                group.put((String)next.get(ModifierDimension.modifierCodeField), next);
            }

            return group;
        }

        private boolean belongsToCurrentGroup(Object[] groupKey, ProjectionMap result) {
            for (int i = 0; i < primaryKey.size(); i++) {
                if (groupKey[i] != result.get(primaryKey.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    static class ProjectionMapIterator extends AbstractIterator<ProjectionMap> {
        final Map<String, Integer> aliases;
        final ScrollableResults results;

        ProjectionMapIterator(Map<String, Integer> aliases, ScrollableResults results) {
            this.aliases = aliases;
            this.results = results;
        }

        public ProjectionMap computeNext() {
            if (!results.next()) {
                return super.endOfData();
            }
            return new ProjectionMap(aliases, results.get());
        }
    }

}
