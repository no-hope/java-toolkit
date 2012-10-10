package org.nohope;

import org.joda.time.DateTime;
import org.nohope.typetools.JSON;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
* Date: 9/20/12
* Time: 11:27 AM
*/
public final class SeriesElement<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DateTime timestamp;
    private final T value;

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
        return JSON.jsonify(this);
    }
}
