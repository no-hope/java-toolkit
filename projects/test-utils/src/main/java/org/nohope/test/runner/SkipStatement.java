package org.nohope.test.runner;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.Statement;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2/20/13 2:19 PM
 */
class SkipStatement extends Statement {
    private final Statement next;
    private final Class<? extends Throwable>[] expected;
    private final boolean deep;

    public SkipStatement(final Statement next,
                         final SkipOnException expected) {
        this.next = next;
        if (expected == null) {
            this.expected = null;
            this.deep = false;
        } else {
            this.expected = expected.value().clone();
            this.deep = expected.deep();
        }
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            next.evaluate();
        } catch (AssumptionViolatedException e) {
            throw e;
        } catch (Throwable e) {
            if (expected == null) {
                throw e;
            }

            if (deep) {
                for (final Throwable t : ExceptionUtils.getThrowables(e)) {
                    doThrowableTest(t);
                }
            } else {
                doThrowableTest(e);
            }

            throw e;
        }
    }

    private void doThrowableTest(final Throwable t) {
        for (final Class<? extends Throwable> clazz : expected) {
            if (clazz.isAssignableFrom(t.getClass())) {
                throw new AssumptionViolatedException(
                        "Method skipped due to exception <"
                                + clazz.getCanonicalName()
                                + "> was thrown"
                                + (deep ? " (as original exception sub-cause)" : ""));
            }
        }
    }
}
