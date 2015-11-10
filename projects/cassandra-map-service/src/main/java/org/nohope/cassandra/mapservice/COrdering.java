package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.Ordering;
import org.nohope.cassandra.mapservice.columns.CColumn;

/**
 */
public final class COrdering {
    private final CColumn<?, ?> column;
    private final Orderings asc;

    public COrdering(final CColumn<?, ?> column,
                     final Orderings ordering) {
        this.column = column;
        this.asc = ordering;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        final COrdering cOrdering = (COrdering) o;
        return (asc == cOrdering.asc)
               && column.equals(cOrdering.column);
    }

    @Override
    public int hashCode() {
        int result = column.hashCode();
        result = (31 * result) + asc.hashCode();
        return result;
    }

    public Ordering ordering() {
        return asc.forColumn(column);
    }

    CColumn<?, ?> getColumn() {
        return column;
    }
}
