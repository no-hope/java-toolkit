package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import org.nohope.cassandra.mapservice.Value;

/**
 */
public interface CFilter<V> {
    Value<V> getValue();

    Clause apply(); // apply(getValue().getColumn().getConverter())
}
