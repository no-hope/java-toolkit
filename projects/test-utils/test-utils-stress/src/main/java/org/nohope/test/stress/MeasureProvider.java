package org.nohope.test.stress;

import org.nohope.test.stress.functors.Get;
import org.nohope.test.stress.functors.Invoke;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:23
 */
public final class MeasureProvider extends MeasureData {
    private final StressScenario scenario;
    private final ConcurrentMap<String, StatAccumulator> map;
    private final Function<String, StatAccumulator> calculatorFunction;

    protected MeasureProvider(final StressScenario scenario,
                              final int threadId,
                              final int operationNumber,
                              final int concurrency,
                              final ConcurrentMap<String, StatAccumulator> map) {
        super(threadId, operationNumber, concurrency);
        this.scenario = scenario;
        this.map = map;
        this.calculatorFunction = newName -> new StatAccumulator(
                this.scenario.getResolution(), newName,
                concurrency);
    }

    public <T> T get(final String name, final Get<T> getter) throws Exception {
        return getStat(name).invoke(getThreadId(), getter);
    }

    public void call(final String name, final Invoke invoke) throws Exception {
        getStat(name).invoke(getThreadId(), invoke);
    }

    private StatAccumulator getStat(final String name) {
        return map.computeIfAbsent(name, calculatorFunction);
    }

}
