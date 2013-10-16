package org.nohope.test.runner;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/27/13 10:54 AM
 *
 * @see InstanceTestClassRunner
 */
public interface InstanceTestSetupListener {
    /** Code inside will run after test class is created before any test method was run. */
    void beforeClassSetup() throws Exception;

    /** Code inside will run after all test methods finished execution. */
    void afterClassSetup() throws Exception;
}
