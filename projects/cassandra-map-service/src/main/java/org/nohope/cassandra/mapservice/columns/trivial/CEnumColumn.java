package org.nohope.cassandra.mapservice.columns.trivial;

import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.custom.EnumType;

/**
 */
public class CEnumColumn<E extends Enum<E>> extends CColumn<E, String> {
    private CEnumColumn(final String name, final Class<E> clazz) {
        super(name, new EnumType<>(clazz));
    }

    public static <E extends Enum<E>> CEnumColumn<E> of(final String name,
                                                        final Class<E> clazz) {
        return new CEnumColumn<>(name, clazz);
    }
}
