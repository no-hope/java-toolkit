package org.nohope.cassandra.mapservice;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.nohope.cassandra.mapservice.cfilter.CFilter;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
        final Set<String> filteredColumns = getFilteredColumnSet(cQuery);
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

    private static Set<String> getFilteredColumnSet(final CQuery cQuery) {
        final Set<String> filteredColumnSet = new LinkedHashSet<>();
        for (final CFilter filter : cQuery.getFilters()) {
            filteredColumnSet.add(filter.getColumnName());
        }
        return filteredColumnSet;
    }

    private static boolean notAllFiltersColumnsFromQueryExistInCurrentTableScheme(
            final Iterable<String> filteredColumns,
            final TableScheme scheme) {

        for (final String filter : filteredColumns) {
            if (!scheme.containsColumn(filter)) {
                return true;
            }
        }
        return false;
    }

    private static void performOrderingsCheck(final CQuery cQuery, final TableScheme scheme)
            throws CMapServiceException {
        for (final COrdering ordering : cQuery.getCOrderingCollection()) {
            final String columnName = ordering.getColumnName();
            if (!scheme.containsColumn(columnName)) {
                throw new CQueryException(
                        MessageFormat.format(NO_SUCH_COLUMN_ERROR_MESSAGE,
                                columnName,
                                scheme.getColumnsSet())
                );
            }
            if (!scheme.isClusteringKey(columnName) && !scheme.isPartitionKey(columnName)) {
                throw new CQueryException(
                        MessageFormat.format(NOT_A_CLUSTERING_KEY_ERROR_MESSAGE,
                                columnName,
                                scheme.getClusteringKeys(),
                                scheme.getPartitionKeys())
                );
            }
        }
    }

    private static boolean notAllMandatoryPrimaryKeysDefinedInFilters(
            final Iterable<String> filteredColumns,
            final TableScheme scheme, final CQuery cQuery) {
        final Collection<String> primaries = Lists.newArrayList(scheme.getPartitionKeys());
        for (final String filter : filteredColumns) {
            if (primaries.contains(filter)) {
                primaries.remove(filter);
            }
        }

        return !primaries.isEmpty() && !cQuery.isAllowFiltering();
    }

    private static void verifyKeysDefinesOneRow(final CQuery cQuery, final TableScheme scheme) {
        final Set<String> columns = cQuery.getExpectedColumnsCollection().getColumns();
        verifyColumns(columns, scheme);
    }

    private static void verifyColumns(final Set<String> columns, final TableScheme scheme) {
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
