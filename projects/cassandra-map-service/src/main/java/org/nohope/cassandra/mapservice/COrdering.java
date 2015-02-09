package org.nohope.cassandra.mapservice;

/**
 */
public final class COrdering {
    private final String columnName;
    private final boolean asc;

    public COrdering(final String columnName, final Orderings ordering) {
        this.columnName = columnName;
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
               && columnName.equals(cOrdering.columnName);
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = (31 * result) + (asc ? 1 : 0);
        return result;
    }

    String getColumnName() {
        return columnName;
    }

    boolean isDesc() {
        return !asc;
    }
}
