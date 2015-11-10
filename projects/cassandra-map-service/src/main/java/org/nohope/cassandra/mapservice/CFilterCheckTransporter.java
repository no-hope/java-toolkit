package org.nohope.cassandra.mapservice;

import com.google.common.collect.Sets;
import org.nohope.cassandra.mapservice.columns.CColumn;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
final class CFilterCheckTransporter {
    private static final String VERIFY_COLUMNS_EXCEPTION_MESSAGE = "{2}: Not all columns passed in Query.\n Were passed: {0}.\n Were missed: {1}";
    private static final String NO_SUCH_COLUMN_ERROR_MESSAGE = "No such column {0}. Has columns {1}";
    private static final String NOT_A_CLUSTERING_KEY_ERROR_MESSAGE = "Column {0} is not a clustering/partition key. Clustering keys is {1}, partition keys is {2}";
    private static final String NOT_EXISTING_FILTERED_COLUMNS_ERROR_MESSAGE = "Filtered columns {0} don't exist in current table scheme. Existed columns {1}.";
    private static final String PARTITIONED_KEYS_ERROR_MESSAGE = "Not all primary keys defined in query. Defined {0}. Primaries: {1}.";

    private CFilterCheckTransporter() {
    }

    static void verifyQueryDueToScheme(final TableScheme scheme, final CQuery cQuery) {
        if (!cQuery.getFilters().isEmpty()) {
            performFiltersCheck(scheme, cQuery);
        }

        verifyKeysDefinesOneRow(cQuery, scheme);
        performOrderingsCheck(cQuery, scheme);
    }

    private static void performFiltersCheck(final TableScheme scheme, final CQuery cQuery) {
        final Set<CColumn<?, ?>> filteredColumns = getFilteredColumnSet(cQuery);
        if (notAllMandatoryPrimaryKeysDefinedInFilters(filteredColumns, scheme, cQuery)) {
            throw new CQueryException(
                    MessageFormat.format(
                            PARTITIONED_KEYS_ERROR_MESSAGE,
                            filteredColumns,
                            scheme.getPartitionKeys())
            );
        }

        if (notAllFiltersColumnsFromQueryExistInCurrentTableScheme(filteredColumns, scheme)) {
            throw new CQueryException(
                    MessageFormat.format(
                            NOT_EXISTING_FILTERED_COLUMNS_ERROR_MESSAGE,
                            Sets.difference(filteredColumns, scheme.getColumnsSet()),
                            scheme.getColumnsSet()));
        }
    }

    private static Set<CColumn<?, ?>> getFilteredColumnSet(final CQuery cQuery) {
        return cQuery.getFilters()
                     .stream().map(filter -> filter.getValue().getColumn())
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean notAllFiltersColumnsFromQueryExistInCurrentTableScheme(
            final Iterable<CColumn<?, ?>> filteredColumns,
            final TableScheme scheme) {

        for (final CColumn<?, ?> filter : filteredColumns) {
            if (!scheme.getColumnNames().contains(filter.getName())) {
                return true;
            }
        }
        return false;
    }

    private static void performOrderingsCheck(final CQuery cQuery, final TableScheme scheme)
            throws CMapServiceException {
        for (final COrdering ordering : cQuery.getCOrderingCollection()) {
            final CColumn<?, ?> column = ordering.getColumn();
            if (!scheme.containsColumn(column)) {
                throw new CQueryException(
                        MessageFormat.format(NO_SUCH_COLUMN_ERROR_MESSAGE,
                                column,
                                scheme.getColumnsSet())
                );
            }
            if (!scheme.isClusteringKey(column) && !scheme.isPartitionKey(column)) {
                throw new CQueryException(
                        MessageFormat.format(NOT_A_CLUSTERING_KEY_ERROR_MESSAGE,
                                column,
                                scheme.getClusteringKeys(),
                                scheme.getPartitionKeys())
                );
            }
        }
    }

    private static boolean notAllMandatoryPrimaryKeysDefinedInFilters(
            final Iterable<CColumn<?, ?>> filteredColumns,
            final TableScheme scheme, final CQuery cQuery) {

        final Collection<String> primaries =
                scheme.getPartitionKeys().stream()
                      .map(CColumn::getName)
                      .collect(Collectors.toList());

        for (final CColumn<?, ?> filter : filteredColumns) {
            final String name = filter.getName();
            if (primaries.contains(name)) {
                primaries.remove(name);
            }
        }

        return !primaries.isEmpty() && !cQuery.isAllowFiltering();
    }

    private static void verifyKeysDefinesOneRow(final CQuery cQuery, final TableScheme scheme) {
        final Set<CColumn<?, ?>> columns = cQuery.getExpectedColumnsCollection().getColumns();
        verifyColumns(columns, scheme);
    }

    private static void verifyColumns(final Set<CColumn<?, ?>> columns,
                                      final TableScheme scheme) {
        if (!scheme.getColumnsSet().containsAll(columns)) {
            throw new CQueryException(
                    MessageFormat.format(
                            VERIFY_COLUMNS_EXCEPTION_MESSAGE,
                            columns,
                            Sets.difference(scheme.getColumnsSet(), columns),
                            scheme.getTableName())
            );
        }
    }
}
