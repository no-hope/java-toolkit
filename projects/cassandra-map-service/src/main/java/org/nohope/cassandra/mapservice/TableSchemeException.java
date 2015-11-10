package org.nohope.cassandra.mapservice;

/**
 */
public final class TableSchemeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TableSchemeException(final String message) {
        super(message);
    }

    public TableSchemeException() {
        super();
    }
}
