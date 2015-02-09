package org.nohope.cassandra.mapservice.columns.collections;

import org.nohope.cassandra.mapservice.CTypeConverter;
import org.nohope.cassandra.mapservice.columns.CCollection;
import org.nohope.cassandra.mapservice.columns.CColumn;

/**
 */
public final class SetCColumn<V, C> extends CColumn<V, C> implements CCollection<V> {

    private final String columnTemplate;

    private SetCColumn(final String name, final CTypeConverter<V, C> converter) {
        super(name, converter);
        columnTemplate = super.getName() + " set<" + super.getConverter().getCType().getType() + '>';
    }

    public static <V, C> SetCColumn<V, C> of(final String name, final CTypeConverter<V, C> converter) {
        return new SetCColumn<>(name, converter);
    }

    @Deprecated
    @Override
    public String insertToCollection(final Iterable<V> valuesToInsert) {
        final CTypeConverter<V, C> converter = super.getConverter();
        final StringBuilder builder = new StringBuilder("INSERT INTO {} {} values {}, ");
        builder.append(" {");
        for (final V value : valuesToInsert) {
            builder.append(converter.toCassandra(value));
            builder.append(',');
        }
        builder.append(" };");
        return builder.toString();
    }

    @Deprecated
    @Override
    public String updateCollectionWith(final Iterable<V> values) {
        final CTypeConverter<V, C> converter = super.getConverter();
        final StringBuilder builder = new StringBuilder("UPDATE {} set ");
        builder.append(super.getName());
        builder.append(" = ");
        builder.append(super.getName());
        builder.append(" + {");
        for (final V value : values) {
            builder.append(converter.toCassandra(value));
            builder.append(',');
        }
        builder.append("} where {} = {}"); //and и т.д.
        return builder.toString();
    }

    @Override
    public String getColumnTemplate() {
        return columnTemplate;
    }

    @Override
    public String collectionType() {
        return "set<" + super.getConverter().getCType().getType() + '>';
    }
}
