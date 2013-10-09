package org.nohope.concurrent;


import javax.annotation.concurrent.ThreadSafe;

/**
 * <h1>Blocking Queue Map.</h1> This class implements blocking map with queue
 * values.
 *
 * @param <K> key type
 * @param <V> value type
 */
@ThreadSafe
public final class BlockingQueueMap<K, V> extends AbstractBlockingMap<K, V> {
    @Override
    protected IObjectSynchronizer<V> makeSynchronizer() {
        return new QueueSynchronizer<>();
    }
}
