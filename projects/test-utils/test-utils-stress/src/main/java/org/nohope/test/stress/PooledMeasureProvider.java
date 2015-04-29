package org.nohope.test.stress;

import org.nohope.test.stress.functors.Get;
import org.nohope.test.stress.functors.Call;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:23
 */
public final class PooledMeasureProvider extends AbstractMeasureData {
    private final Function<String, ExecutorService> poolLoader;
    private final Function<String, ActionStatsAccumulator> statLoader;

    PooledMeasureProvider(final int threadId,
                          final int operationNumber,
                          final Function<String, ExecutorService> poolLoader,
                          final Function<String, ActionStatsAccumulator> statLoader) {
        super(threadId, operationNumber);
        this.poolLoader = poolLoader;
        this.statLoader = statLoader;
    }

    @Override
    public long getThreadId() {
        return Thread.currentThread().getId();
    }

    public <T> Future<T> getFuture(final String name, final Get<T> getter) throws Exception {
        final ActionStatsAccumulator calc = statLoader.apply(name);
        return poolLoader.apply(name).submit(() -> calc.measure(getThreadId(), getter));
    }

    public Future<?> callFuture(final String name, final Call call) throws Exception {
        final ActionStatsAccumulator calc = statLoader.apply(name);
        return poolLoader.apply(name).submit(() -> {
            try {
                calc.measure(getThreadId(), call);
            } catch (final InvocationException ignored) { // already accounted
            }
        });
    }

    @Override
    public <T> T get(final String name, final Get<T> getter) throws Exception {
        return getFuture(name, getter).get();
    }

    @Override
    public void call(final String name, final Call getter) throws Exception {
        callFuture(name, getter).get();
    }
}
