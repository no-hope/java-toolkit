package org.nohope.test.stress;

import org.nohope.test.stress.functors.Get;
import org.nohope.test.stress.functors.Invoke;

import java.util.function.Function;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:23
 */
public final class MeasureProvider extends MeasureData {
    private final Function<String, StatAccumulator> accumulatorLoader;

    protected MeasureProvider(final int threadId,
                              final int operationNumber,
                              final Function<String, StatAccumulator> accumulatorLoader) {
        super(threadId, operationNumber);
        this.accumulatorLoader = accumulatorLoader;
    }

    public <T> T get(final String name, final Get<T> getter) throws Exception {
        return accumulatorLoader.apply(name).invoke(getThreadId(), getter);
    }

    public void call(final String name, final Invoke invoke) throws Exception {
        accumulatorLoader.apply(name).invoke(getThreadId(), invoke);
    }
}
