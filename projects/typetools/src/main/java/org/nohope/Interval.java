package org.nohope;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.Serializable;

/**
 * Date: 10/4/12
 * Time: 5:53 PM
 */
public final class Interval implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalTime begin;
    private LocalTime end;

    public Interval() {
    }

    public Interval(final LocalTime begin, final LocalTime end) {
        this.begin = begin;
        this.end = end;
    }

    public LocalTime getBegin() {
        return begin;
    }

    public Interval setBegin(final LocalTime begin) {
        this.begin = begin;
        return this;
    }

    public LocalTime getEnd() {
        return end;
    }

    public Interval setEnd(final LocalTime end) {
        this.end = end;
        return this;
    }

    public boolean contains(final DateTime timestamp) {
        final LocalTime time = timestamp.toLocalTime();
        if (begin.isBefore(end)) {
            return begin.isBefore(time) && end.isAfter(time);
        } else {
            return begin.isBefore(time) || end.isAfter(time);
        }
    }
}
