package org.nohope.concurrent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <h2>A Blocking Map</h2> This class implements a blocking map, that acts as a
 * synchronizer between the producers of a objects and the consumers.
 * <p/>
 * Object are set with {@code put()} only ONCE. Further attempts to set an
 * object are just ignored. Consumers request the object with
 * {@code get(key)} or with {@code take(take)}. If the object is not
 * already set, consumers are blocked waiting until the object is available or
 * until an interrupt (InterruptedException) terminates the wait.
 * {@code take(take)} gets and removes the object from the map. The map can
 * be tested for object availability with {@code isAvailable()}, which answers true if
 * the object has already been set.
 *
 * @author Sarveswaran M
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @version 1.1, 08/12/08 based on the implementation by Alfred Peisl
 */
abstract class AbstractBlockingMap<K, V> implements IBlockingMap<K, V> {

    /** The queues map. */
    private final ConcurrentMap<K, IObjectSynchronizer<V>> map =
            new ConcurrentHashMap<>();

    /** lock for remove invariant. */
    private final Lock removeLock = new ReentrantLock();

    /**
     * map containing blocked threads,necessary to interrupt all threads waiting
     * on keys in a cleared map.
     */
    private final Map<Thread, IObjectSynchronizer<V>> blockedThreadsMap =
            new ConcurrentHashMap<>();

    /**
     * Sets the object with the given key if it is not already set.
     * Otherwise ignore this request.
     *
     * @param key object key
     * @param object the object
     * @return V always null
     */
    @Override
    @Nullable
    public final V put(final K key, final V object) {
        IObjectSynchronizer<V> latch = map.get(key);

        // part of remove invariant,should be locked
        removeLock.lock();
        try {
            if (latch == null) {
                map.putIfAbsent(key, makeSynchronizer());
                latch = map.get(key);
            }
            latch.set(object);
        } finally {
            removeLock.unlock();
        }

        return null;
    }

    @Override
    public final V poll(final K key, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        final V result;
        IObjectSynchronizer<V> object = map.get(key);

        // part of remove invariant, should be locked
        removeLock.lock();
        try {
            if (object == null) {
                map.putIfAbsent(key, makeSynchronizer());
                object = map.get(key);
            }
        } finally {
            removeLock.unlock();
        }

        // put thread in map before awaiting
        blockedThreadsMap.put(Thread.currentThread(), object);
        result = object.get(timeout, unit);
        // remove thread after awaiting
        blockedThreadsMap.remove(Thread.currentThread());

        return result;
    }

    @Override
    public boolean isAvailable(final K key) {
        // ==> part of remove invariant, should be locked
        removeLock.lock();
        try {
            final IObjectSynchronizer<V> latch = map.get(key);
            return latch != null && latch.isAvailable();
        } finally {
            removeLock.unlock();
        }
    }

    //FIXME: taking element is removing key! (think about queue impl)
    @Override
    public V take(final K key) throws InterruptedException {
        final V result;
        IObjectSynchronizer<V> latch = map.get(key);

        // part of remove invariant,should be locked
        removeLock.lock();
        try {
            if (latch == null) {
                map.putIfAbsent(key, makeSynchronizer());
                latch = map.get(key);
            }
        } finally {
            removeLock.unlock();
        }

        // put thread in map before awaiting
        blockedThreadsMap.put(Thread.currentThread(), latch);
        result = latch.get();
        // remove thread after awaiting
        blockedThreadsMap.remove(Thread.currentThread());

        // ==> part of remove invariant, should be locked
        removeLock.lock();
        try {
            map.remove(key);
        } finally {
            removeLock.unlock();
        }
        return result;
    }

    @Override
    public final V poll(final K key, final Timeout t)
            throws InterruptedException, TimeoutException {
        final V value = poll(key, t.time(), t.timeUnit());
        if (value == null) {
            throw new TimeoutException("Timeout getting '"
                                       + key + "' from queue");
        }
        return value;
    }

    /**
     * Removes all mappings from this map.
     * <p/>
     * Interrupts any threads waiting on any key in map before clearing.
     * This is done to prevent threads being blocked forever.
     */
    @Override
    public final void clear() {
        // part of remove invariant,should be locked
        removeLock.lock();
        try {
            for (final Thread thread : blockedThreadsMap.keySet()) {
                thread.interrupt();
            }
            map.clear();
        } finally {
            removeLock.unlock();
        }
    }

    @Override
    public final boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Creates new {@link IObjectSynchronizer} instance.
     *
     * @return new synchronizer instance
     */
    protected abstract IObjectSynchronizer<V> makeSynchronizer();

    //------------------------------------------------------------------------
    // Unsupported methods
    //------------------------------------------------------------------------

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public final boolean containsKey(final Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public final boolean containsValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public final V get(final Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    @Nonnull
    public final Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    @Nonnull
    public final Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public final void putAll(@Nonnull final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public final V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public final int size() {
        throw new UnsupportedOperationException();
    }

    /**
     * operation not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    @Nonnull
    public final Collection<V> values() {
        throw new UnsupportedOperationException();
    }

}
