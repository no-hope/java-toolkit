package org.nohope.cassandra.util;

/**
 */
public class RowNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public RowNotFoundException(final String id) {
        super(id);
    }
}
