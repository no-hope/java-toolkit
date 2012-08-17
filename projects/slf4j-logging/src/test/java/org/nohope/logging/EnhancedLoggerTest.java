package org.nohope.logging;

import org.easymock.Capture;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * @author ketoth xupack <ketoth.xupack@gmail.com>
 * @since 3/19/12 11:19 PM
 */
public final class EnhancedLoggerTest {
    private static final ThreadLocal<Capture<String>> LOG_ARGUMENT =
            new ThreadLocal<Capture<String>>() {
                @Override
                protected Capture<String> initialValue() {
                    return new Capture<>();
                }
            };
    private static final ThreadLocal<Capture<Marker>> LOG_MARKER =
            new ThreadLocal<Capture<Marker>>() {
                @Override
                protected Capture<Marker> initialValue() {
                    return new Capture<>();
                }
            };
    private static final ThreadLocal<Capture<String>> LOG_EXCEPTIONAL_ARGUMENT =
            new ThreadLocal<Capture<String>>() {
                @Override
                protected Capture<String> initialValue() {
                    return new Capture<>();
                }
            };
    private static final ThreadLocal<Capture<Throwable>> LOG_EXCEPTIONAL_THROWABLE =
            new ThreadLocal<Capture<Throwable>>() {
                @Override
                protected Capture<Throwable> initialValue() {
                    return new Capture<>();
                }
            };

    @Test
    public void trace() {
        assertVarargs(new ReflectiveVarargTester("trace"));
    }

    @Test
    public void debug() {
        assertVarargs(new ReflectiveVarargTester("debug"));
    }

    @Test
    public void info() {
        assertVarargs(new ReflectiveVarargTester("info"));
    }

    @Test
    public void warn() {
        assertVarargs(new ReflectiveVarargTester("warn"));
    }

    @Test
    public void error() {
        assertVarargs(new ReflectiveVarargTester("error"));
    }

    @Test
    public void properties() {
        final String randomName = UUID.randomUUID().toString();
        final org.nohope.logging.Logger log = LoggerFactory.getLogger(randomName);
        assertEquals(randomName, log.getName());

        final org.nohope.logging.Logger log1 = LoggerFactory.getLogger(getClass());
        assertEquals(getClass().getCanonicalName(), log1.getName());
    }


    public static void assertVarargs(final VarargsTester tester) {
        assertCorrectLogging(tester
                , "1 2 {}"
                , "[1, 2] {} {}"
                , "{} {} {}"
                , 1, 2);

        assertCorrectLogging(tester
                , "1 2 3 4 5"
                , "[1, 2, 3, 4, 5] {} {} {} {}"
                , "{} {} {} {} {}"
                , 1, 2, 3, 4, 5);

        assertCorrectLogging(tester
                , "1"
                , "[1, 2, 3, 4, 5, 6]"
                , "{}"
                , 1, 2, 3, 4, 5, 6);

        assertCorrectLogging(tester, "{}", "[]", "{}");
        assertCorrectLogging(tester, "{}", "null", "{}", (Object[]) null);
        assertCorrectLogging(tester, "null", "[null]", "{}", (Object) null);
    }

    public static void assertCorrectLogging(final VarargsTester tester,
                                            final String expected,
                                            final String expectedVarargAsObject,
                                            final String format,
                                            final Object... args) {
        final org.slf4j.Logger silentSlf4jLogger = createMock(org.slf4j.Logger.class);
        expect(tester.requestLogLevel(silentSlf4jLogger)).andReturn(false).times(5);
        expect(tester.requestLogLevel(silentSlf4jLogger, capture(LOG_MARKER.get()))).andReturn(false).times(2);
        final EnhancedLogger silentLogger = new EnhancedLogger(silentSlf4jLogger);

        final org.slf4j.Logger slf4jLogger = createMock(org.slf4j.Logger.class);
        final EnhancedLogger logger = new EnhancedLogger(slf4jLogger);

        tester.check(slf4jLogger, capture(LOG_ARGUMENT.get()));
        expectLastCall().anyTimes();

        tester.check(slf4jLogger, capture(LOG_EXCEPTIONAL_ARGUMENT.get()),
                capture(LOG_EXCEPTIONAL_THROWABLE.get()));
        expectLastCall().times(3);

        tester.check(slf4jLogger, capture(LOG_MARKER.get()),
                capture(LOG_EXCEPTIONAL_ARGUMENT.get()));
        expectLastCall().times(2);

        expect(tester.requestLogLevel(slf4jLogger)).andReturn(true).anyTimes();
        expect(tester.requestLogLevel(slf4jLogger, capture(LOG_MARKER.get()))).andReturn(true).anyTimes();

        replay(silentSlf4jLogger, slf4jLogger);

        {
            tester.doLogging(logger, format, args);
            assertEquals(expected, LOG_ARGUMENT.get().getValue());

            tester.doLogging(silentLogger, format, args);
        }

        {
            tester.doLogging(logger, format, (Object) args);
            assertEquals(expectedVarargAsObject, LOG_ARGUMENT.get().getValue());

            tester.doLogging(silentLogger, format, (Object) args);
        }

        {
            final Marker marker = createMock(Marker.class);
            tester.doLogging(logger, marker, format, args);
            assertEquals(expected, LOG_EXCEPTIONAL_ARGUMENT.get().getValue());
            assertSame(marker, LOG_MARKER.get().getValue());

            tester.doLogging(silentLogger, marker, format, args);
        }

        {
            final Marker marker = createMock(Marker.class);
            tester.doLogging(logger, marker, format, (Object) args);
            assertEquals(expectedVarargAsObject, LOG_EXCEPTIONAL_ARGUMENT.get().getValue());
            assertSame(marker, LOG_MARKER.get().getValue());

            tester.doLogging(silentLogger, marker, format, (Object) args);
        }

        {
            final Throwable ex = new Throwable(UUID.randomUUID().toString());
            tester.doLogging(logger, ex, format, args);
            assertEquals(expected, LOG_EXCEPTIONAL_ARGUMENT.get().getValue());
            assertSame(ex, LOG_EXCEPTIONAL_THROWABLE.get().getValue());

            tester.doLogging(silentLogger, ex, format, args);
        }

        {
            final Throwable ex = new Throwable(UUID.randomUUID().toString());
            tester.doLogging(logger, ex, format, (Object) args);
            assertEquals(expectedVarargAsObject, LOG_EXCEPTIONAL_ARGUMENT.get().getValue());
            assertSame(ex, LOG_EXCEPTIONAL_THROWABLE.get().getValue());

            tester.doLogging(silentLogger, ex, format, (Object) args);
        }

        {
            final Throwable ex = new Throwable(UUID.randomUUID().toString());
            tester.doLogging(logger, ex);
            assertNull(LOG_EXCEPTIONAL_ARGUMENT.get().getValue());
            assertSame(ex, LOG_EXCEPTIONAL_THROWABLE.get().getValue());

            tester.doLogging(silentLogger, ex);
        }

        verify(silentSlf4jLogger, slf4jLogger);
    }

    private interface VarargsTester {
        void doLogging(EnhancedLogger logger, String format, Object... args);
        boolean requestLogLevel(final org.slf4j.Logger mock);
        boolean requestLogLevel(final org.slf4j.Logger mock, final Marker m);

        void doLogging(EnhancedLogger logger,
                       Throwable e,
                       String format,
                       Object... args);

        void doLogging(EnhancedLogger logger, Throwable e);

        void doLogging(EnhancedLogger logger,
                       Marker m,
                       String format,
                       Object... args);

        void check(org.slf4j.Logger logger, String message);
        void check(org.slf4j.Logger logger, final String str, final Throwable e);
        void check(org.slf4j.Logger logger, final Marker e, final String str);
    }

    private static class ReflectiveVarargTester implements VarargsTester {
        private final String level;

        public ReflectiveVarargTester(final String level) {
            this.level = level;
        }

        @SuppressWarnings("unchecked")
        private static<T> T invoke(final Object target,
                                   final String method,
                                   final Class[] classes,
                                   final Object... args) {
            try {
                return (T) target.getClass().getMethod(method, classes).invoke(target, args);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void doLogging(final EnhancedLogger logger, final String format, final Object... args) {
            invoke(logger, level, new Class[]{String.class, Object[].class}, format, args);
        }

        @Override
        public boolean requestLogLevel(final Logger mock) {
            return invoke(mock,
                    "is" + Character.toUpperCase(level.charAt(0)) + level.substring(1) + "Enabled",
                    new Class[0]);
        }

        @Override
        public boolean requestLogLevel(final Logger mock, final Marker m) {
            return invoke(mock,
                    "is" + Character.toUpperCase(level.charAt(0)) + level.substring(1) + "Enabled",
                    new Class[]{Marker.class}, m);
        }

        @Override
        public void doLogging(final EnhancedLogger logger, final Throwable e, final String format, final Object... args) {
            invoke(logger, level, new Class[]{Throwable.class, String.class, Object[].class}, e, format, args);
        }

        @Override
        public void doLogging(final EnhancedLogger logger, final Throwable e) {
            invoke(logger, level, new Class[]{Throwable.class}, e);
        }

        @Override
        public void doLogging(final EnhancedLogger logger, final Marker m, final String format, final Object... args) {
            invoke(logger, level, new Class[]{Marker.class, String.class, Object[].class}, m, format, args);
        }

        @Override
        public void check(final Logger logger, final String message) {
            invoke(logger, level, new Class[]{String.class}, message);
        }

        @Override
        public void check(final Logger logger, final String str, final Throwable e) {
            invoke(logger, level, new Class[]{String.class, Throwable.class}, str, e);
        }

        @Override
        public void check(final Logger logger, final Marker e, final String str) {
            invoke(logger, level, new Class[]{Marker.class, String.class}, e, str);
        }
    }
}
