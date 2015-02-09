package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.CTypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#eq(String, Object)}
 */
@Immutable
final class EqFilter implements CFilter {
    private final String columnName;
    private final Object value;

    EqFilter(@Nonnull final String key, final Object value) {
        this.columnName = key;
        this.value = value;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Clause apply(final CTypeConverter<?, ?> converter) {
        return QueryBuilder.eq(columnName, converter.toCassandra(value));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final EqFilter eqFilter = (EqFilter) o;
        return columnName.equals(eqFilter.columnName)
               && ((value == null) ? (eqFilter.value == null) : value.equals(eqFilter.value));
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = (31 * result) + ((value != null) ? value.hashCode() : 0);
        return result;
    }
}
