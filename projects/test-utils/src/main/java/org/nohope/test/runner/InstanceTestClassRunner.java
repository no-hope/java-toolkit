package org.nohope.test.runner;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * This runner allows to make analogue of non-static {@link org.junit.BeforeClass &#064;BeforeClass} and
 * {@link org.junit.AfterClass &#064;AfterClass}.
 *
 * <p>Example:
 * <pre>
 *     {@link org.junit.runner.RunWith &#064;RunWith}({@link InstanceTestClassRunner InstanceTestClassRunner})
 *     public class MyTest implements {@link InstanceTestSetupListener} {
 *         &#064;Override
 *         public void beforeClassSetup() {
 *         }
 *
 *         &#064;Override
 *         public void afterClassSetup(){
 *         }
 *     }
 * </pre>
 * </p>
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/27/13 10:53 AM
 */
public class InstanceTestClassRunner extends BlockJUnit4ClassRunner {
    private InstanceTestSetupListener instanceSetupListener;
    private Object test;

    public InstanceTestClassRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected Object createTest() throws Exception {
        // Note that JUnit4 will call this createTest() multiple times for each
        // test method, so we need to ensure to create test class only once as well
        // as to call "beforeClassSetup".

        if (test == null) {
            test = super.createTest();
        }
        if (test instanceof InstanceTestSetupListener && instanceSetupListener == null) {
            instanceSetupListener = (InstanceTestSetupListener) test;
            instanceSetupListener.beforeClassSetup();
        }
        return test;
    }

    @Override
    public void run(final RunNotifier notifier) {
        super.run(notifier);
        if (instanceSetupListener != null) {
            instanceSetupListener.afterClassSetup();
        }
    }
}
