package org.nohope.logging;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/22/12 4:37 PM
 */
final class EnhancedLoggerFactory implements ILoggerFactory {
    private final org.slf4j.ILoggerFactory factory;

    EnhancedLoggerFactory(final org.slf4j.ILoggerFactory factory) {
        this.factory = factory;
    }

    @Override
    public Logger getLogger(final String name) {
        return new EnhancedLogger(factory.getLogger(name));
    }
}
