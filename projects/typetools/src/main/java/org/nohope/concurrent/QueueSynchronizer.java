package org.nohope.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <H1>A Blocking Object Latch</H1> This class implements a blocking object
 * latch, that acts as a synchronizer between a producer of an object and it's
 * consumer(s).
 * <p/>
 * An object is set with {@code set()} only ONCE. Further attempts to set
 * the object are just ignored.<br>
 * Consumers request the object with {@code get()}. If the object is not
 * already set, consumers are blocked waiting until the object is available or
 * until an interrupt (InterruptedException) terminates the wait. The map can
 * be tested for object availability with {@code isAvailable()}, which answers true if
 * the object has already been set. <br>
 *
 * @author Sarveswaran M
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @version 1.1 08/12/08 based on the implementation by Alfred Peisl
 * @see BlockingQueueMap
 */
@ThreadSafe
final class QueueSynchronizer<R> implements IObjectSynchronizer<R> {

    /** The object. */
    /* ==> object is set and got on different threads
     * should be volatile,to get rid of caching issues
     */
    private final BlockingQueue<R> object = new LinkedBlockingQueue<>();
    /** lock for set invariant. */
    private final Lock setLock = new ReentrantLock();

    @Override
    public boolean isAvailable() {
        /* ==> this forms an invariant with set(..)
         * should be locked
         */
        setLock.lock();
        try {
            return !this.object.isEmpty();
        } finally {
            setLock.unlock();
        }
    }

    @Override
    public void set(final R o) {
        //==> forms an invariant with isAvailable(..)
        setLock.lock();
        try {
            this.object.add(o);
        } finally {
            setLock.unlock();
        }
    }

    @Override
    public R get() {
        //not part of any invariant
        //no need to lock/synchronize
        return object.poll();
    }

    @Override
    public R get(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        // not part of any invariant
        // no need to lock/synchronize
        return object.poll(timeout, unit);
    }
}
