package org.nohope.test.stress;

import com.google.common.cache.LoadingCache;
import org.nohope.test.stress.functors.Get;
import org.nohope.test.stress.functors.Invoke;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:23
 */
public final class PooledMeasureProvider extends MeasureData {
    private final LoadingCache<String, ExecutorService> poolCache;
    private final LoadingCache<String, StatAccumulator> statCache;

    protected PooledMeasureProvider(final int threadId,
                                    final int operationNumber,
                                    final int concurrency,
                                    final LoadingCache<String, StatAccumulator> statCache,
                                    final LoadingCache<String, ExecutorService> poolCache) {
        super(threadId, operationNumber, concurrency);
        this.poolCache = poolCache;
        this.statCache = statCache;
    }

    @Override
    public long getThreadId() {
        return Thread.currentThread().getId();
    }

    public <T> Future<T> invoke(final String name, final Get<T> getter) throws Exception {
        final StatAccumulator calc = statCache.get(name);
        return poolCache.get(name).submit(() -> calc.invoke(getThreadId(), getter));
    }

    public void invoke(final String name, final Invoke invoke) throws Exception {
        final StatAccumulator calc = statCache.get(name);
        poolCache.get(name).submit(() -> {
            try {
                calc.invoke(getThreadId(), invoke);
            } catch (final InvocationException ignored) { // already accounted
            }
        });
    }
}
