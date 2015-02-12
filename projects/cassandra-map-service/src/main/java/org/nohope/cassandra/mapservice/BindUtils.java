package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import org.nohope.cassandra.mapservice.ctypes.Converter;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-02-11 16:54
 */
public final class BindUtils {
    private BindUtils() {
    }

    public static <V> Object maybeBindable(final Value<V> value) {
        if (value.getType() != Value.Type.BOUND) {
            return value.getValue();
        }
        final Converter converter = value.getColumn().getConverter();
        return converter.asCassandraValue(value.getValue());
    }

    public static void bind(final BoundStatement statement,
                            final TableScheme scheme,
                            final ColumnDefinitions meta,
                            final Value<?> value) {
        final String name = value.getColumn().getName();
        if (statement.isSet(name)) {
            throw new IllegalStateException(); // FIXME: descriptive
        }

        if (value.getType() != Value.Type.BOUND) {
            throw new IllegalStateException(); // FIXME: descriptive
        }

        final Converter converter = scheme.getColumns().get(name).getConverter();
        final DataType columnDataType = converter.getCassandraType().getDataType();
        final DataType tableType = meta.getType(name);
        if (!columnDataType.equals(tableType)) {
            throw new IllegalArgumentException(); // FIXME: descriptive
        }

        final Object converted = converter.asCassandraValue(value.getValue()); // FIXME: descriptive
        statement.setBytesUnsafe(name, columnDataType.serialize(converted, ProtocolVersion.NEWEST_SUPPORTED));
    }
}
