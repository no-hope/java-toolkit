package org.nohope.test.stress;

import org.nohope.test.stress.functors.Call;
import org.nohope.test.stress.functors.Get;
import org.nohope.test.stress.result.ActionResult;
import org.nohope.test.stress.util.Measurement;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Map.Entry;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:18
*/
final class StatAccumulator {
    private static final Function<Long, List<Measurement>> NEW_LIST =
            x -> new ArrayList<>();
    private static final Function<Class<?>, ConcurrentLinkedQueue<Exception>> NEW_QUEUE =
            c -> new ConcurrentLinkedQueue<>();

    private final ConcurrentHashMap<Long, List<Measurement>> timesPerThread = new ConcurrentHashMap<>();
    private final Map<Class<?>, ConcurrentLinkedQueue<Exception>> errorStats = new ConcurrentHashMap<>();
    private final AtomicReference<ActionResult> result = new AtomicReference<>();
    private final String name;

    StatAccumulator(final String name) {
        this.name = name;
    }

    @Nonnull
    public ActionResult getResult() {
        if (result.get() == null) {
            calculate();
        }
        return result.get();
    }

    <T> T measure(final long threadId, final Get<T> invoke) throws InvocationException {
        Optional<Measurement> times = Optional.empty();
        try {
            final long start = System.nanoTime();
            final T result = invoke.get();
            final long end = System.nanoTime();
            times = Optional.of(Measurement.of(start, end));
            return result;
        } catch (final Exception e) {
            handleException(e);
            throw new InvocationException();
        } finally {
            // it's safe to use ArrayList here, they are always modified by same thread!
            times.ifPresent(timesPerThread.computeIfAbsent(threadId, NEW_LIST)::add);
        }
    }

    void measure(final long threadId, final Call call) throws InvocationException {
        Optional<Measurement> times = Optional.empty();
        try {
            final long start = System.nanoTime();
            call.call();
            final long end = System.nanoTime();
            times = Optional.of(Measurement.of(start, end));
        } catch (final Exception e) {
            handleException(e);
            throw new InvocationException();
        } finally {
            // it's safe to use ArrayList here, they are always modified by same thread!
            times.ifPresent(timesPerThread.computeIfAbsent(threadId, NEW_LIST)::add);
        }
    }

    private void handleException(final Exception e) {
        final Class<?> aClass = e.getClass();
        errorStats.computeIfAbsent(aClass, NEW_QUEUE).add(e);
    }

    private void calculate() {
        long maxTimeNanos = 0;
        long minTimeNanos = Long.MAX_VALUE;
        long totalDeltaNanos = 0L;

        for (final List<Measurement> perThread : timesPerThread.values()) {
            for (final Measurement e : perThread) {
                final long runtimeNanos = e.getEndNanos() - e.getStartNanos();
                totalDeltaNanos += runtimeNanos;
                if (maxTimeNanos < runtimeNanos) {
                    maxTimeNanos = runtimeNanos;
                }
                if (minTimeNanos > runtimeNanos) {
                    minTimeNanos = runtimeNanos;
                }
            }
        }

        final Map<Class<?>, List<Exception>> eStats = new HashMap<>(errorStats.size());

        for (final Entry<Class<?>, ConcurrentLinkedQueue<Exception>> entry: errorStats.entrySet()) {
            eStats.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        result.set(new ActionResult(
                name,
                timesPerThread,
                eStats,
                totalDeltaNanos,
                minTimeNanos,
                maxTimeNanos));
    }
}
