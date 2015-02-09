package org.nohope.cassandra.mapservice.cops;

import com.datastax.driver.core.querybuilder.Assignment;
import org.nohope.cassandra.mapservice.CTypeConverter;

/**
 * @since 2014-07-28 17:19
 */
public interface Operation {
    String getColumnName();

    Assignment apply(CTypeConverter<?, ?> converter);
}
