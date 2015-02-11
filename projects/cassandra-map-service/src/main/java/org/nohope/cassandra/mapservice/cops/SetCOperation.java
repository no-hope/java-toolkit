package org.nohope.cassandra.mapservice.cops;

import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.ctypes.Converter;

/**
 * Set operation
 */
public final class SetCOperation<V> implements Operation<V> {
    private final String columnName;
    private final V value;

    public SetCOperation(final String columnName, final V value) {
        this.columnName = columnName;
        this.value = value;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Assignment apply(final Converter<?, V> converter) {
        return QueryBuilder.set(columnName, converter.asCassandraValue(value));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final SetCOperation that = (SetCOperation) o;
        return value.equals(that.value)
               && columnName.equals(that.columnName);
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = (31 * result) + columnName.hashCode();
        return result;
    }
}
