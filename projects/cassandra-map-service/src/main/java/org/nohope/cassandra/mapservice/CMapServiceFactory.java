package org.nohope.cassandra.mapservice;

import org.nohope.cassandra.factory.CassandraFactory;

/**
 * Preferable factory for creating {@link org.nohope.cassandra.mapservice.CMapService map service}
 */
public final class CMapServiceFactory {
    private final CassandraFactory factory;

    public CMapServiceFactory(final CassandraFactory factory) {
        this.factory = factory;
    }

    public CMapService getService(final Iterable<TableScheme> schemes) {
        return new CMapService(factory, schemes);
    }

    public CMapService getService(final TableScheme... schemes) {
        return new CMapService(factory, schemes);
    }

    public CassandraFactory getFactory() {
        return factory;
    }
}
