package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-02-11 16:54
 */
public final class BindUtils {
    private BindUtils() {
    }

    public static <V> Object maybeBindable(final Value<V> value) {
        return value.getColumn().asCassandraValue(value);
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

        final DataType tableType = meta.getType(name);
        final DataType valueType = value.getColumn().getCassandraType().getDataType();

        if (!typesEquals(tableType, valueType)) {
            throw new IllegalArgumentException(MessageFormat.format("Unexpected types: {0} != {1}", tableType, valueType));
        }

        final Object converted = value.asCassandraValue();
        statement.setBytesUnsafe(name, tableType.serialize(converted, ProtocolVersion.NEWEST_SUPPORTED));
    }

    private static boolean typesEquals(DataType t1, DataType t2) {
        if (t1.isCollection() && t2.isCollection()) {
            final List<DataType> t1Args = t1.getTypeArguments();
            final List<DataType> t2Args = t2.getTypeArguments();
            if (t1Args.size() == t2Args.size()) {
                boolean eq = true;
                for (int i = 0; i < t1Args.size(); i++) {
                    eq &= typesEquals(t1Args.get(i), t2Args.get(i));
                }
                return eq;
            }
        }

        final Class<?> jTableCls = t1.asJavaClass();
        final Class<?> jColumnCls = t2.asJavaClass();
        return Objects.equals(jTableCls, jColumnCls);
    }
}
