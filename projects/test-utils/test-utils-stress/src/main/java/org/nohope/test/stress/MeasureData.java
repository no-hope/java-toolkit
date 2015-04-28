package org.nohope.test.stress;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:19
 */
public class MeasureData {
    private final int threadId;
    private final int operationNumber;

    protected MeasureData(final int threadId, final int operationNumber) {
        this.threadId = threadId;
        this.operationNumber = operationNumber;
    }

    public long getThreadId() {
        return threadId;
    }

    public final int getOperationNumber() {
        return operationNumber;
    }
}
