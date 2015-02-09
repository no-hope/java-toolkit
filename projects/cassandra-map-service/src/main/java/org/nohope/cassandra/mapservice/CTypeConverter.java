package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.BindMarker;
import org.nohope.cassandra.mapservice.ctypes.CType;

public abstract class CTypeConverter<V, C> {
    public abstract CType getCType();

    public abstract V readValue(Row result, String name);

    protected abstract C convert(V value);

    public final Object toCassandra(final Object value) {
        if (value instanceof BindMarker) {
            return value;
        }

        @SuppressWarnings("unchecked") final V casted = (V) value;
        return convert(casted);
    }
}
