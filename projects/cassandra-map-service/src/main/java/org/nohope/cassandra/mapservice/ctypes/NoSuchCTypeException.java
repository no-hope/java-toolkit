package org.nohope.cassandra.mapservice.ctypes;

/**
 */
public final class NoSuchCTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoSuchCTypeException(final String type) {
        super("Type " + type + " not known");
    }
}
