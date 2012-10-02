package org.nohope;

import org.joda.time.DateTime;
import org.nohope.typetools.JSON;

import javax.annotation.Nonnull;

/**
* Date: 9/20/12
* Time: 11:27 AM
*/
public class SeriesElement<T> {
    private final DateTime timestamp;
    private final T value;

    public SeriesElement(final DateTime timestamp, final T value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    @Nonnull public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return JSON.jsonify(this);
    }
}
