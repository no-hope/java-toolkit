package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.Converter;

/**
 */
public interface CFilter<V> {
    CColumn<?, ?> getColumn();
    Clause apply(Converter<?, V> converter);
}
