package nl.thehyve.hypercubeapi.query.hypercube;

import com.google.common.collect.ImmutableList;

import java.util.*;

//@TupleConstructor(includeFields=true)  // TupleConstructor generates invalid bytecode, sometimes. Started happening
// after converting IndexedArraySet to java, dunno if that has anything to do with it. It includes the (hidden)
// metaClass field in the constructor args. Maybe something to do with the order in which some transformations run?
public class ProjectionMap extends AbstractMap<String, Object> {
    // @Delegate(includes="size, containsKey, keySet") can't delegate since these methods are already implemented on AbstractMap :(
    private final Map<String, Integer> mapping;
    private final Object[] tuple;

    ProjectionMap(Map<String, Integer> _mapping, Object[] _tuple) {
        mapping = _mapping;
        tuple = _tuple;
    }

    @Override
    public Object get(Object key) {
        Integer idx = mapping.get((String) key);
        if (idx == null) {
            return null;
        }
        //throw new IllegalArgumentException("key '$key' not present in this result row: $this")
        return tuple[idx];
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new Entries();
    }

    @Override
    public int size() {
        return mapping.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return mapping.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return mapping.keySet();
    }

    @Override
    public List<Object> values() {
        return ImmutableList.copyOf(tuple);
    }

    HashMap<String, Object> toMutable() {
        HashMap<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Integer> entry: mapping.entrySet()) {
            map.put(entry.getKey(), tuple[entry.getValue()]);
        }
        return map;
    }

    class Entries extends AbstractSet<Map.Entry<String, Object>> {
        Set<Map.Entry<String, Integer>> mappingEntries = mapping.entrySet();

        @Override
        public int size() {
            return mappingEntries.size();
        }

        @Override
       public boolean contains(Object target) {
            if (!(target instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry) target;
            if (entry.getValue() == null) {
                return false;
            }
            return ProjectionMap.this.get(entry.getKey()) == entry.getValue();
        }

        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {
            return new Iterator<>() {
                private Iterator<Entry<String, Integer>> mappingIterator = mappingEntries.iterator();

                @Override
                public boolean hasNext() {
                    return mappingIterator.hasNext();
                }

                @Override
                public Map.Entry<String, Object> next() {
                    Map.Entry<String, Integer> mappingEntry = mappingIterator.next();
                    return new AbstractMap.SimpleImmutableEntry<>(mappingEntry.getKey(), tuple[mappingEntry.getValue()]);
                }
            };
        }
    }

}
