package org.nohope.logging;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/22/12 4:41 PM
 */
@SuppressFBWarnings(
        value = "NM_SAME_SIMPLE_NAME_AS_INTERFACE",
        justification = "The whole point was to use same names"
)
public interface ILoggerFactory extends org.slf4j.ILoggerFactory {
    @Override Logger getLogger(String name);
}
