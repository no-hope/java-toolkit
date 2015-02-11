package org.nohope.cassandra.mapservice;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.nohope.cassandra.mapservice.columns.CColumn;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Builders for CMapSync {@link org.nohope.cassandra.mapservice.TableScheme table scheme}.
 * <p/>
 * <b>Example:</b>
 * <p/>
 * Let's say we need to create a cassandra table with such description:
 * <pre>
 * CREATE TABLE IF NOT EXISTS "Owls" (
 *     Species text,
 *     LastTimeSeen timestamp,
 *     LastSeenPlace ascii,
 *     PRIMARY KEY ((Species), LastTimeSeen)
 * );
 * </pre>
 * <p/>
 * So we have one primary key, one clustering key and one grouping key.
 * <p/>
 * <p/>
 * <pre>
 * TableScheme scheme =
 *     new CMapBuilder("Owls").addColumn("Species",  TrivialType.TEXT)
 *                            .addColumn("LastTimeSeen", TrivialType.TIMESTAMP)
 *                            .addColumn("LastSeenPlace", TrivialType.ASCII)
 *                        .end()
 *                            .setPartition("Species")
 *                            .setClustering("LastTimeSeen")
 *                        .end()
 *                        .buildScheme()
 * </pre>
 * <p/>
 * <p/>
 * Now we have a  TableScheme, that's an immutable representation of table structure: columns, keys, column types.
 * Next step is to use {@link org.nohope.cassandra.mapservice.CMapServiceFactory service factory}.
 */
public final class CMapBuilder {
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("\\A[a-zA-Z][\\w|_]+");

    private final Map<String, CColumn<?, ?>> columns = new LinkedHashMap<>();
    private final Set<String> partitionKeys = new LinkedHashSet<>();
    private final Set<String> clusteringKeys = new LinkedHashSet<>();
    private final Set<String> staticKeys = new LinkedHashSet<>();

    private final String tableName;

    public CMapBuilder(@Nonnull final String tableID) {
        if (tableNameContainsForbiddenSymbols(tableID)) {
            throw new TableSchemeException(tableID + " is not correct name for table.");
        }
        this.tableName = tableID;
    }

    private static boolean tableNameContainsForbiddenSymbols(@Nonnull final CharSequence tableID) {
        return !TABLE_NAME_PATTERN.matcher(tableID).matches();
    }

    public ColumnsBuilder addColumn(final CColumn<?, ?> column) {
        return new ColumnsBuilder(column);
    }

    public ColumnsBuilder addColumns(final CColumn<?, ?>... columns) {
        return new ColumnsBuilder(columns);
    }

    private void verifyColumnDefinitions(final Collection<String> keys) {
        final Set<String> prohibitedNames = columns.keySet();
        final String message = "The following columns was not defined: ";
        verifyColumnsAreNotProhibited(keys, prohibitedNames, message, true);
    }

    private void verifyColumnsNotPartition(final Collection<String> keys) {
        verifyColumnsAreNotProhibited(keys, partitionKeys, "The following columns is already defined as partition: ", false);
    }

    private void verifyColumnsNotClustering(final Collection<String> keys) {
        verifyColumnsAreNotProhibited(keys, clusteringKeys, "The following columns is already defined as clustering: ", false);
    }

    private static void verifyColumnsAreNotProhibited(final Collection<String> keys,
                                                      final Set<String> prohibitedNames,
                                                      final String message,
                                                      final boolean negate) {
        final Sets.SetView<String> diff = Sets.difference(new HashSet<>(keys), prohibitedNames);
        if ((negate && !diff.isEmpty()) || (!negate && diff.isEmpty())) {
            throw new TableSchemeException(message + StringUtils.join(diff.toArray(), "; "));
        }
    }

    public final class ColumnsBuilder {

        private ColumnsBuilder(@Nonnull final CColumn<?, ?> column) {
            columns.put(column.getName(), column);
        }

        private ColumnsBuilder(@Nonnull final CColumn<?, ?>[] columns) {
            addColumns(columns);
        }

        public ColumnsBuilder addColumn(@Nonnull final CColumn<?, ?> column) {
            final String name = column.getName();
            if (!columns.containsKey(name)) {
                columns.put(column.getName(), column);
            }
            return this;
        }

        public ColumnsBuilder addColumns(@Nonnull final CColumn<?, ?>... columns) {
            for (final CColumn<?, ?> column : columns) {
                addColumn(column);
            }

            return this;
        }

        public PartitionBuilder end() {
            return new PartitionBuilder();
        }
    }

    public final class PartitionBuilder {
        public ClusteringBuilder setPartition(@Nonnull final CColumn<?, ?>... columns) {
            for (final CColumn<?, ?> column : columns) {
                if (column.getConverter().getCassandraType().getDataType().isCollection()) {
                    throw new TableSchemeException("Collection type can't be partion column: " + column.getName());
                }
            }
            final String[] columnNames = getNames(columns);
            return setPartitionColumnsNames(columnNames);
        }

        private ClusteringBuilder setPartitionColumnsNames(@Nonnull final String... partitionColumns) {
            partitionKeys.addAll(Arrays.asList(partitionColumns));
            verifyColumnDefinitions(partitionKeys);
            return new ClusteringBuilder();
        }
    }

    public final class ClusteringBuilder {
        public StaticBuilder setClustering(final CColumn<?, ?>... columns) {
            for (final CColumn<?, ?> column : columns) {
                if (column.getConverter().getCassandraType().getDataType().isCollection()) {
                    throw new TableSchemeException("Collection type can't be clustering column: " + column.getName());
                }
            }
            final String[] columnNames = getNames(columns);
            return setClusteringColumnsNames(columnNames);
        }

        public StaticBuilder withoutClustering() {
            return new StaticBuilder();
        }

        public TableScheme buildScheme() throws TableSchemeException {
            return new TableScheme(tableName, columns, partitionKeys, clusteringKeys, staticKeys);
        }

        private StaticBuilder setClusteringColumnsNames(@Nonnull final String... columns) {
            clusteringKeys.addAll(Arrays.asList(columns));
            verifyColumnDefinitions(clusteringKeys);
            verifyColumnsNotPartition(clusteringKeys);

            return new StaticBuilder();
        }
    }

    public final class StaticBuilder {
        public SchemeBuilder setStatic(final CColumn<?, ?>... columns) {
            for (final CColumn<?, ?> column : columns) {
                if (column.getConverter().getCassandraType().getDataType().isCollection()) {
                    throw new TableSchemeException("Collection type can't be static column: " + column.getName());
                }
            }
            final String[] columnNames = getNames(columns);
            return setStaticColumnsNames(columnNames);
        }

        public SchemeBuilder withoutStatic() {
            return new SchemeBuilder();
        }

        public TableScheme buildScheme() throws TableSchemeException {
            return new TableScheme(tableName, columns, partitionKeys, clusteringKeys, staticKeys);
        }

        private SchemeBuilder setStaticColumnsNames(@Nonnull final String... columns) {
            staticKeys.addAll(Arrays.asList(columns));
            verifyColumnDefinitions(staticKeys);
            verifyColumnsNotPartition(staticKeys);
            verifyColumnsNotClustering(staticKeys);

            return new SchemeBuilder();
        }
    }

    static String[] getNames(final CColumn<?, ?>[] columns) {
        final List<String> ret = new ArrayList<>();
        for (final CColumn<?, ?> column : columns) {
            ret.add(column.getName());
        }

        return ret.toArray(new String[ret.size()]);
    }

    public class SchemeBuilder {
        public TableScheme buildScheme() throws TableSchemeException {
            return new TableScheme(tableName, columns, partitionKeys, clusteringKeys, staticKeys);
        }
    }
}
