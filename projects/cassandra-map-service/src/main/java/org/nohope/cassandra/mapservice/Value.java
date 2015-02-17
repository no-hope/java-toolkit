package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.columns.CColumn;

import java.util.Objects;
import java.util.Optional;

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

    public Object asCassandraValue() {
        return column.asCassandraValue(this);
    }

    /*
    public static <T> Value<T[]> from(final Value<T>... values) {
        final List<Value<T>> list = Lists.newArrayList(values);
        final Set<CColumn<T, ?>> columns =
                list.stream().map(Value::getColumn).collect(Collectors.toSet());

        if (columns.size() != 1) {
            throw new IllegalStateException(); // FIXME: description
        }


        for (final Value<T> v : list) {

        }
    }*/

    public static<T> Value<T> bound(final CColumn<T, ?> column, final T value) {
        return new Value<>(Type.BOUND, column, value);
    }

    @Deprecated
    public static<T> Value<T[]> boundMany(final CColumn<T, ?> column, final T... value) {
        return new Value<>(Type.BOUND, (CColumn<T[], ?>) column, value);
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

    @SuppressWarnings("unchecked")
    public Optional<T> getBoundValue() {
        return (type == Type.BOUND)
             ? Optional.of((T) value)
             : Optional.<T> empty();
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

    public enum Type {
        BOUND, UNBOUND, FCALL
    }
}
