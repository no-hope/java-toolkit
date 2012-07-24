package org.nohope.logging;

import org.slf4j.Marker;

/**
 * An extended {@link org.slf4j.Logger} interface.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2012-02-22 12:13
 */
public interface Logger extends org.slf4j.Logger {
    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param t the exception (throwable) to log
     * @param format the format string
     * @param args an array of arguments
     */
    void trace(Throwable t, String format, Object... args);

    @Override
    void trace(String format, Object... args);

    @Override
    void trace(Marker marker, String format, Object... args);

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param t the exception (throwable) to log
     * @param format the format string
     * @param args an array of arguments
     */
    void debug(Throwable t, String format, Object... args);

    @Override
    void debug(String format, Object... args);

    @Override
    void debug(Marker marker, String format, Object... args);

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param t the exception (throwable) to log
     * @param format the format string
     * @param args an array of arguments
     */
    void info(Throwable t, String format, Object... args);

    @Override
    void info(String format, Object... args);

    @Override
    void info(Marker marker, String format, Object... args);

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param t the exception (throwable) to log
     * @param format the format string
     * @param args an array of arguments
     */
    void warn(Throwable t, String format, Object... args);

    @Override
    void warn(String format, Object... args);

    @Override
    void warn(Marker marker, String format, Object... args);

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param t the exception (throwable) to log
     * @param format the format string
     * @param args an array of arguments
     */
    void error(Throwable t, String format, Object... args);

    @Override
    void error(String format, Object... args);

    @Override
    void error(Marker marker, String format, Object... args);
}
