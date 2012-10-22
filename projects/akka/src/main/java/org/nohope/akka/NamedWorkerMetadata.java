package org.nohope.akka;

import java.io.Serializable;

/**
 *
 * data field must not contain "Object" field, otherwise
 * NamedWorkerMetadata will not be able to be restored from
 * mongoDb
 *
 * Date: 9/4/12
 * Time: 12:00 PM
 */
public class NamedWorkerMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private String identifier;
    private Serializable data;

    /**
     * @deprecated do not use this constructor directly.
     *             It's used for jackson serialization only
     */
    @SuppressWarnings("unused")
    @Deprecated
    private NamedWorkerMetadata() {
    }

    public NamedWorkerMetadata(final String identifier, final Serializable data) {
        this.identifier = identifier;
        this.data = data;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setData(final Serializable data) {
        this.data = data;
    }

    public Serializable getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof NamedWorkerMetadata && ((NamedWorkerMetadata) obj).identifier.equals(identifier);
    }
}
