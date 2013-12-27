package org.nohope.test.stress;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-27 16:21
 */
public abstract class Action {
    protected abstract void doAction(final int threadId,
                                     final int operationNumber)
            throws Exception;

    private final AtomicInteger threads = new AtomicInteger();

    final ConcurrentMap<String, MultiInvocationStat> map = new ConcurrentHashMap<>();


    protected final <T> T invoke(final int threadId,
                                 final String name,
                                 final Getter<T> getter) throws Exception {
        return getStat(name).invoke(threadId, getter);
    }

    protected final void invoke(final int threadId,
                                final String name,
                                final Invoke invoke) throws Exception {
        getStat(name).invoke(threadId, invoke);
    }

    public interface Invoke {
        void invoke() throws Exception;
    }

    protected final void setTcreadsCount(final int threadsCount) {
        threads.set(threadsCount);
    }

    private MultiInvocationStat getStat(final String name) {
        map.putIfAbsent(name, new MultiInvocationStat(name, threads.get()));
        return map.get(name);
    }

    public ConcurrentMap<String, MultiInvocationStat> getMap() {
        return map;
    }

    public interface Getter<T> {
        T get() throws Exception;
    }
}
