package org.nohope.test.stress;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
public class MultiInvocationStat extends AbstractStat {
    protected MultiInvocationStat(final TimerResolution resolution,
                                  final String name) {
        super(resolution, name);
    }

    protected void invoke(final int threadId,
                          final Action.Invoke invoke)
            throws InvocationException {

        invoke(threadId, new InvocationHandler<Object>() {
            @Override
            public Object invoke() throws Exception {
                invoke.invoke();
                return null;
            }
        });
    }

    protected <T> T invoke(final int threadId, final Action.Getter<T> getter)
            throws InvocationException {
        return invoke(threadId, new InvocationHandler<T>() {
            @Override
            public T invoke() throws Exception {
                return getter.get();
            }
        });
    }
}
