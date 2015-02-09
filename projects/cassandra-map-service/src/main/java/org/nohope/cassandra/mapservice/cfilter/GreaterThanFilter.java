package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.CTypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#gt(String, Object)}
 */
@Immutable
final class GreaterThanFilter implements CFilter {
    private final String columnName;
    private final Object value;

    GreaterThanFilter(@Nonnull final String key, @Nonnull final Object value) {
        this.columnName = key;
        this.value = value;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Clause apply(final CTypeConverter<?, ?> converter) {
        return QueryBuilder.gt(columnName, converter.toCassandra(value));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final GreaterThanFilter that = (GreaterThanFilter) o;
        return columnName.equals(that.columnName) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = (31 * result) + value.hashCode();
        return result;
    }
}
