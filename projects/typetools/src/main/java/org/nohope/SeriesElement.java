package org.nohope;

import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.io.Serializable;

import static org.nohope.typetools.JSON.JSON;

/**
* Date: 9/20/12
* Time: 11:27 AM
*/
public final class SeriesElement<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DateTime timestamp;
    private final T value;

    /**
     * @deprecated do not use this constructor directly.
     *             It's used for jackson serialization only
     */
    @Deprecated
    @SuppressWarnings("unused")
    private SeriesElement() {
        this.timestamp = null;
        this.value = null;
    }

    public SeriesElement(@Nonnull final DateTime timestamp,
                         @Nonnull final T value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Nonnull
    public DateTime getTimestamp() {
        return timestamp;
    }

    @Nonnull
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return JSON.jsonify(this).toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SeriesElement that = (SeriesElement) o;
        return timestamp.isEqual(timestamp) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = ((Long) timestamp.getMillis()).hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
