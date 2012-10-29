package org.nohope.akka;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * Date: 9/4/12
 * Time: 12:00 PM
 */
@Immutable
public class NamedWorkerMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String identifier;
    private final Serializable data;

    /**
     * @deprecated do not use this constructor directly.
     *             It's used for jackson serialization only
     */
    @SuppressWarnings("unused")
    @Deprecated
    private NamedWorkerMetadata() {
        data = null;
        identifier = null;
    }

    public NamedWorkerMetadata(final String identifier,
                               final Serializable data) {
        this.identifier = identifier;
        this.data = data;
    }

    public String getIdentifier() {
        return identifier;
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
