package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.querybuilder.BindMarker;
import org.nohope.cassandra.mapservice.ctypes.Converter;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-02-11 16:54
 */
public final class BindUtils {
    private BindUtils() {
    }

    public static <V> Object maybeBindable(final Converter<?, V> converter, final V value) {
        if (value instanceof BindMarker) {
            return value;
        }
        return converter.asCassandraValue(value);
    }

    public static void bind(final BoundStatement statement,
                            final TableScheme scheme,
                            final ColumnDefinitions meta,
                            final String name,
                            final Object value) {
        if (statement.isSet(name)) {
            throw new IllegalStateException(); // FIXME: descriptive
        }

        final Converter converter = scheme.getColumns().get(name).getConverter();
        final DataType columnDataType = converter.getCassandraType().getDataType();
        final DataType tableType = meta.getType(name);
        if (!columnDataType.equals(tableType)) {
            throw new IllegalArgumentException();
        }

        final Object converted = converter.asCassandraValue(value); // FIXME: descriptive
        statement.setBytesUnsafe(name, columnDataType.serialize(converted, ProtocolVersion.NEWEST_SUPPORTED));

    }
}
