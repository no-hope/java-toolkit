package org.nohope.cassandra.factory;

/**
 */
public class CassandraSchemaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CassandraSchemaException(final String message) {
        super(message);
    }
}
