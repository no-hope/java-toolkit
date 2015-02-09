package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.CTypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#in(String, Object...)}
 */
@Immutable
final class InFilter implements CFilter {
    private final String columnName;
    private final List<Object> values;

    InFilter(@Nonnull final String key, @Nonnull final Object... value) {
        this.columnName = key;
        this.values = new ArrayList<>(Arrays.asList(value));
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Clause apply(final CTypeConverter<?, ?> converter) {
        return QueryBuilder.in(columnName, converter.toCassandra(values.toArray()));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final InFilter inFilter = (InFilter) o;
        return columnName.equals(inFilter.columnName) && values.equals(inFilter.values);
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = (31 * result) + values.hashCode();
        return result;
    }
}
