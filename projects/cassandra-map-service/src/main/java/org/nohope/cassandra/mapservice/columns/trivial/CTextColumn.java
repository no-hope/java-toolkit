package org.nohope.cassandra.mapservice.columns.trivial;

import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.TrivialType;

/**
 */
public final class CTextColumn extends CColumn<String, String> {
    private CTextColumn(final String name) {
        super(name, TrivialType.TEXT);
    }

    public static CTextColumn of(final String name) {
        return new CTextColumn(name);
    }
}
