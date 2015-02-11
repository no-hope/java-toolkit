package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
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
    private final Set<String> columnSet = new LinkedHashSet<>();

    private final Collection<String> partitionKeys = new LinkedHashSet<>();
    private final Collection<String> clusteringKeys = new LinkedHashSet<>();
    private final Collection<String> staticKeys = new LinkedHashSet<>();

    public TableScheme(
            @Nonnull final String name
            , @Nonnull final Map<String, CColumn<?, ?>> columns
            , @Nonnull final Collection<String> partitionKeys
            , @Nonnull final Collection<String> clusteringKeys
            , @Nonnull final Collection<String> staticKeys) {
        this.tableName = name;
        this.partitionKeys.addAll(partitionKeys);
        this.columns.putAll(columns);
        this.columnSet.addAll(columns.keySet());
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

    public boolean isClusteringKey(final String columnName) {
        return clusteringKeys.contains(columnName);
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

    Set<String> getColumnsSet() {
        return Collections.unmodifiableSet(columnSet);
    }

    Collection<String> getPartitionKeys() {
        return Collections.unmodifiableCollection(partitionKeys);
    }

    boolean containsColumn(final String column) {
        return columnSet.contains(column);
    }

    boolean isPartitionKey(final String key) {
        return partitionKeys.contains(key);
    }

    Collection<String> getClusteringKeys() {
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
            if (staticKeys.contains(column.getName())) {
                columnsString.append(" STATIC");
            }
            columnsString.append(", ");
        }
        return columnsString.toString();
    }

    private String formPartitionKeys() {
        final StringBuilder primariesString = new StringBuilder();
        for (final String key : partitionKeys) {
            primariesString.append(String.format(PRIMARY_KEY_TEMPLATE, key));
        }
        return primariesString.substring(0, getLastIndexOfSubstringWithoutLastCommaAndSpace(primariesString.length()));
    }

    private String formClusteringKeys() {
        final String clusteringKeysString = StringUtils.join(clusteringKeys, ", ");
        if (!clusteringKeysString.isEmpty()) {
            return StringUtils.join(", ", clusteringKeysString);
        }
        return clusteringKeysString;
    }

    /*
    public boolean containsCollectionColumns() {
        for (final CColumn<?, ?> column : columns.values()) {
            if (column instanceof CCollection) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getNonCollectionColumns() {
        final Collection<String> nonCollectionColumnsSet = new LinkedHashSet<>();
        for (final CColumn<?, ?> column : columns.values()) {
            if (!(column instanceof CCollection)) {
                nonCollectionColumnsSet.add(column.getName());
            }
        }
        return Sets.newHashSet(nonCollectionColumnsSet);
    }*/
}
