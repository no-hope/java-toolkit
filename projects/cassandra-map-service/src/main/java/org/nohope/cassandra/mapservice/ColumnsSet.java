package org.nohope.cassandra.mapservice;

import com.google.common.collect.Lists;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.*;

/**
 */
@Immutable
@Deprecated
final class ColumnsSet implements Iterable<CColumn<?, ?>> {
    private final Set<CColumn<?, ?>> columns = new LinkedHashSet<>();

    ColumnsSet(@Nonnull final CColumn<?, ?>... columns) {
        this.columns.addAll(Lists.newArrayList(columns));
    }

    ColumnsSet(@Nonnull final Collection<CColumn<?, ?>> newColumns) {
        this.columns.addAll(newColumns);
    }

    ColumnsSet(@Nonnull final ColumnsSet other) {
        this.columns.addAll(other.columns);
    }

    static ColumnsSet of(@Nonnull final CColumn<?, ?>... columns) {
        return new ColumnsSet(columns);
    }

    public Set<CColumn<?, ?>> getColumns() {
        return Collections.unmodifiableSet(columns);
    }

    public ColumnsSet with(@Nonnull final CColumn<?, ?> newColumn) {
        final Collection<CColumn<?, ?>> newColumns = new LinkedHashSet<>(columns);
        newColumns.add(newColumn);
        return new ColumnsSet(newColumns);
    }

    public ColumnsSet without(@Nonnull final CColumn<?, ?> column) {
        final Collection<CColumn<?, ?>> newColumns = new LinkedHashSet<>(columns);
        newColumns.remove(column);
        return new ColumnsSet(newColumns);
    }

    public ColumnsSet withAll(@Nonnull final ColumnsSet anotherColumnCollection) {
        final Collection<CColumn<?, ?>> newColumns = new LinkedHashSet<>(columns);
        newColumns.addAll(anotherColumnCollection.getColumns());
        return new ColumnsSet(newColumns);
    }

    @Override
    public Iterator<CColumn<?, ?>> iterator() {
        return columns.iterator();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final ColumnsSet strings = (ColumnsSet) o;
        return columns.equals(strings.columns);
    }

    @Override
    public int hashCode() {
        return columns.hashCode();
    }

    @Override
    public String toString() {
        return "ColumnsSet{" +
               "columns=" + columns +
               '}';
    }
}

