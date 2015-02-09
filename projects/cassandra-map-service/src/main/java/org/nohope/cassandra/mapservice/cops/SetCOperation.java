package org.nohope.cassandra.mapservice.cops;

import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.CTypeConverter;

/**
 * Set operation
 */
public final class SetCOperation implements Operation {
    private final String columnName;
    private final Object value;

    public SetCOperation(final String columnName, final Object value) {
        this.columnName = columnName;
        this.value = value;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Assignment apply(final CTypeConverter<?, ?> converter) {
        return QueryBuilder.set(columnName, converter.toCassandra(value));
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
