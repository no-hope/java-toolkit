package org.nohope.test.stress;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 18:19
 */
class MeasureData {
    private final int concurrency;

    private final int threadId;
    private final int operationNumber;

    protected MeasureData(final int threadId,
                          final int operationNumber,
                          final int concurrency) {
        this.concurrency = concurrency;
        this.threadId = threadId;
        this.operationNumber = operationNumber;
    }

    public long getThreadId() {
        return threadId;
    }

    public final int getOperationNumber() {
        return operationNumber;
    }


    public int getConcurrency() {
        return concurrency;
    }
}
