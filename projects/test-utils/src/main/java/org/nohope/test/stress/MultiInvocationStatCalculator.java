package org.nohope.test.stress;

import org.nohope.test.stress.action.Get;
import org.nohope.test.stress.action.Invoke;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
class MultiInvocationStatCalculator extends StatCalculator {
    protected MultiInvocationStatCalculator(final TimerResolution resolution, final String name) {
        super(resolution, name);
    }

    protected void invoke(final long threadId, final Invoke invoke)
            throws InvocationException {
        invoke(threadId, new InvocationHandler<Object>() {
            @Override
            public Object invoke() throws Exception {
                invoke.invoke();
                return null;
            }
        });
    }

    protected <T> T invoke(final long threadId, final Get<T> getter)
            throws InvocationException {
        return invoke(threadId, new InvocationHandler<T>() {
            @Override
            public T invoke() throws Exception {
                return getter.get();
            }
        });
    }
}
