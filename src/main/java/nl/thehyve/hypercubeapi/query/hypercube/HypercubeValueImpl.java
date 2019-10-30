package nl.thehyve.hypercubeapi.query.hypercube;

import nl.thehyve.hypercubeapi.query.dimension.Dimension;

import java.util.List;

public class HypercubeValueImpl implements HypercubeValue {
    // Not all dimensions apply to all values, and the set of dimensions is extensible using modifiers.
    // We can either use a Map or methodMissing().
    private final HypercubeImpl cube;
    // dimension
    private final Object[] dimensionElementIdxes;
    private final Object value;

    HypercubeValueImpl(HypercubeImpl cube, Object[] dimensionElementIdxes, Object value) {
        this.cube = cube;
        this.dimensionElementIdxes = dimensionElementIdxes;
        this.value = value;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    public Object getAt(Dimension dim) {
        return getDimElement(dim);
    }

    Object getDimElement(Dimension dim) {
        cube.checkDimension(dim);
        Object dimensionElementIdx = dimensionElementIdxes[cube.getDimensionsIndex(dim)];
        if (dimensionElementIdx == null) {
            return null;
        }
        if (dim.getDensity().isDense()) {
            return cube.dimensionElement(dim, (Integer) dimensionElementIdx);
        } else {
            return dim.resolveElement(dimensionElementIdx);
        }
    }

    public Integer getDimElementIndex(Dimension dim) {
        cube.checkDimension(dim);
        cube.checkIsDense(dim);
        return (Integer) dimensionElementIdxes[cube.getDimensionsIndex(dim)];
    }

    public Object getDimKey(Dimension dim) {
        cube.checkDimension(dim);
        Object dimensionElementIdx = dimensionElementIdxes[cube.getDimensionsIndex(dim)];
        if (dimensionElementIdx == null) {
            return null;
        }
        if (dim.getDensity().isDense()) {
            return cube.dimensionElementKey(dim, (Integer) dimensionElementIdx);
        } else {
            return dimensionElementIdx;
        }
    }

    public List<Dimension> getAvailableDimensions() {
        return cube.getDimensions();
    }

}
