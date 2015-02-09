package org.nohope.cassandra.mapservice.columns.collections;

import org.nohope.cassandra.mapservice.CTypeConverter;
import org.nohope.cassandra.mapservice.columns.CCollection;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CType;

/**
 */
public final class MapCColumn<K extends CType, V, C> extends CColumn<V, C> implements CCollection<V> {
    private final CType key;
    private final String columnTemplate;

    MapCColumn(final String name, final K key, final CTypeConverter<V, C> converter) {
        super(name, converter);
        this.key = key;
        columnTemplate = super.getName() + " map<" + key.getType() + ", " + super.getConverter().getCType().getType() + '>';
    }

    public static <K extends CType, V, C> MapCColumn<K, V, C> of(final String name,
                                                                 final K key,
                                                                 final CTypeConverter<V, C> converter) {
        return new MapCColumn<>(name, key, converter);
    }

    @Deprecated
    @Override
    public String insertToCollection(final Iterable<V> valuesToInsert) {
        return null;
    }

    @Deprecated
    @Override
    public String updateCollectionWith(final Iterable<V> valuesToInsert) {
        return null;
    }

    @Override
    public String collectionType() {
        return "map<" + key.getType() + ", " + super.getConverter().getCType().getType() + '>';
    }

    @Override
    public String getColumnTemplate() {
        return columnTemplate;
    }
}
