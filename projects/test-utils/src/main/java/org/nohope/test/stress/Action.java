package org.nohope.test.stress;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-27 16:21
 */
public abstract class Action {
    private final AtomicReference<StressScenario> scenario= new AtomicReference<>();
    private final ConcurrentMap<String, MultiInvocationStat> map = new ConcurrentHashMap<>();

    protected abstract void doAction(final int threadId,
                                     final int operationNumber) throws Exception;

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

    final void setScenario(final StressScenario scenario) {
        this.scenario.set(scenario);
    }

    private MultiInvocationStat getStat(final String name) {
        map.putIfAbsent(name, new MultiInvocationStat(
                this.scenario.get().getResolution(),
                name));

        return map.get(name);
    }

    ConcurrentMap<String, MultiInvocationStat> getMap() {
        return map;
    }

    public interface Invoke {
        void invoke() throws Exception;
    }

    public interface Getter<T> {
        T get() throws Exception;
    }
}
