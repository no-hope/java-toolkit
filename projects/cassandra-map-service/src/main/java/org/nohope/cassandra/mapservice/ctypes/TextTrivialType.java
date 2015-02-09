package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;

/**
 * Wrapper for cassandra trivial type TEXT, VARCHAR
 */
public final class TextTrivialType extends TrivialType<String> {
    TextTrivialType(final CType ctype) {
        super(ctype);
    }

    @Override
    public String readValue(final Row result, final String name) {
        return result.getString(name);
    }
}
