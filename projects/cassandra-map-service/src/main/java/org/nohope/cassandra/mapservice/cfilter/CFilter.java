package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import org.nohope.cassandra.mapservice.Value;
import org.nohope.cassandra.mapservice.ctypes.Converter;

/**
 */
public interface CFilter<V> {
    Value<V> getValue();
    Clause apply(Converter<?, V> converter);
}
