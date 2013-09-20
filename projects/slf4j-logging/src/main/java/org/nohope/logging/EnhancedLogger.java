package org.nohope.logging;

import org.slf4j.Marker;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

/**
 * This class is just wrapper for foreign {@link org.slf4j.Logger}
 * implementation with small improvements.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2012-02-22 12:16
 */
final class EnhancedLogger implements Logger {
    private final org.slf4j.Logger logger;

    EnhancedLogger(final org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(final Throwable t, final String format, final Object... args) {
        if (isTraceEnabled()) {
            logger.trace(arrayFormat(format, args).getMessage(), t);
        }
    }

    @Override
    public void debug(final Throwable t, final String format, final Object... args) {
        if (isDebugEnabled()) {
            logger.debug(arrayFormat(format, args).getMessage(), t);
        }
    }

    @Override
    public void info(final Throwable t, final String format, final Object... args) {
        if (isInfoEnabled()) {
            logger.info(arrayFormat(format, args).getMessage(), t);
        }
    }

    @Override
    public void warn(final Throwable t, final String format, final Object... args) {
        if (isWarnEnabled()) {
            logger.warn(arrayFormat(format, args).getMessage(), t);
        }
    }

    @Override
    public void error(final Throwable t, final String format, final Object... args) {
        if (isErrorEnabled()) {
            logger.error(arrayFormat(format, args).getMessage(), t);
        }
    }

    @Override
    public void trace(final String format, final Object... args) {
        if (isTraceEnabled()) {
            logger.trace(arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... args) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void debug(final String format, final Object... args) {
        if (isDebugEnabled()) {
            logger.debug(arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... args) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void info(final String format, final Object... args) {
        if (isInfoEnabled()) {
            logger.info(arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void info(final Marker marker, final String format, final Object... args) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void warn(final String format, final Object... args) {
        if (isWarnEnabled()) {
            logger.warn(arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... args) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void error(final String format, final Object... args) {
        if (isErrorEnabled()) {
            logger.error(arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void error(final Marker marker, final String format, final Object... args) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, arrayFormat(format, args).getMessage());
        }
    }

    @Override
    public void error(final Throwable t) {
        if (isErrorEnabled()) {
            logger.error(null, t);
        }
    }

    @Override
    public void debug(final Throwable t) {
        if (isDebugEnabled()) {
            logger.debug(null, t);
        }
    }

    @Override
    public void info(final Throwable t) {
        if (isInfoEnabled()) {
            logger.info(null, t);
        }
    }

    @Override
    public void warn(final Throwable t) {
        if (isWarnEnabled()) {
            logger.warn(null, t);
        }
    }

    @Override
    public void trace(final Throwable t) {
        if (isTraceEnabled()) {
            logger.trace(null, t);
        }
    }

    // the rest is just proxying
    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(final String msg) {
        logger.trace(msg);
    }

    @Override
    public void trace(final String format, final Object arg) {
        logger.trace(format, arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        logger.trace(msg, t);
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        logger.trace(marker, msg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        logger.trace(marker, format, arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(final String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(final String format, final Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        logger.debug(msg, t);
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        logger.debug(marker, msg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        logger.debug(marker, format, arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(final String msg) {
        logger.info(msg);
    }

    @Override
    public void info(final String format, final Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(final String msg, final Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public void info(final Marker marker, final String msg) {
        logger.info(marker, msg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        logger.info(marker, format, arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(final String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(final String format, final Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        logger.warn(msg, t);
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        logger.warn(marker, msg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        logger.warn(marker, format, arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(final String msg) {
        logger.error(msg);
    }

    @Override
    public void error(final String format, final Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        logger.error(msg, t);
    }

    @Override
    public void error(final Marker marker, final String msg) {
        logger.error(marker, msg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        logger.error(marker, format, arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        logger.error(marker, msg, t);
    }

    /*
    private static final Pattern ptrn = Pattern.compile("(?<!\\\\)(\\\\\\\\)*\\{\\}");
    protected static int count(final String target) {
        if (target == null) {
            return 0;
        }

        final Matcher matcher = ptrn.matcher(target);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    protected static boolean isOne(final String target) {
        if (target == null) {
            return false;
        }

        final Matcher matcher = ptrn.matcher(target);
        int count = 0;
        while (matcher.find()) {
            count++;
            if (count > 1) {
                return false;
            }
        }
        return count == 1;
    }

    private static FormattingTuple detectWarargsFormat(final String format, final Object[] args) {
        if (count(format) != args.length){//isOne(format)) {
            return format(format, args);
        }
        return arrayFormat(format, args);
    }*/
}


