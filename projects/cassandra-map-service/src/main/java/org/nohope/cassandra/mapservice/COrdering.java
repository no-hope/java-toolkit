package org.nohope.cassandra.mapservice;

import org.nohope.cassandra.mapservice.columns.CColumn;

/**
 */
public final class COrdering {
    private final CColumn<?, ?> column;
    private final boolean asc;

    public COrdering(final CColumn<?, ?> column, final Orderings ordering) {
        this.column = column;
        this.asc = ordering.getOrdering();
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
        result = (31 * result) + (asc ? 1 : 0);
        return result;
    }

    CColumn<?, ?> getColumn() {
        return column;
    }

    boolean isDesc() {
        return !asc;
    }
}
