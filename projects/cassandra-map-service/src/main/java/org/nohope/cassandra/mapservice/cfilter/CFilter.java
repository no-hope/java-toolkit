package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import org.nohope.cassandra.mapservice.CTypeConverter;

/**
 */
public interface CFilter {
    String getColumnName();

    Clause apply(CTypeConverter<?, ?> converter);
}
