package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.text.MessageFormat;
import java.util.*;

/**
 * Cassandra table scheme for CMap.  <br>
 * Use {@link org.nohope.cassandra.mapservice.CMapBuilder map builder} for creating one.
 */
@Immutable
public final class TableScheme {
    private static final String PRIMARY_KEY_TEMPLATE = "%s, "; // FIXME: Joiner.on(..)
    private final String tableName;
    private final String tableDescription;
    private final Map<String, CColumn<?, ?>> columns = new LinkedHashMap<>();
    private final Set<CColumn<?, ?>> columnSet = new LinkedHashSet<>();

    private final Collection<CColumn<?, ?>> partitionKeys = new LinkedHashSet<>();
    private final Collection<CColumn<?, ?>> clusteringKeys = new LinkedHashSet<>();
    private final Collection<CColumn<?, ?>> staticKeys = new LinkedHashSet<>();

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

    private static int getLastIndexOfSubstringWithoutLastCommaAndSpace(final int stringLength) {
        return stringLength - 2;
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
        return Sets.newHashSet(Collections2.transform(getColumnsSet(), new Function<CColumn<?, ?>, String>() {
            @Override
            public String apply(final CColumn<?, ?> input) {
                return input.getName();
            }
        }));
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
                "{1}" +
                "PRIMARY KEY (({2}){3}));"
                , tableName
                , formColumns()
                , formPartitionKeys()
                , formClusteringKeys()
        );
    }

    private String formColumns() {
        final StringBuilder columnsString = new StringBuilder();
        for (final CColumn<?, ?> column : columns.values()) {
            columnsString.append(column.getColumnTemplate());
            if (staticKeys.contains(column)) {
                columnsString.append(" STATIC");
            }
            columnsString.append(", ");
        }
        return columnsString.toString();
    }

    private String formPartitionKeys() {
        final StringBuilder primariesString = new StringBuilder();
        for (final CColumn<?, ?> key : partitionKeys) {
            primariesString.append(String.format(PRIMARY_KEY_TEMPLATE, key.getName()));
        }
        return primariesString.substring(0, getLastIndexOfSubstringWithoutLastCommaAndSpace(primariesString.length()));
    }

    private String formClusteringKeys() {
        final String clusteringKeysString = StringUtils.join(Collections2.transform(clusteringKeys, new Function<CColumn<?,?>, String>() {
            @Nullable
            @Override
            public String apply(final CColumn<?, ?> input) {
                return input.getName();
            }
        }), ", ");

        if (!clusteringKeysString.isEmpty()) {
            return StringUtils.join(", ", clusteringKeysString);
        }
        return clusteringKeysString;
    }
}
