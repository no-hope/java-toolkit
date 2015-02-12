package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cassandra table scheme for CMap.  <br>
 * Use {@link org.nohope.cassandra.mapservice.CMapBuilder map builder} for creating one.
 */
@Immutable
public final class TableScheme {
    private final String tableName;
    private final String tableDescription;

    private final Map<String, CColumn<?, ?>> columns = new LinkedHashMap<>();
    private final Set<CColumn<?, ?>> columnSet = new LinkedHashSet<>();
    private final Set<CColumn<?, ?>> partitionKeys = new LinkedHashSet<>();
    private final Set<CColumn<?, ?>> clusteringKeys = new LinkedHashSet<>();
    private final Set<CColumn<?, ?>> staticKeys = new LinkedHashSet<>();

    public TableScheme(
            @Nonnull final String name
            , @Nonnull final Map<String, CColumn<?, ?>> columns
            , @Nonnull final Collection<CColumn<?, ?>> partitionKeys
            , @Nonnull final Collection<CColumn<?, ?>> clusteringKeys
            , @Nonnull final Collection<CColumn<?, ?>> staticKeys) {
        this.tableName = name;
        this.partitionKeys.addAll(partitionKeys);
        this.columns.putAll(columns);
        this.columnSet.addAll(columns.values());
        this.clusteringKeys.addAll(clusteringKeys);
        this.staticKeys.addAll(staticKeys);
        this.tableDescription = buildTableDescription();
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableDescription() {
        return tableDescription;
    }

    public boolean isClusteringKey(final CColumn<?, ?> column) {
        return clusteringKeys.contains(column);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        final TableScheme that = (TableScheme) o;
        return clusteringKeys.equals(that.clusteringKeys)
               && columnSet.equals(that.columnSet)
               && columns.equals(that.columns)
               && partitionKeys.equals(that.partitionKeys)
               && tableDescription.equals(that.tableDescription)
               && tableName.equals(that.tableName);
    }

    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = (31 * result) + tableDescription.hashCode();
        result = (31 * result) + columns.hashCode();
        result = (31 * result) + columnSet.hashCode();
        result = (31 * result) + partitionKeys.hashCode();
        result = (31 * result) + clusteringKeys.hashCode();
        return result;
    }

    String getTableNameQuoted() {
        return QueryBuilder.quote(tableName);
    }

    Map<String, CColumn<?, ?>> getColumns() {
        return Collections.unmodifiableMap(columns);
    }

    Set<CColumn<?, ?>> getColumnsSet() {
        return Collections.unmodifiableSet(columnSet);
    }

    Set<String> getColumnNames() {
        return Sets.newHashSet(Collections2.transform(getColumnsSet(), CColumn::getName));
    }

    Collection<CColumn<?, ?>> getPartitionKeys() {
        return Collections.unmodifiableCollection(partitionKeys);
    }

    boolean containsColumn(final CColumn<?, ?> column) {
        return columnSet.contains(column);
    }

    boolean isPartitionKey(final CColumn<?, ?> key) {
        return partitionKeys.contains(key);
    }

    Collection<CColumn<?, ?>> getClusteringKeys() {
        return Collections.unmodifiableCollection(clusteringKeys);
    }

    String buildTableDescription() {
        return MessageFormat.format(
                "CREATE TABLE IF NOT EXISTS \"{0}\" " +
                '(' +
                "{1}, " +
                "PRIMARY KEY (({2}){3}));"
                , tableName
                , formColumns()
                , fromColumns(partitionKeys)
                , clusteringKeys.isEmpty() ? "" : (", " + fromColumns(clusteringKeys))
        );
    }

    private String formColumns() {
        return Joiner.on(", ").join(columns.values().stream().map(c -> {
            if (staticKeys.contains(c)) {
                return c.getColumnTemplate() + " STATIC";
            }
            return c.getColumnTemplate();
        }).collect(Collectors.toList()));
    }

    private static String fromColumns(final Collection<CColumn<?, ?>> columns) {
        return Joiner.on(", ").join(columns.stream().map(CColumn::getName).collect(Collectors.toList()));
    }
}
