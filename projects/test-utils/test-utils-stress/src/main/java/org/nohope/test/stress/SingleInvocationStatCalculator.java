package org.nohope.test.stress;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
class SingleInvocationStatCalculator extends StatCalculator {
    private final NamedAction action;

    public SingleInvocationStatCalculator(final TimerResolution resolution,
                                          final NamedAction action,
                                          final int concurrency) {
        super(resolution, action.getName(), concurrency);
        this.action = action;
    }

    protected void invoke(final int threadId,
                          final int operationNumber) throws InvocationException {
        final MeasureData p = new MeasureData(threadId, operationNumber, getConcurrency());
        this.invoke(threadId, () -> {
            action.doAction(p);
            return null;
        });
    }
}
