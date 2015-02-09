package org.nohope.cassandra.mapservice;

import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tuple of row values
 * <b>Example:</b>
 * <pre>
 * {@link org.nohope.cassandra.mapservice.CMapSync CMapSync} map = service.getMap("mapToPut") // service as a {@link org.nohope.cassandra.mapservice.CMapService CMapService}
 * </pre>
 * <p/>
 * <ol>
 * <li>For using with put wrap ValueTuple with {@link org.nohope.cassandra.mapservice.CPutQuery special put query}
 * <pre>
 * {@link org.nohope.cassandra.mapservice.ValueTuple ValueTuple} valueToPut =
 *     ValueTuple.of(COL_QUOTE, newQuote())
 *               .with(COL_TIMESTAMP, new Date())
 *               .with(COL_QUOTE_UUID, UUID.randomUUID())
 *
 * map.put(new CPutQuery(valueToPut))
 *     </pre>
 * </li>
 * <li>For get using
 * <pre>
 * {@link org.nohope.cassandra.mapservice.CQuery CQuery} query =
 *     {@link org.nohope.cassandra.mapservice.CQueryBuilder CQueryBuilder}.createQuery()
 *                  .of(COL_QUOTE, COL_TIMESTAMP, COL_QUOTE_UUID)
 *                  .end();
 *
 * {@link org.nohope.cassandra.mapservice.ValueTuple ValueTuple} valueToGet = map.getOne(query);
 *     </pre>
 * </li>
 * </ol>
 */
@Immutable
public final class ValueTuple {
    private final Map<String, Object> columns = new HashMap<>();

    ValueTuple(Map<String, Object> mapToPut) {
        columns.putAll(mapToPut);
    }

    public static ValueTuple of(@Nonnull String keyName, @Nullable Object value) {
        return new ValueTuple(Collections.singletonMap(keyName, value));
    }

    public static <T> ValueTuple of(@Nonnull CColumn<T, ?> column, @Nullable T value) {
        return new ValueTuple(Collections.singletonMap(column.getName(), (Object) value));
    }

    public ValueTuple with(@Nonnull String key, @Nullable Object value) {
        Map<String, Object> newMap = new HashMap<>(columns);
        newMap.put(key, value);
        return new ValueTuple(newMap);
    }

    public <T> ValueTuple with(@Nonnull CColumn<T, ?> column, @Nullable T value) {
        Map<String, Object> newMap = new HashMap<>(columns);
        newMap.put(column.getName(), value);
        return new ValueTuple(newMap);
    }

    public ValueTuple with(@Nonnull ValueTuple tuple) {
        Map<String, Object> newMap = new HashMap<>(columns);
        newMap.putAll(tuple.columns);
        return new ValueTuple(newMap);
    }

    /**
     * Typed get if you know what to get.
     *
     * @param <T>  the type parameter
     * @param name the name
     * @return the t
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        if (columns.containsKey(name)) {
            return (T) columns.get(name);
        }
        throw new IllegalArgumentException(
                MessageFormat.format("No such column as {0}. Has columns: {1}",
                        name, columns)
        );
    }

    @SuppressWarnings("unchecked")
    public <V> V get(CColumn<V, ?> column) {
        return get(column.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        ValueTuple that = (ValueTuple) o;
        return columns.equals(that.columns);
    }

    @Override
    public String toString() {
        return "ValueTuple{" +
               "columns=" + columns +
               '}';
    }

    @Override
    public int hashCode() {
        return columns.hashCode();
    }

    Map<String, Object> getColumns() {
        return Collections.unmodifiableMap(columns);
    }
}
