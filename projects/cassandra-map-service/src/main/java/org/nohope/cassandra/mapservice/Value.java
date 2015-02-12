package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.columns.CColumn;

import java.util.Objects;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-02-12 14:26
 */
public final class Value<T> {
    private final CColumn<T, ?> column;
    private final Object value;
    private final Type type;

    private Value(final Type type, final CColumn<T, ?> column, final Object value) {
        this.column = column;
        this.value = value;
        this.type = type;
    }

    public static<T> Value<T> bound(final CColumn<T, ?> column, final T value) {
        return new Value<>(Type.BOUND, column, value);
    }

    public static<T> Value<T> unbound(final CColumn<T, ?> column) {
        return new Value<>(Type.UNBOUND, column, QueryBuilder.bindMarker(column.getName()));
    }

    public static<T> Value<T> fcall(final CColumn<T, ?> column, final String function, final Object... parameters) {
        return new Value<>(Type.FCALL, column, QueryBuilder.fcall(function, parameters));
    }

    public CColumn<T, ?> getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        final Value<?> that = (Value<?>) o;
        return column.equals(that.column)
            && (type == that.type)
            && Objects.deepEquals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = column.hashCode();
        result = (31 * result) + value.hashCode();
        result = (31 * result) + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Value{" +
               "column=" + column +
               ", value=" + value +
               ", type=" + type +
               '}';
    }

    enum Type {
        BOUND, UNBOUND, FCALL
    }
}
