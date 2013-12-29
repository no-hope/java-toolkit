package org.nohope.test.stress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-26 21:39
 */
public class StressScenario {
    private StressScenario(final TimerResolution resolution) {
        this.resolution = resolution;
    }

    private final TimerResolution resolution;

    protected TimerResolution getResolution() {
        return resolution;
    }

    public static StressScenario of(TimerResolution resolution) {
        return new StressScenario(resolution);
    }

    public StressResult measure(final int threadsNumber,
                               final int cycleCount,
                               final NamedAction... actions)
            throws InterruptedException {

        final Map<String, SingleInvocationStat> result = new HashMap<>();
        for (final NamedAction action : actions) {
            result.put(action.getName(), new SingleInvocationStat(resolution,
                    action));
        }

        final List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadsNumber; i++) {
            final int k = i;
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = k * cycleCount; j < (k + 1) * cycleCount; j++) {
                        try {
                            for (final SingleInvocationStat stats : result.values()) {
                                stats.invoke(k, j);
                            }
                        } catch (final IStressStat.InvocationException e) {
                            // TODO
                        }
                    }
                }
            }, "stress-worker-" + k));
        }

        final long overallStart = resolution.currentTime();
        for (final Thread thread : threads) {
            thread.start();
        }

        for (final Thread thread : threads) {
            thread.join();
        }
        final long overallEnd = resolution.currentTime();

        final double overallApprox = resolution.toSeconds(overallEnd - overallStart);

        int fails = 0;
        for (final SingleInvocationStat stats : result.values()) {
            stats.calculate();
            fails += stats.getFails();
        }

        return new StressResult(result,
                threadsNumber,
                cycleCount,
                fails,
                overallStart, overallEnd,
                overallApprox);
    }

    public StressResult measure(final int threadsNumber,
                                final int cycleCount,
                                final Action action)
            throws InterruptedException {

        action.setScenario(this);
        final List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadsNumber; i++) {
            final int k = i;
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = k * cycleCount; j < (k + 1) * cycleCount; j++) {
                        try {
                            action.doAction(k, j);
                        } catch (final Exception e) {
                            // TODO: print skipped
                        }
                    }
                }
            }, "stress-worker-" + k));
        }



        final long overallStart = resolution.currentTime();
        for (final Thread thread : threads) {
            thread.start();
        }
        for (final Thread thread : threads) {
            thread.join();
        }
        final long overallEnd = resolution.currentTime();

        final double overallApprox = resolution.toSeconds(overallEnd - overallStart);

        int fails = 0;
        for (final MultiInvocationStat stats : action.getMap().values()) {
            stats.calculate();
            fails += stats.getFails();
        }

        action.setScenario(null);
        return new StressResult(action.getMap(),
                threadsNumber,
                cycleCount,
                fails,
                overallStart, overallEnd,
                overallApprox);
    }
}
