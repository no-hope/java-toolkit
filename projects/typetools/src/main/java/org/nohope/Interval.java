package org.nohope;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Date: 10/4/12
 * Time: 5:53 PM
 */
public final class Interval implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalTime begin;
    private LocalTime end;
    private final Set<Integer> daysOfWeek = new HashSet<>();

    Interval() {
    }

    public Interval(final LocalTime begin, final LocalTime end) {
        this.begin = begin;
        this.end = end;
    }

    public Interval(final LocalTime begin, final LocalTime end, final Set<Integer> daysOfWeek) {
        this.begin = begin;
        this.end = end;
        setDaysOfWeek(daysOfWeek);
    }

    public Interval(final LocalTime begin, final LocalTime end, final int day) {
        this.begin = begin;
        this.end = end;
        final Set<Integer> dayOfWeek = new HashSet<>();
        dayOfWeek.add(day);
        setDaysOfWeek(dayOfWeek);
    }

    public Set<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * The values for the day of week are defined in {@link org.joda.time.DateTimeConstants}.
     *
     */
    public Interval setDaysOfWeek(final Set<Integer> daysOfWeek) {
        this.daysOfWeek.clear();
        this.daysOfWeek.addAll(daysOfWeek);
        return this;
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
        if (!daysOfWeek.isEmpty()) {
            final int dayOfWeek = timestamp.getDayOfWeek();
            if (!daysOfWeek.contains(dayOfWeek)) {
                return false;
            }
        }

        final LocalTime time = timestamp.toLocalTime();
        if (begin.isBefore(end)) {
            return begin.isBefore(time) && end.isAfter(time);
        } else {
            return begin.isBefore(time) || end.isAfter(time);
        }
    }
}
