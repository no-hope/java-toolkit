package org.nohope.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <h1>A Blocking Latch Synchronizer</h1> This class implements a blocking
 * object latch, that acts as a synchronizer between a producer of an object
 * and it's consumer(s).
 * <p/>
 * An object is set with {@code set()} only ONCE. Further attempts to set
 * the object are just ignored.<br>
 * Consumers request the object with {@code get()}. If the object is not
 * already set, consumers are blocked waiting until the object is available or
 * until an {@link InterruptedException interrupt} terminates the wait. The
 * map can be tested for object availability with isAvailable(),
 * which answers true if the object has already been set.
 * <p/>
 *
 * @author Sarveswaran M
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @version 1.1 08/12/08 based on the implementation by Alfred Peisl
 * @see BlockingMap
 */
@ThreadSafe
final class LatchSynchronizer<R> implements IObjectSynchronizer<R> {

    /** The latch counter created and set to 1. */
    private final CountDownLatch latch = new CountDownLatch(1);
    /** lock for set invariant. */
    private final Lock setLock = new ReentrantLock();
    /** The object. */
    /* ==> object is set and got on different threads
     * should be volatile,to get rid of caching issues
     */
    private volatile R object;

    @Override
    public boolean isAvailable() {
        /* ==> this forms an invariant with set(..)
         * should be locked
         */
        setLock.lock();
        try {
            return latch.getCount() == 0;
        } finally {
            setLock.unlock();
        }
    }

    @Override
    public void set(final R o) {
        //==> forms an invariant with isAvailable(..)
        setLock.lock();
        try {
            if (!isAvailable()) {
                this.object = o;
                latch.countDown();
            }
        } finally {
            setLock.unlock();
        }
    }

    @Override
    public R get() throws InterruptedException {
        latch.await();
        //not part of any invariant
        //no need to lock/synchronize
        return object;
    }

    @Override
    public R get(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        latch.await(timeout, unit);
        // not part of any invariant
        // no need to lock/synchronize
        return object;
    }

}
