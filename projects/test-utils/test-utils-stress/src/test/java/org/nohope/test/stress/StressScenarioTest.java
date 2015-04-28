package org.nohope.test.stress;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.nohope.test.stress.TimeUtils.throughputTo;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-28 00:10
 */
public class StressScenarioTest {

    @Test
    @Ignore("for manual tests only")
    public void roughTest() throws InterruptedException {
        final StressResult m1 =
                StressScenario.of(TimerResolution.MILLISECONDS)
                              .measure(50, 1000, new NamedAction("test1") {
                                  @Override
                                  protected void doAction(final MeasureData p)
                                          throws Exception {
                                      Thread.sleep(10);
                                  }
                              });
        final StressResult m2 =
                StressScenario.of(TimerResolution.NANOSECONDS)
                              .measure(50, 1000, new NamedAction("test2") {
                                  @Override
                                  protected void doAction(final MeasureData p)
                                          throws Exception {
                                      Thread.sleep(10);
                                  }
                              });

        System.err.println(m1);
        System.err.println();
        System.err.println(m2);
        System.err.println("------------------------------------------------");

        final StressResult m3 =
                StressScenario.of(TimerResolution.MILLISECONDS)
                              .measure(50, 1000, new Action() {
                                  @Override
                                  protected void doAction(final MeasureProvider p)
                                          throws Exception {
                                      p.call("test1", () -> Thread.sleep(10));
                                  }
                              });
        final StressResult m4 =
                StressScenario.of(TimerResolution.NANOSECONDS)
                              .measure(50, 1000, new Action() {
                                  @Override
                                  protected void doAction(final MeasureProvider p)
                                          throws Exception {
                                      p.call("test1", () -> Thread.sleep(10));
                                  }
                              });

        System.err.println(m3);
        System.err.println();
        System.err.println(m4);
    }

    @Test
    public void counts() throws InterruptedException {
        {
            final StressResult m =
                    StressScenario.of(TimerResolution.NANOSECONDS)
                                  .measure(2, 100, new NamedAction("test") {
                                      @Override
                                      protected void doAction(final MeasureData p)
                                              throws Exception {
                                          if (p.getOperationNumber() >= 100) {
                                              //System.err.println(p.getOperationNumber());
                                              throw new IllegalStateException();
                                          }
                                          Thread.sleep(1);
                                      }
                                  });

            final Map<String, Result> results = m.getResults();
            assertNotNull(m.toString());
            assertEquals(1, results.size());

            final Result testResult = results.get("test");
            //System.err.println(testResult);
            assertNotNull(testResult);
            final double throughput = testResult.getThroughput();
            final double workerThroughput = testResult.getWorkerThroughput();
            assertTrue(workerThroughput <= throughput);
            final double throughputSeconds = throughputTo(throughput, SECONDS);
            final double workerSeconds = throughputTo(workerThroughput, SECONDS);

            assertTrue("Illegal assumptions : " + throughputSeconds + " <= 2000", throughputSeconds <= 2000);
            assertTrue(workerSeconds <= 1000);

            // FIXME: virtual hosting on travis really weak so there is no way to run this test in such environment
            if (System.getenv("TRAVIS") == null) {
                assertTrue("Illegal assumptions : " + throughputSeconds + " >= 1000", throughputSeconds >= 1000);
                assertTrue("Illegal assumptions : " + workerSeconds + " >= 500", workerSeconds >= 500);
            }

            assertEquals(100, testResult.getRunTimes().size());
            assertEquals(2, testResult.getPerThreadRuntimes().size());
            assertTrue(testResult.getMaxTime() >= testResult.getMinTime());
            assertTrue(testResult.getMaxTime() >= testResult.getMeanTime());
            assertTrue(testResult.getMeanTime() >= testResult.getMinTime());
            assertTrue(m.getRuntime() > 0);
            assertTrue(testResult.getRuntime() > 0);
            assertEquals(100, m.getFails());
            assertEquals(100, testResult.getErrorsPerClass()
                                        .get(IllegalStateException.class)
                                        .size()
            );
        }

        {
            final StressResult m =
                    StressScenario.of(TimerResolution.NANOSECONDS)
                                  .measure(2, 100, new Action() {
                                      @Override
                                      protected void doAction(final MeasureProvider p)
                                              throws Exception {
                                          p.call("test", () -> {
                                              if (p.getOperationNumber() >= 100) {
                                                  throw new IllegalStateException();
                                              }
                                              Thread.sleep(1);
                                          });
                                      }
                                  });

            final Map<String, Result> results = m.getResults();
            assertNotNull(m.toString());
            assertEquals(1, results.size());
            final Result testResult = results.get("test");
            assertNotNull(testResult);
            assertTrue(testResult.getThroughput() <= 2000);
            assertTrue(testResult.getWorkerThroughput() <= 1000);
            assertEquals(100, testResult.getRunTimes().size());
            assertEquals(2, testResult.getPerThreadRuntimes().size());
            assertTrue(testResult.getMaxTime() >= testResult.getMinTime());
            assertTrue(testResult.getMaxTime() >= testResult.getMeanTime());
            assertTrue(testResult.getMeanTime() >= testResult.getMinTime());
            assertTrue(m.getRuntime() > 0);
            assertTrue(testResult.getRuntime() > 0);
            assertTrue(testResult.getAvgRuntimeIncludingWastedNanos() > 0);
            assertTrue(testResult.getAvgWastedNanos() > 0);
            assertTrue(testResult.getAvgWastedNanos() < testResult.getAvgRuntimeIncludingWastedNanos());
            assertEquals(100, m.getFails());
            assertEquals(100, testResult.getErrorsPerClass()
                                        .get(IllegalStateException.class)
                                        .size()
            );
        }

        {
            final StressResult m2 =
                    StressScenario.of(TimerResolution.MILLISECONDS)
                                  .measure(2, 100, new NamedAction("test") {
                                      @Override
                                      protected void doAction(final MeasureData p) throws Exception {
                                          Thread.sleep(10);
                                      }
                                  });
            assertNotNull(m2.toString());
            assertTrue(m2.getRuntime() >= 1);
            assertTrue(m2.getApproxThroughput() <= 200);

            final StressResult m3 =
                    StressScenario.of(TimerResolution.MILLISECONDS)
                                  .measure(2, 100, new Action() {
                                      @Override
                                      protected void doAction(final MeasureProvider p) throws Exception {
                                          p.call("test", () -> Thread.sleep(10));
                                      }
                                  });
            assertNotNull(m3.toString());
            assertTrue(m3.getRuntime() >= 1);
            assertTrue(m3.getApproxThroughput() <= 200);

            final StressResult m4 =
                    StressScenario.of(TimerResolution.MILLISECONDS)
                                  .measure(2, 100, new Action() {
                                      @Override
                                      protected void doAction(final MeasureProvider p) throws Exception {
                                          p.get("test", () -> {
                                              Thread.sleep(10);
                                              return null;
                                          });
                                      }
                                  });
            assertNotNull(m4.toString());
            assertTrue(m4.getRuntime() >= 1);
            assertTrue(m4.getApproxThroughput() <= 200);
        }

        {
            final StressResult m2 =
                    StressScenario.of(TimerResolution.MILLISECONDS)
                                  .measurePooled(2, 100, 2, new PooledAction() {
                                      @Override
                                      protected void doAction(final PooledMeasureProvider p) throws Exception {
                                          p.invoke("test", () -> Thread.sleep(10));
                                      }
                                  });
            assertNotNull(m2.toString());
            assertTrue(m2.getRuntime() >= 1);
            assertTrue(m2.getApproxThroughput() <= 200);
            assertNotNull(m2.toString());

            final StressResult m3 =
                    StressScenario.of(TimerResolution.MILLISECONDS)
                                  .measurePooled(2, 100, 2, new PooledAction() {
                                      @Override
                                      protected void doAction(final PooledMeasureProvider p) throws Exception {
                                          p.invoke("test", () -> {
                                              Thread.sleep(10);
                                              return null;
                                          });
                                      }
                                  });
            assertNotNull(m3.toString());
            assertTrue(m3.getRuntime() >= 1);
            assertTrue(m3.getApproxThroughput() <= 200);
            assertNotNull(m3.toString());
        }
    }

    @Test
    @Ignore("for manual testing")
    public void pooled() throws InterruptedException {
        {
            final AtomicLong atomic = new AtomicLong();
            final StressResult result =
                    StressScenario.of(TimerResolution.NANOSECONDS)
                                  .measure(500, 100, new Action() {
                                      @Override
                                      protected void doAction(final MeasureProvider p) throws Exception {
                                          p.get("test1", () -> {
                                              long old;
                                              for (; ; ) {
                                                  old = atomic.get();
                                                  Thread.sleep(1);
                                                  if (atomic.compareAndSet(old, old + 1)) {
                                                      break;
                                                  }
                                              }
                                              return old;
                                          });
                                      }
                                  });

            System.err.println(result);
        }

        System.err.println("-----------------------------------");

        {
            final AtomicLong atomic = new AtomicLong();
            final StressResult result =
                    StressScenario.of(TimerResolution.MILLISECONDS)
                                  .measurePooled(500, 100, 10, new PooledAction() {
                                      @Override
                                      protected void doAction(final PooledMeasureProvider p) throws Exception {
                                          p.invoke("test1", () -> {
                                              long old;
                                              while (true) {
                                                  old = atomic.get();
                                                  Thread.sleep(1);
                                                  if (atomic.compareAndSet(old, old + 1)) {
                                                      break;
                                                  }
                                              }
                                              return old;
                                          });
                                      }
                                  });

            System.err.println(result);
        }
    }


    @Test
    public void sortedOutput() throws InterruptedException {
        final StressResult result =
                StressScenario.of(TimerResolution.MILLISECONDS).measure(10, 1, new Action() {
                    @Override
                    protected void doAction(final MeasureProvider p) throws Exception {
                        p.call("action4", () -> {
                        });
                        p.call("action1", () -> {
                        });
                        p.call("action2", () -> {
                        });
                        p.call("action3", () -> {
                        });
                    }
                });

        final int action1 = result.toString().indexOf("action1");
        final int action2 = result.toString().indexOf("action2");
        final int action3 = result.toString().indexOf("action3");
        final int action4 = result.toString().indexOf("action4");
        assertTrue(action4 > action3);
        assertTrue(action3 > action2);
        assertTrue(action2 > action1);
    }
}
