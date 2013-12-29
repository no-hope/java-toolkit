package org.nohope.test.stress;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-27 19:27
 */
public interface IStressStat {
    List<Map.Entry<Long, Long>> getInvocationTimes();
    Map<Class, List<Exception>> getErrorStats();
    int getFails();
    @Nullable Result getResult();
}
