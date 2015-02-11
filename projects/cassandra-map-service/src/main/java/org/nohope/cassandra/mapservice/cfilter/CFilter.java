package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import org.nohope.cassandra.mapservice.ctypes.Converter;

/**
 */
public interface CFilter<V> {
    String getColumnName();

    Clause apply(Converter<?, V> converter);
}
