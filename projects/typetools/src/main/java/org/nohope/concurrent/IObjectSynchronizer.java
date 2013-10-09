package org.nohope.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Interface for object stored in {@link org.nohope.concurrent.AbstractBlockingMap AbstractBlockingMap}. Realization of
 * that interface should acts as a synchronizer between a producer of an
 * object and it's consumer(s).
 * <p/>
 * {@link org.nohope.concurrent.AbstractBlockingMap} delegates storing objects to a synchronizer.
 * So every time {@link org.nohope.concurrent.AbstractBlockingMap#put(Object, Object) put} or
 * {@link org.nohope.concurrent.AbstractBlockingMap#take(Object) take}/
 * {@link org.nohope.concurrent.AbstractBlockingMap#poll(Object, Timeout) poll} called
 * {@link #get() get} or {@link #set(Object) set} will be called respectively.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 29/03/11 1:01 PM
 */
interface IObjectSynchronizer<R> {

    /**
     * Checks if the object is already available (has been already set).
     *
     * @return {@code true}, if the object is available
     */
    boolean isAvailable();

    /**
     * Sets the object if it is not already set.
     *
     * @param object the object
     */
    void set(R object);

    /**
     * Get the object if it is already available (has already been set).
     * <p>
     * If it is not available, wait until it is or until an interrupt
     * (InterruptedException) terminates the wait.
     *
     * @return the object if it is already available (has already been set)
     * @throws InterruptedException on getting interrupt
     */
    R get() throws InterruptedException;

    /**
     * Get the object if it is already available (has already been set).
     * <p>
     * If it is not available, wait until it is or until an interrupt
     * (InterruptedException) terminates the wait.
     *
     * @param timeout timeout in time units
     * @param unit time unit
     * @return the object if it is already available (has already been set)
     * @throws InterruptedException on getting interrupt
     */
    R get(long timeout, TimeUnit unit) throws InterruptedException;
}
