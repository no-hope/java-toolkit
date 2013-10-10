package org.nohope.concurrent;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Interface for blocking map.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 6/10/11 4:38 PM
 */
public interface IBlockingMap<K, V> extends Map<K, V> {

    /**
     * Poll message of key from message queue.
     *
     * @param key K
     * @param t Timeout time to wait if message is unavailable
     * @return V message
     *
     * @throws InterruptedException on interrupting poll event
     * @throws TimeoutException if timeout exceeded
     */
    V poll(K key, Timeout t) throws InterruptedException, TimeoutException;

    /**
     * Get the object with the given key if it is already available (has already
     * been set).
     * <p/>
     * If it is not available, wait until it is or until an interrupt
     * (InterruptedException) terminates the wait.
     *
     * @param key object key
     * @param timeout long
     * @param unit TimeUnit
     * @return V the object if it is already available
     *
     * @throws InterruptedException if map is cleared while waiting on this get
     * @throws TimeoutException if timeout exceeded
     */
    V poll(K key, long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException;

    /**
     * Answer and remove the object with the given key if it is already
     * available (has already been set).
     * <p/>
     * If it is not available, wait until it is or until an interrupt
     * (InterruptedException) terminates the wait.
     *
     * @param key object key
     * @return the object if it is already available (has already been set)
     *
     * @throws InterruptedException if map is cleared while waiting on this
     * take
     */
    V take(K key) throws InterruptedException;

    /**
     * Checks if the object is already available (has been already set).
     *
     * @param key object key
     * @return true, if the object is already available (has been already set)
     */
    boolean isAvailable(K key);
}
