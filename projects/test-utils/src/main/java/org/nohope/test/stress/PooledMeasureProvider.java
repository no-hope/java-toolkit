package org.nohope.test.stress;

import com.google.common.cache.LoadingCache;
import org.nohope.test.stress.action.Get;
import org.nohope.test.stress.action.Invoke;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:23
 */
public final class PooledMeasureProvider extends MeasureData {
    private final LoadingCache<String, ExecutorService> poolCache;
    private final LoadingCache<String, MultiInvocationStatCalculator> statCache;

    protected PooledMeasureProvider(final int threadId,
                                    final int operationNumber,
                                    final LoadingCache<String, MultiInvocationStatCalculator> statCache,
                                    final LoadingCache<String, ExecutorService> poolCache) {
        super(threadId, operationNumber);
        this.poolCache = poolCache;
        this.statCache = statCache;
    }

    @Override
    public long getThreadId() {
        return Thread.currentThread().getId();
    }

    public <T> Future<T> invoke(final String name, final Get<T> getter) throws Exception {
        final MultiInvocationStatCalculator calc = statCache.get(name);
        return poolCache.get(name).submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return calc.invoke(getThreadId(), getter);
            }
        });
    }

    public void invoke(final String name, final Invoke invoke) throws Exception {
        final MultiInvocationStatCalculator calc = statCache.get(name);
        poolCache.get(name).submit(new Runnable() {
            @Override
            public void run() {
                try {
                    calc.invoke(getThreadId(), invoke);
                } catch (InvocationException ignored) { // already accounted
                }
            }
        });
    }
}
