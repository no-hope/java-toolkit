package org.nohope.typetools;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.nohope.IMatcher;
import org.nohope.Matchers;

import javax.annotation.Nonnull;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Date: 21.05.12
 * Time: 13:19
 */
public final class TTime {
    public static final String UTC_ID = "UTC";

    private TTime() {
    }

    public static DateTime utcNow() {
        return now(UTC_ID);
    }

    public static DateTime now(final String id) {
        return DateTime.now(DateTimeZone.forID(id));
    }

    public static int deltaInSeconds(final DateTime ts1, final DateTime ts2) {
        final Period delta = new Period(ts1, ts2);
        final int deltaSec = delta.toStandardSeconds().getSeconds();
        return Math.abs(deltaSec);
    }

    /** Sets both system and joda time default timezones to {@code UTC} */
    public static void setUtcTimezone() {
        setDefaultTimezone(UTC_ID);
    }

    /** Sets both system and joda time default timezones to given timezone */
    static void setDefaultTimezone(final String id) {
        final DateTimeZone defaultZone = DateTimeZone.forID(id);
        DateTimeZone.setDefault(defaultZone);
        TimeZone.setDefault(TimeZone.getTimeZone(id));
    }

    public static DatatypeFactory getDatatypeFactory() {
        return LazyDataTypeFactorySingleton.getFactory();
    }

    /**
     * @return {@link XMLGregorianCalendar XMLGregorianCalendar} representing current date in {@code UTC} timezone
     *
     * @see #toXmlUtcCalendar(java.util.Date)
     */
    public static XMLGregorianCalendar xmlCalendarUtcNow() {
        return toXmlUtcCalendar(new Date());
    }

    /**
     * Creates {@link XMLGregorianCalendar XMLGregorianCalendar} for given date in {@code UTC} timezone.
     *
     * @see #toXmlCalendar(java.util.Date, String)
     */
    public static XMLGregorianCalendar toXmlUtcCalendar(final Date date) {
        return toXmlCalendar(date, UTC_ID);
    }

    /** Creates {@link XMLGregorianCalendar XMLGregorianCalendar} for given date in given timezone. */
    public static XMLGregorianCalendar toXmlCalendar(final Date date, final String timezoneId) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone(timezoneId));
        calendar.setTime(date);
        return getDatatypeFactory().newXMLGregorianCalendar(calendar);
    }

    public static boolean ne(@Nonnull final DateTime c1,
                             @Nonnull final DateTime c2) {
        return !eq(c1, c2);
    }

    public static boolean eq(@Nonnull final DateTime c1,
                             @Nonnull final DateTime c2) {
        return c1.isEqual(c2);
    }

    public static boolean lte(@Nonnull final DateTime c1,
                              @Nonnull final DateTime c2) {
        return lt(c1, c2) || eq(c1, c2);
    }

    public static boolean lt(@Nonnull final DateTime c1,
                             @Nonnull final DateTime c2) {
        return c1.isBefore(c2);
    }

    public static boolean gte(@Nonnull final DateTime c1,
                              @Nonnull final DateTime c2) {
        return gt(c1, c2) || eq(c1, c2);
    }

    public static boolean gt(@Nonnull final DateTime c1,
                             @Nonnull final DateTime c2) {
        return c1.isAfter(c2);
    }

    public static boolean ne(@Nonnull final XMLGregorianCalendar c1,
                             @Nonnull final XMLGregorianCalendar c2) {
        return compare(c1, c2, Matchers.not(Matchers.eq(DatatypeConstants.EQUAL)));
    }

    public static boolean eq(@Nonnull final XMLGregorianCalendar c1,
                             @Nonnull final XMLGregorianCalendar c2) {
        return compare(c1, c2, Matchers.eq(DatatypeConstants.EQUAL));
    }

    public static boolean lte(@Nonnull final XMLGregorianCalendar c1,
                              @Nonnull final XMLGregorianCalendar c2) {
        return compare(c1, c2, Matchers.or(Matchers.eq(DatatypeConstants.LESSER),
                Matchers.eq(DatatypeConstants.EQUAL)));
    }

    public static boolean lt(@Nonnull final XMLGregorianCalendar c1,
                             @Nonnull final XMLGregorianCalendar c2) {
        return compare(c1, c2, Matchers.eq(DatatypeConstants.LESSER));
    }

    public static boolean gte(@Nonnull final XMLGregorianCalendar c1,
                              @Nonnull final XMLGregorianCalendar c2) {
        return compare(c1, c2, Matchers.or(
                Matchers.eq(DatatypeConstants.GREATER),
                Matchers.eq(DatatypeConstants.EQUAL)
        ));
    }

    public static boolean gt(@Nonnull final XMLGregorianCalendar c1,
                             @Nonnull final XMLGregorianCalendar c2) {
        return compare(c1, c2, Matchers.eq(DatatypeConstants.GREATER));
    }

    public static boolean compare(@Nonnull final XMLGregorianCalendar c1,
                                  @Nonnull final XMLGregorianCalendar c2,
                                  @Nonnull final IMatcher<Integer> compareResultMatcher) {
        return compareResultMatcher.matches(c1.compare(c2));
    }

    static final class LazyDataTypeFactorySingleton {
        private static final DatatypeFactory datatypeFactory;
        static {
            try {
                datatypeFactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException("Unable to create DataTypeFactory instance", e);
            }
        }

        private LazyDataTypeFactorySingleton() {
        }

        public static DatatypeFactory getFactory() {
            return LazyDataTypeFactorySingleton.datatypeFactory;
        }
    }
}
