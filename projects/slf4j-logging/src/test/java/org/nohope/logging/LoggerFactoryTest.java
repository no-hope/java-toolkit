package org.nohope.logging;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/22/12 2:06 PM
 */
public class LoggerFactoryTest extends UtilitiesTestSupport {
    @Override
    protected Class<?> getUtilityClass() {
        return LoggerFactory.class;
    }

    @Test
    public void properties() {
        final String randomName = UUID.randomUUID().toString();
        final org.nohope.logging.Logger log = LoggerFactory.getLogger(randomName);
        assertEquals(randomName, log.getName());

        final org.nohope.logging.Logger log1 = LoggerFactory.getLogger(getClass());
        assertEquals(getClass().getCanonicalName(), log1.getName());

        assertTrue(LoggerFactory.getILoggerFactory() instanceof EnhancedLoggerFactory);
        assertTrue(LoggerFactory.getILoggerFactory().getLogger("test") instanceof EnhancedLogger);
    }
}
