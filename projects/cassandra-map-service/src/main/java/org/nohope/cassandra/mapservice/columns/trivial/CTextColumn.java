package org.nohope.cassandra.mapservice.columns.trivial;

import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

/**
 */
public final class CTextColumn extends CColumn<String, String> {
    private CTextColumn(final String name) {
        super(name, CoreConverter.TEXT);
    }

    public static CTextColumn of(final String name) {
        return new CTextColumn(name);
    }
}
