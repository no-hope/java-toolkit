package org.nohope.test.runner;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * This runner allows to make analogue of non-static {@link org.junit.BeforeClass &#064;BeforeClass} and
 * {@link org.junit.AfterClass &#064;AfterClass}.
 *
 * <p>Example:
 * <pre>
 *     {@link org.junit.runner.RunWith &#064;RunWith}({@link TestLifecycleListeningClassRunner TestLifecycleListeningClassRunner})
 *     public class MyTest implements {@link TestLifecycleListener} {
 *         &#064;Override
 *         public void runBeforeAllTests() {
 *         }
 *
 *         &#064;Override
 *         public void runAfterAllTests(){
 *         }
 *     }
 * </pre>
 * </p>
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/27/13 10:53 AM
 */
public class TestLifecycleListeningClassRunner extends BlockJUnit4ClassRunner {
    private TestLifecycleListener instanceSetupListener;
    private Object test;

    public TestLifecycleListeningClassRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected synchronized Object createTest() throws Exception {
        // Note that JUnit4 will call this createTest() multiple times for each
        // test method, so we need to ensure to create test class only once as well
        // as to call "runBeforeAllTests".

        if (test == null) {
            test = super.createTest();
        }
        if (test instanceof TestLifecycleListener && instanceSetupListener == null) {
            instanceSetupListener = (TestLifecycleListener) test;
            instanceSetupListener.runBeforeAllTests();
        }
        return test;
    }

    @Override
    public void run(final RunNotifier notifier) {
        super.run(notifier);
        if (instanceSetupListener != null) {
            try {
                instanceSetupListener.runAfterAllTests();
            } catch (final AssumptionViolatedException e) {
                new EachTestNotifier(notifier, getDescription()).fireTestIgnored();
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
