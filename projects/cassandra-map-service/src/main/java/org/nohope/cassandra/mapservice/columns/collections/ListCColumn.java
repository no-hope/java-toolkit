package org.nohope.cassandra.mapservice.columns.collections;

import org.nohope.cassandra.mapservice.CTypeConverter;
import org.nohope.cassandra.mapservice.columns.CCollection;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CListSerializableType;
import org.nohope.cassandra.mapservice.ctypes.CListType;
import org.nohope.cassandra.mapservice.ctypes.CType;
import org.nohope.serialization.streams.KryoProvider;

import java.io.Serializable;
import java.util.List;

/**
 */
public class ListCColumn<V, C> extends CColumn<List<V>, C> implements CCollection<V> {
    private final String columnTemplate;

    ListCColumn(final String name, final CTypeConverter<List<V>, C> converter) {
        super(name, converter);
        columnTemplate = super.getName() + " list<" + super.getConverter().getCType().getType() + '>';
    }

    public static <V, C> ListCColumn<V, C> of(final String name, final CListType<V, C> converter) {
        return new ListCColumn<>(name, converter);
    }

    public static ListCColumn<String, Object> ofText(final String name) {
        return new ListCColumn<>(name, CListType.of(String.class, CType.TEXT));
    }

    public static <T extends Serializable> ListCColumn<T, String> ofSerializable(final String name,
                                                                                 final Class<T> clazz) {
        return new ListCColumn<>(name, CListSerializableType.of(clazz, new KryoProvider()));
    }

    @Override
    public String insertToCollection(final Iterable<V> valuesToInsert) {
        final StringBuilder values = new StringBuilder("[");
        final CTypeConverter<List<V>, C> converter = super.getConverter();
        for (final V value : valuesToInsert) {
            values.append('\'');
            values.append(converter.toCassandra(value));
            values.append('\'');
            values.append(", ");
        }
        values.replace(values.length() - 2, values.length(), "]");
        return values.toString();
    }

    @Override
    public String getColumnTemplate() {
        return columnTemplate;
    }

    @Deprecated
    @Override
    public String updateCollectionWith(final Iterable<V> valuesToInsert) {
        // TODO: not implemented
        return null;
    }

    @Override
    public String collectionType() {
        return "list<" + super.getConverter().getCType().getType() + '>';
    }
}
