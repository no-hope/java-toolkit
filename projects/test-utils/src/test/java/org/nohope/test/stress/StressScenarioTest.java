package org.nohope.test.stress;

import org.junit.Ignore;
import org.junit.Test;

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
                                  protected void doAction(final int threadId, final int operationNumber)
                                          throws Exception {
                                      Thread.sleep(1);
                                  }
                              });
        final StressResult m2 =
                StressScenario.of(TimerResolution.NANOSECONDS)
                              .measure(50, 1000, new NamedAction("test2") {
                                  @Override
                                  protected void doAction(final int threadId, final int operationNumber)
                                          throws Exception {
                                      Thread.sleep(1);
                                  }
                              });

        System.err.println(m1);
        System.err.println();
        System.err.println(m2);
    }
}
