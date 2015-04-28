package org.nohope.test.stress;

import org.nohope.test.stress.functors.Get;
import org.nohope.test.stress.functors.Invoke;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:23
 */
public final class PooledMeasureProvider extends MeasureData {
    private final Function<String, ExecutorService> poolLoader;
    private final Function<String, StatAccumulator> statLoader;

    PooledMeasureProvider(final int threadId,
                          final int operationNumber,
                          final Function<String, ExecutorService> poolLoader,
                          final Function<String, StatAccumulator> statLoader) {
        super(threadId, operationNumber);
        this.poolLoader = poolLoader;
        this.statLoader = statLoader;
    }

    @Override
    public long getThreadId() {
        return Thread.currentThread().getId();
    }

    public <T> Future<T> invoke(final String name, final Get<T> getter) throws Exception {
        final StatAccumulator calc = statLoader.apply(name);
        return poolLoader.apply(name).submit(() -> calc.invoke(getThreadId(), getter));
    }

    public Future<?> invoke(final String name, final Invoke invoke) throws Exception {
        final StatAccumulator calc = statLoader.apply(name);
        return poolLoader.apply(name).submit(() -> {
            try {
                calc.invoke(getThreadId(), invoke);
            } catch (final InvocationException ignored) { // already accounted
            }
        });
    }
}
