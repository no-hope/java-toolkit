package org.nohope.test.stress;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-27 19:27
 */
public interface IStressStat {
    List<Map.Entry<Long, Long>> getInvocationTimes();
    Map<Class, AtomicInteger> getErrorStats();
    int getFails();

    interface InvocationHandler<T> {
        T invoke() throws Exception;
    }

    class InvocationException extends Exception {
        private static final long serialVersionUID = 397360373479180369L;
    }
}
