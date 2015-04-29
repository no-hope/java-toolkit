package org.nohope.test.stress;

import org.nohope.test.stress.functors.Call;
import org.nohope.test.stress.functors.Get;
import org.nohope.test.stress.util.Measurement;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
final class ActionStatsAccumulator {
    private static final Function<Long, Queue<Measurement>> NEW_LIST =
            x -> new ConcurrentLinkedQueue<>();
    private static final Function<Long, Queue<InvocationException>> NEW_QUEUE =
            c -> new ConcurrentLinkedQueue<>();

    private final Map<Long, Collection<Measurement>> timesPerThread = new ConcurrentHashMap<>();
    private final Map<Long, Collection<InvocationException>> errorStats = new ConcurrentHashMap<>();
    private final String name;

    ActionStatsAccumulator(final String name) {
        this.name = name;
    }

    public Map<Long, Collection<Measurement>> getTimesPerThread() {
        return timesPerThread;
    }

    public Map<Long, Collection<InvocationException>> getErrorStats() {
        return errorStats;
    }

    public String getName() {
        return name;
    }

    <T> T measure(final long threadId, final Get<T> invoke) throws InvocationException {
        Measurement times = null;
        final long start = System.nanoTime();
        try {
            final T result = invoke.get();
            final long end = System.nanoTime();
            times = Measurement.of(start, end);
            return result;
        } catch (final Exception e) {
            final InvocationException ex = new InvocationException(e, start, System.nanoTime());
            handleException(threadId, ex);
            throw ex;
        } finally {
            if (times != null) {
                timesPerThread.computeIfAbsent(threadId, NEW_LIST).add(times);
            }
        }
    }

    void measure(final long threadId, final Call call) throws InvocationException {
        Measurement times = null;
        final long start = System.nanoTime();
        try {
            call.call();
            final long end = System.nanoTime();
            times = Measurement.of(start, end);
        } catch (final Exception e) {
            final InvocationException ex = new InvocationException(e, start, System.nanoTime());
            handleException(threadId, ex);
            throw ex;
        } finally {
            if (times != null) {
                timesPerThread.computeIfAbsent(threadId, NEW_LIST).add(times);
            }
        }
    }

    private void handleException(final long threadId, final InvocationException e) {
        errorStats.computeIfAbsent(threadId, NEW_QUEUE).add(e);
    }
}
