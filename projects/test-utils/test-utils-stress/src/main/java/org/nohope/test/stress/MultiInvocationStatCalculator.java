package org.nohope.test.stress;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
class MultiInvocationStatCalculator extends StatCalculator {
    protected MultiInvocationStatCalculator(final TimerResolution resolution, final String name, final int concurrency) {
        super(resolution, name, concurrency);
    }
}
