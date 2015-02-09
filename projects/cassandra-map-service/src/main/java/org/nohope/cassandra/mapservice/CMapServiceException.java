package org.nohope.cassandra.mapservice;

/**
 */
public final class CMapServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CMapServiceException(final String errorMessage) {
        super(errorMessage);
    }

    public CMapServiceException(final String errorMessage, final Throwable throwable) {
        super(errorMessage, throwable);
    }
}
