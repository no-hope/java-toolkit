package org.nohope.logging;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/22/12 4:41 PM
 */
public interface ILoggerFactory extends org.slf4j.ILoggerFactory {
    @Override Logger getLogger(String name);
}
