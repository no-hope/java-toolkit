package org.nohope.cassandra.mapservice.cops;

import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.ctypes.Converter;

import javax.annotation.Nonnull;

/**
 * Update operation
 */
public final class CounterUpdate implements Operation<Long> {
    private final String columnName;
    private final long value;

    public CounterUpdate(@Nonnull final String columnName, final long value) {
        this.columnName = columnName;
        this.value = value;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Assignment apply(@Nonnull final Converter<?, Long> converter) {
        return QueryBuilder.incr(columnName, (Long) converter.asCassandraValue(value));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final CounterUpdate that = (CounterUpdate) o;
        return (value == that.value)
               && columnName.equals(that.columnName);
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = (31 * result) + (int) (value ^ (value >>> 32));
        return result;
    }
}
