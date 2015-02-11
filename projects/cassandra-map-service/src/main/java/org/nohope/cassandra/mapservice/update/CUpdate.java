package org.nohope.cassandra.mapservice.update;

import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.cops.Operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Update wrapper for {@link com.datastax.driver.core.querybuilder.QueryBuilder#update(String) update}
 */
public class CUpdate {
    private final Collection<CFilter> filters = new ArrayList<>();
    private final Collection<Operation<?>> operations = new ArrayList<>();

    public static CUpdate withFilters(final CFilter... filters) {
        final CUpdate update = new CUpdate();
        update.filters.addAll(Arrays.asList(filters));
        return update;
    }

    public Iterable<CFilter> getFilters() {
        return Collections.unmodifiableCollection(filters);
    }

    public Iterable<Operation<?>> getOperations() {
        return Collections.unmodifiableCollection(operations);
    }

    public CUpdate apply(final Operation<?>... operations) {
        this.operations.addAll(Arrays.asList(operations));
        return this;
    }
}
