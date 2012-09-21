package org.nohope.akka;

import java.io.Serializable;

/**
 * Date: 9/4/12
 * Time: 12:00 PM
 */
public class NamedWorkerMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String identifier;
    private Serializable data;

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

    public Object getData() {
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
