package org.nohope.akka;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * Date: 9/4/12
 * Time: 12:00 PM
 */
@Immutable
public final class NamedWorkerMetadata implements Serializable {
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

    public NamedWorkerMetadata(@Nonnull final String identifier,
                               @Nonnull final Serializable data) {
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final NamedWorkerMetadata that = (NamedWorkerMetadata) o;
        return identifier.equals(that.identifier);
    }
}
