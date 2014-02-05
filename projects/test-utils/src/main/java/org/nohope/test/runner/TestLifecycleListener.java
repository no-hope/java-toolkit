package org.nohope.test.runner;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/27/13 10:54 AM
 *
 * @see TestLifecycleListeningClassRunner
 */
public interface TestLifecycleListener {
    /**
     * Code inside will be executed after test instance creation
     * and before any test method invocation.
     */
    void runBeforeAllTests() throws Exception;

    /**
     * Code inside will be executed after all test methods compete it's
     * execution.
     */
    void runAfterAllTests() throws Exception;
}
