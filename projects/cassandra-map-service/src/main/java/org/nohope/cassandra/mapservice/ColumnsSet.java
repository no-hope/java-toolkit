package org.nohope.cassandra.mapservice;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.*;

/**
 */
@Immutable
final class ColumnsSet implements Iterable<String> {
    private final Set<String> columns = new LinkedHashSet<>();

    ColumnsSet(@Nonnull final String... columns) {
        this.columns.addAll(Lists.newArrayList(columns));
    }

    ColumnsSet(@Nonnull final Collection<String> newColumns) {
        this.columns.addAll(newColumns);
    }

    ColumnsSet(@Nonnull final ColumnsSet other) {
        this.columns.addAll(other.columns);
    }

    static ColumnsSet of(@Nonnull final String... columns) {
        return new ColumnsSet(columns);
    }

    public Set<String> getColumns() {
        return Collections.unmodifiableSet(columns);
    }

    public ColumnsSet with(@Nonnull final String newColumn) {
        final Collection<String> newColumns = new LinkedHashSet<>(columns);
        newColumns.add(newColumn);
        return new ColumnsSet(newColumns);
    }

    public ColumnsSet without(@Nonnull final String column) {
        final Collection<String> newColumns = new LinkedHashSet<>(columns);
        newColumns.remove(column);
        return new ColumnsSet(newColumns);
    }

    public boolean contains(final String column) {
        return columns.contains(column);
    }

    public ColumnsSet withAll(@Nonnull final ColumnsSet anotherColumnCollection) {
        final Collection<String> newColumns = new LinkedHashSet<>(columns);
        newColumns.addAll(anotherColumnCollection.getColumns());
        return new ColumnsSet(newColumns);
    }

    @Override
    public Iterator<String> iterator() {
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

        ColumnsSet strings = (ColumnsSet) o;
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

