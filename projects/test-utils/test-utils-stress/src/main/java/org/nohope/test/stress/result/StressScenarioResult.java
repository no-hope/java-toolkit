package org.nohope.test.stress.result;

import org.nohope.test.stress.InvocationException;
import org.nohope.test.stress.result.metrics.StressMetrics;
import org.nohope.test.stress.util.Measurement;
import org.nohope.test.stress.util.Memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-04-29 15:05
 */
public class StressScenarioResult {

    @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter", "ReturnOfCollectionOrArrayField"})
    public static class ActionStats {
        private final Map<Long, Collection<Measurement>> timesPerThread;
        private final Map<Long, Collection<InvocationException>> errorStats;
        private final String actionName;

        public ActionStats(Map<Long, Collection<Measurement>> timesPerThread,
                           Map<Long, Collection<InvocationException>> errorStats,
                           String actionName) {
            this.timesPerThread = timesPerThread;
            this.errorStats = errorStats;
            this.actionName = actionName;
        }

        public Map<Long, Collection<Measurement>> getTimesPerThread() {
            return timesPerThread;
        }

        public Map<Long, Collection<InvocationException>> getErrorStats() {
            return errorStats;
        }

        public String getActionName() {
            return actionName;
        }
    }

    private final Collection<ActionStats> accumulators = new ArrayList<>();
    private final Collection<StressMetrics> metrics = new ArrayList<>();

    private final long startNanos;
    private final long endNanos;
    private final int threadsNumber;
    private final int cycleCount;

    private final Memory memoryStart;
    private final Memory memoryEnd;

    public StressScenarioResult(final int threadsNumber,
                                final int cycleCount,
                                final long startNanos,
                                final long endNanos,
                                final Collection<ActionStats> accumulators,
                                final Collection<StressMetrics> metrics,
                                final Memory memoryStart,
                                final Memory memoryEnd) {
        this.startNanos = startNanos;
        this.endNanos = endNanos;
        this.threadsNumber = threadsNumber;
        this.cycleCount = cycleCount;
        this.accumulators.addAll(accumulators);
        this.metrics.addAll(metrics);
        this.memoryStart = memoryStart;
        this.memoryEnd = memoryEnd;
    }

    public <T> T interpret(final Interpreter<T> interpreter) {
        return interpreter.interpret(this);
    }

    public void visitError(final ErrorProcessor processor) {
        for (final ActionStats accumulator : accumulators) {
            final String name = accumulator.getActionName();
            for (final Entry<Long, Collection<InvocationException>> e: accumulator.getErrorStats().entrySet()) {
                for (final InvocationException m : e.getValue()) {
                    processor.process(name, e.getKey(), m.getCause(), m.getStartNanos(), m.getEndNanos());
                }
            }
        }
    }

    public void visitResult(final ResultProcessor processor) {
        for (final ActionStats accumulator : accumulators) {
            final String name = accumulator.getActionName();
            for (final Entry<Long, Collection<Measurement>> e : accumulator.getTimesPerThread().entrySet()) {
                for (final Measurement m : e.getValue()) {
                    processor.process(name, e.getKey(), m.getStartNanos(), m.getEndNanos());
                }
            }
        }
    }

    public void visitMetrics(final MetricsProcessor processor) {
        metrics.forEach(processor::process);
    }

    public long getStartNanos() {
        return startNanos;
    }

    public long getEndNanos() {
        return endNanos;
    }

    public int getThreadsNumber() {
        return threadsNumber;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public Memory getMemoryStart() {
        return memoryStart;
    }

    public Memory getMemoryEnd() {
        return memoryEnd;
    }

    public Collection<StressMetrics> getMetrics() {
        return Collections.unmodifiableCollection(metrics);
    }

    public Collection<ActionStats> getActionStats() {
        return Collections.unmodifiableCollection(accumulators);
    }

    @FunctionalInterface
    public interface ResultProcessor {
        void process(final String name, final long threadId, long startNanos, long endNanos);
    }

    @FunctionalInterface
    public interface ErrorProcessor {
        void process(final String name, final long threadId, final Throwable e, long startNanos, long endNanos);
    }

    @FunctionalInterface
    public interface MetricsProcessor {
        void process(final StressMetrics metric);
    }

    @FunctionalInterface
    public interface Interpreter<T> {
        T interpret(final StressScenarioResult result);
    }
}
