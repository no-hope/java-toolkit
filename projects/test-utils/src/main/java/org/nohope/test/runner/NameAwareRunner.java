package org.nohope.test.runner;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple {@link org.junit.runner.Runner JUnit runner} which prints out name
 * of test before proceeding it.
 *
 * Usage:
 * <pre>
 *  &#064;RunWith({@link NameAwareRunner NameAwareRunner.class})
 *  public class Test {
 *      ...
 *  }
 * </pre>
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 18/01/11 05:40 PM
 */
public final class NameAwareRunner extends BlockJUnit4ClassRunner {
    /** Logger which will be used for logging out method name. */
    private final Logger log;

    /**
     * Runner constructor.
     *
     * @param clazz test class
     * @throws InitializationError on runner initialization error
     */
    public NameAwareRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
        log = LoggerFactory.getLogger(clazz);
    }

    @Override
    protected Statement methodBlock(final FrameworkMethod method) {
        log.info("------------------- {} -------------------",
                method.getName());
        return super.methodBlock(method);
    }
}
