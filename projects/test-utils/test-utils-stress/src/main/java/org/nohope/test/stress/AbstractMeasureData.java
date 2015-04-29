package org.nohope.test.stress;

import org.nohope.test.stress.functors.Call;
import org.nohope.test.stress.functors.Get;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:19
 */
abstract public class AbstractMeasureData {
    private final int threadId;
    private final int operationNumber;

    protected AbstractMeasureData(final int threadId, final int operationNumber) {
        this.threadId = threadId;
        this.operationNumber = operationNumber;
    }

    public long getThreadId() {
        return threadId;
    }

    public final int getOperationNumber() {
        return operationNumber;
    }

    abstract public <T> T get(final String name, final Get<T> getter) throws Exception;

    abstract public void call(final String name, final Call call) throws Exception;
}
