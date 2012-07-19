package org.nohope.logging;

import org.slf4j.ILoggerFactory;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2012-02-22 12:12
 */
public final class LoggerFactory {
    private LoggerFactory() {
    }

    public static Logger getLogger(final Class<?> clazz) {
        return new EnhancedLogger(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    public static Logger getLogger(final String name) {
        return new EnhancedLogger(org.slf4j.LoggerFactory.getLogger(name));
    }

    public static ILoggerFactory getILoggerFactory() {
        return org.slf4j.LoggerFactory.getILoggerFactory();
    }
}
