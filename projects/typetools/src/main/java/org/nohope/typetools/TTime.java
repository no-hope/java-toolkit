package org.nohope.typetools;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Date: 21.05.12
 * Time: 13:19
 */
public class TTime {
    static public int delta(final DateTime ts1, final DateTime ts2) {
        final Period delta = new Period(ts1, ts2);
        final int deltaSec = delta.toStandardSeconds().getSeconds();
        return Math.abs(deltaSec);
    }
}
